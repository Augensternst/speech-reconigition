<template>
    <div class="task-selector">
      <div 
        v-for="task in tasks" 
        :key="task.id" 
        class="task-card" 
        @click="navigateToTask(task.id)"
      >
        <!-- <img :src="getFirstImage(task)" alt="任务图片" /> -->
         <img :src="task.imageDescription" alt="任务图片" />
        <p class="task-description">{{ task.taskName || '任务说明' }}</p>
      </div>
    </div>
</template>
  
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { useStore } from 'vuex';
  
  const tasks = ref([]);
  const router = useRouter();
  const store = useStore();
  const token = store.state.user.token;
  const fetchTasks = async () => {
    try {console.log(token);
      const response = await axios.get('http://localhost:3001/getAllTasks',
      {headers: {              // 在请求头中添加 Authorization
        Authorization: `Bearer ${token}`,
      }}
    ); // 假设存在一个API返回任务数据
      
      tasks.value = response.data.data; // 任务数据数组
    } catch (error) {
      console.error('获取任务失败', error);
    }
  };
  
  const getFirstImage = (task) => {
    const sortedImages = task.images.sort((a, b) => a.imageOrder - b.imageOrder);
    return sortedImages.length > 0 ? sortedImages[0].imageUrl : '';
  };
  
  const navigateToTask = (taskId) => {
    router.push(`/task/${taskId}`);
  };
  
  onMounted(fetchTasks);
  </script>
  
  <style scoped>
  .task-selector {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
    padding: 16px;
    background-color: rgb(151, 218, 247); /* 背景颜色 */
  }
  
  .task-card {
    width: 200px;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background-color: #ffffff; /* 卡片背景颜色 */
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    text-align: center;
  }
  
  .task-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  }
  
  .task-card img {
    width: 100%;
    height: auto;
    border-radius: 8px;
  }
  
  .task-description {
    margin-top: 12px;
    font-size: 16px;
    font-weight: 500;
    color: #333333; /* 字体颜色 */
    line-height: 1.5;
  }
  
  .task-card:hover .task-description {
    color: #1a73e8; /* 鼠标悬停时字体颜色变化 */
  }
  </style>
  
  