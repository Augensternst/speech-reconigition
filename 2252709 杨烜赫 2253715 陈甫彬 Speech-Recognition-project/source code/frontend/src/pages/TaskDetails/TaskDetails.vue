<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const store = useStore();
const taskId = route.params.task_id as string; // 获取任务ID
const userId = Number(store.state.user.id); // 获取用户ID
const token = store.state.user.token; // 获取token
const phase = ref(1);
const isRecognizing = ref(false);  // 记录是否正在进行语音识别
const isVoiceInteraction = ref(false);
const aiResponse = ref<{ text: string, type: string, content: string[] }[]>([]);
const aiImages = ref<{ [key: string]: string[] }>({});
const selectedResponse = ref<string | null>(null);
const isTaskAreaFocused = ref(false);
const isAIResponseFocused = ref(false);
const recognizedText = ref(''); // 存储识别的文本
const displayText = ref(''); // 动态显示的文本
const task = ref<any>(null); // 存储任务对象

//依据路由传过来的taskId来找到对应的任务
const fetchTask = async (id: string) => {
  try {
    const response = await axios.get('http://localhost:3001/getTaskDetails', {
      params: { taskId: id }, // 使用 params 传递查询参数
      headers: {              // 在请求头中添加 Authorization
        Authorization: `Bearer ${token}`,
      },
    });
    task.value = response.data.task; // 假设后端返回任务对象
  } catch (error) {
    console.error('获取任务失败:', error);
  }
};

function setPhase(phaseNumber: number) {
      phase.value = phaseNumber;
    }

// 添加对 SpeechRecognition 的类型定义
declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

let audioContext: AudioContext | null = null;
let audioStream: MediaStream | null = null;
let socket: WebSocket | null = null;
let isRecording = false;
const SAMPLE_RATE = 16000;
const BUFFER_SIZE = 4096;

function updateRecognitionResult(text: string) {
  recognizedText.value = text;
  displayText.value = text; // 动态更新显示文本
}

function connectWebSocket() {
  const wsUrl = `ws://localhost:3001/ws?token=${token}`;
  socket = new WebSocket(wsUrl);

  socket.onopen = () => {
    console.log('WebSocket连接成功');
    isRecognizing.value = true;  // 连接成功后开始语音识别
  };

  socket.onclose = (event) => {
    console.log('WebSocket连接关闭', event);
    isRecognizing.value = false;  // WebSocket关闭后停止语音识别
    if (isRecording) {
      setTimeout(connectWebSocket, 1000);
    }
  };

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      if (data.result) {
        updateRecognitionResult(data.result);
      } else if (data.text) {
        updateRecognitionResult(data.text);
      } else if (data.error) {
        updateRecognitionResult('错误: ' + data.error);
      }
    } catch (e) {
      console.error('解析消息失败:', e);
    }
  };

  socket.onerror = (error) => {
    console.error('WebSocket错误:', error);
    isRecognizing.value = false
  };
}

async function initAudio() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        channelCount: 1,
        sampleRate: SAMPLE_RATE,
        sampleSize: 16,
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true,
      },
    });

    audioStream = stream;
    audioContext = new (window.AudioContext)({
      sampleRate: SAMPLE_RATE,
    });

    const source = audioContext.createMediaStreamSource(stream);
    const processor = audioContext.createScriptProcessor(BUFFER_SIZE, 1, 1);

    source.connect(processor);
    processor.connect(audioContext.destination);

    processor.onaudioprocess = function (e) {
      if (!isRecording || !socket || socket.readyState !== WebSocket.OPEN) return;

      const inputData = e.inputBuffer.getChannelData(0);
      const pcmData = new Int16Array(inputData.length);

      for (let i = 0; i < inputData.length; i++) {
        const gain = 1.5;
        let sample = Math.max(-1, Math.min(1, inputData[i] * gain));
        pcmData[i] = sample < 0 ? sample * 0x8000 : sample * 0x7FFF;
      }

      socket.send(pcmData.buffer);
    };

    return true;
  } catch (error) {
    console.error('初始化音频失败:', error);
    return false;
  }
}

async function startVoiceInteraction() {
  isVoiceInteraction.value = true;
  aiResponse.value = []; // 清空AI回复
  selectedResponse.value = null;
  isTaskAreaFocused.value = true;
  isAIResponseFocused.value = false;
  recognizedText.value = '';
  displayText.value = '';

  if (!socket || socket.readyState !== WebSocket.OPEN) {
    connectWebSocket();
  }

  if (!audioContext) {
    const initialized = await initAudio();
    if (!initialized) {
      alert('无法启动麦克风，请检查麦克风权限');
      return;
    }
  }

  isRecording = true;
}

function stopVoiceInteraction() {
  if (!isRecording) return;
  isVoiceInteraction.value = false;
  isTaskAreaFocused.value = false;
  isAIResponseFocused.value = true;
  isRecording = false;

  if (audioStream) {
    audioStream.getTracks().forEach((track) => track.stop());
    audioStream = null;
  }

  if (audioContext) {
    audioContext.close();
    audioContext = null;
  }

  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.close();
  }

  getAiResponse(recognizedText.value);
}

function getAiResponse(text: string) {
  const requestBody = {
    studentId: userId,
    response: text,
    phase: phase.value,
    taskId: taskId, // Use the actual taskId
  };

  axios
    .post('http://localhost:3001/api/ai/questions', requestBody,{
    headers: {              // 在请求头中添加 Authorization
        Authorization: `Bearer ${token}`,
      },}
    )
    .then((response) => {
      // Handle the response from the backend
      console.log(response.data);
      const questions = response.data;
      if (Array.isArray(questions)) {
        aiResponse.value = questions.map((item: { question: string; hints: string[] }) => ({
          text: item.question,
          type: 'image',
          content: item.hints.map((hint) => `data:image/png;base64,${hint}`),
        }));
      } else {
        console.error('Unexpected questions format:', questions);
      }
    })
    .catch((error) => {
      console.error('Error fetching AI response:', error);
    });
}

// AI回复点击逻辑
function handleAIResponseClick(response: { text: string; type: string; content: string[] }) {
  if (selectedResponse.value === response.text) {
    selectedResponse.value = null;
    if (response.type === 'image') {
      aiImages.value[response.text] = [];
    }
  } else {
    selectedResponse.value = response.text;
    if (response.type === 'image') {
      aiImages.value[response.text] = response.content;
    }
  }
  isAIResponseFocused.value = true;
  isTaskAreaFocused.value = false;
}

onMounted(() => {
  fetchTask(taskId);
});

onUnmounted(() => {
  stopVoiceInteraction();
});
</script>

<template>
  <div class="container">
    <div
      class="task-area"
      :class="{ 'full-screen': isVoiceInteraction, 'blurred': aiResponse.length > 0 && !isTaskAreaFocused, 'focused': isTaskAreaFocused }"
    >
    <!-- 按钮选择框 -->
    <div class="phase-selector">
      <button
        :class="{ active: phase === 1 }"
        @click="setPhase(1)"
      >
        完整性考验
      </button>
      <button
        :class="{ active: phase === 2 }"
        @click="setPhase(2)"
      >
        逻辑性考验
      </button>
      <button
        :class="{ active: phase === 3 }"
        @click="setPhase(3)"
      >
        情感性考验
      </button>
    </div>
    <!-- <div class="phase-selector">
          <label for="phase">选择考验类型：</label>
          <select id="phase" v-model="phase">
            <option value="1">完整性考验</option>
            <option value="2">逻辑性考验</option>
           <option value="3">情感性考验</option>
         </select>
       </div> -->
      <!-- 任务区域内容 -->
      <div v-if="task"   class="task-images">
        
        <img v-for="image in task.images" :key="image.imageOrder" :src="image.imageUrl"  alt="任务图片" />
      </div>
      <div v-if="displayText" class="animated-text">{{ displayText }}</div>
      
    </div>
    <!-- 选择考验类型 -->
    
    <!-- AI回复 -->
    <div v-if="aiResponse.length > 0" class="ai-response">
      <div
        v-for="response in aiResponse"
        :key="response.text"
        @click="handleAIResponseClick(response)"
      >
        <div class="message">
          <img class="avatar" src="../../assets/image/robot.png" alt="Robot Avatar" />
          <div class="response">
            <div>{{ response.text }}</div>
            <div v-if="response.type === 'text' && selectedResponse === response.text">
              {{ response.content }}
            </div>
            <div v-if="response.type === 'image' && selectedResponse === response.text">
              <span
                v-for="image in aiImages[response.text]"
                :key="image"
                >
                <img :src="image" :alt="'Image ' + image" class="ai-image"/>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 圆形语音按钮 -->
    <!-- <div class="voice-button-container" :class="{ 'bottom-right': aiResponse.length > 0 }">
      <div
        class="voice-button"
        :class="{ active: isVoiceInteraction }"
        @click="isVoiceInteraction ? stopVoiceInteraction() : startVoiceInteraction()"
      >
        🎤
      </div>
    </div> -->
    <div class="voice-button-container" :class="{ 'bottom-right': aiResponse.length > 0 }">
  <div
    class="voice-button"
    :class="{ active: isVoiceInteraction, recognizing: isRecognizing }"
    @click="isVoiceInteraction ? stopVoiceInteraction() : startVoiceInteraction()"
  >
    <span v-if="isRecognizing">🎤</span> <!-- 正在识别时显示麦克风图标 -->
    <span v-else>🎤</span>  <!-- 默认显示麦克风图标 -->
  </div>
</div>
  </div>
</template>

<style lang="less" scoped>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f9f7f7;
}

.task-area {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgb(151, 218, 247);
  position: relative;
  margin: 0; /* 移除外边距 */
  padding: 0; /* 移除内边距 */
}
.phase-selector {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.phase-selector button {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  background-color: #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.phase-selector button.active {
  background-color: #007bff;
  color: white;
}
/* 取消悬停时变色的效果 */
.phase-selector button:hover {
  background-color: #f0f0f0;
}

.phase-selector button.active:hover {
  background-color: #007bff; /* 保持选中的按钮颜色不变 */
}
.task-images {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center; /* 居中图片 */
  align-items: center; /* 垂直居中图片 */
  gap: 10px; /* 图片间距 */
}

.task-images img {
    width:20vw;
    height:40vh;
  object-fit: cover; /* 图片填充 */
  margin: 0 5px;
}

.animated-text {
  font-size: 2.5rem;
  font-weight: bold;
  color: #21b371;
  animation: fade-in 1s ease-in-out;
  margin-top: 20px;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.ai-response {
  width: 100%;
  background:  rgb(151, 218, 247);
}

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 12px;
}

.response {
  background: rgb(107, 182, 236);
  padding: 10px 15px;
  border-radius: 18px;
  text-align: left;

  
  max-width: 60vw;
}

.voice-button-container {
  position: fixed;
  bottom: 50px;
  display: flex;
  justify-content: center;
  width: auto;
  &.bottom-right {
    right: 50px;
    justify-content: flex-end;
  }
}

.voice-button {
  width: 80px;
  height: 80px;
  background-color: #42b883;
  border-radius: 50%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  color: white;
  cursor: pointer;
  transition: transform 0.3s, background-color 0.3s;
}

.voice-button.active {
  background-color: #2fc47a;
  transform: scale(1.2);
}
.ai-image {
  
  display: inline-flex;
  flex-wrap: nowrap; /* Ensure images are displayed in a single row */
  margin-left: 10px;
  height: 100px; /* Set a fixed height for images */
}
// .voice-button.recognizing {
//   background-color: #42b883; /* 识别中时的背景色 */
// }

</style>
