<script setup lang="ts">
import { ElMenu, ElIcon, ElMenuItem } from 'element-plus'
import { ref } from 'vue'
import { useRouter } from 'vue-router';

const router = useRouter()
const MenuList = ref([
  {
    index: '1',
    title: '我的任务',
    icon: 'tickets',
  },
  {
    index: '2',
    title: '返回任务中心',
    icon: 'tickets',
  },
])
//@ts-ignore
const handleSelect = (index, indexPath) => {
  console.log(index, indexPath)
  switch (index) {
    case '1':
      router.push({ name: 'myTasks' });
      break;
    case '2':
      router.push({ name: 'taskSelector' });
      break;
    default:
      break;
  }
}

</script>

<template>
  <el-menu @select="handleSelect">
    <el-menu-item v-for="(item,i) in MenuList" :key="i" :index="item.index" :icon="item.icon">
                <el-icon size="32">
                    <component :is="item.icon"></component>
                </el-icon>
                <h3>{{ item.title }}</h3>
            </el-menu-item>
  </el-menu>
</template>
