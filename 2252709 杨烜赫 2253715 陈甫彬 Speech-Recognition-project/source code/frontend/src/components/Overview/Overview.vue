<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

const isVoiceInteraction = ref(false);
const aiResponse = ref<{ text: string, type: string, content: string[] }[]>([]);
const aiImages = ref<{ [key: string]: string[] }>({});
const selectedResponse = ref<string | null>(null);
const isTaskAreaFocused = ref(false);
const isAIResponseFocused = ref(false);
const recognizedText = ref(''); // 存储识别的文本
const displayText = ref(''); // 动画显示的文本

// 添加对 SpeechRecognition 的类型定义
declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

let recognition: (typeof window.SpeechRecognition) | null = null;

// 启动语音识别
function startVoiceInteraction() {
  isVoiceInteraction.value = true;
  aiResponse.value = []; // 清空AI回复
  selectedResponse.value = null;
  isTaskAreaFocused.value = true;
  isAIResponseFocused.value = false;
  recognizedText.value = '';
  displayText.value = '';
  recognition?.start(); // 开始语音识别
}

// 停止语音识别并显示AI回复
function stopVoiceInteraction() {
  isVoiceInteraction.value = false;
  isTaskAreaFocused.value = false;
  isAIResponseFocused.value = true;
  recognition?.stop();
  getAiResponse(recognizedText.value);
}

function getAiResponse(text: string) {
  const requestBody = {
    studentId: 2,
    response: text,
    phase: 2,
    taskId: 2
  };

  axios.post('http://localhost:3001/api/ai/questions', requestBody)
    .then(response => {
      // Handle the response from the backend
      console.log(response.data);
      const questions = response.data;
      if (Array.isArray(questions)) {
        aiResponse.value = questions.map((item: { question: string, hints: string[] }) => ({
          text: item.question,
          type: 'image',
          content: item.hints.map(hint => `data:image/png;base64,${hint}`)
        }));
      } else {
        console.error('Unexpected questions format:', questions);
      }
    })
    .catch(error => {
      console.error('Error fetching AI response:', error);
    });
}

// AI回复点击逻辑
function handleAIResponseClick(response: { text: string, type: string, content: string[] }) {
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

// 语音识别初始化
onMounted(() => {
  recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
  recognition.lang = 'zh-CN';
  recognition.continuous = true;
  recognition.interimResults = true;

  recognition.onresult = (event: { resultIndex: any; results: string | any[] }) => {
    let finalTranscript = '';
    let interimTranscript = '';
    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        finalTranscript += event.results[i][0].transcript;
      } else {
        interimTranscript += event.results[i][0].transcript;
      }
    }
    recognizedText.value = finalTranscript + interimTranscript;
    displayText.value = recognizedText.value; // 动态更新显示文本
  };

  recognition.onerror = (event: { error: string }) => {
    console.error('Speech recognition error detected: ' + event.error);
  };
});

onUnmounted(() => {
  recognition?.stop();
  recognition = null;
});
</script>

<template>
  <div class="container">
    <div
      class="task-area"
      :class="{ 'full-screen': isVoiceInteraction, 'blurred': aiResponse.length > 0 && !isTaskAreaFocused, 'focused': isTaskAreaFocused }"
    >
      <!-- 任务区域内容 -->
      <div class="task-images">
        <img src="../../assets/image/牛吃草1.png" alt="Image 1" width="20%" />
        <img src="../../assets/image/牛吃草2.png" alt="Image 2" width="20%" />
        <img src="../../assets/image/牛吃草3.png" alt="Image 3" width="20%" />
      </div>
      <div v-if="displayText" class="animated-text">{{ displayText }}</div>
    </div>

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
    <div class="voice-button-container" :class="{ 'bottom-right': aiResponse.length > 0 }">
      <div
        class="voice-button"
        :class="{ active: isVoiceInteraction }"
        @click="isVoiceInteraction ? stopVoiceInteraction() : startVoiceInteraction()"
      >
        🎤
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

.task-images {
  display: flex;
  justify-content: center;
  align-items: center;
}

.task-images img {
  max-width: 30%;
  object-fit: contain;
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
  background:  rgb(255, 193, 8);
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
  background: rgb(232, 211, 145);
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
  width: 20% ;
  display: inline-flex;
  flex-wrap: nowrap; /* Ensure images are displayed in a single row */
}

</style>
