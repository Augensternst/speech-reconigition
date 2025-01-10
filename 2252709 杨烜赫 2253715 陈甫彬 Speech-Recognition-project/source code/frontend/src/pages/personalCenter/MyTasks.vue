<template>
  <el-table :data="myTasks" height="250" style="width: 100%">
    <el-table-column prop="id" label="任务编号" width="540" />
    <el-table-column prop="taskName" label="名称" width="540" />
  </el-table>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import $ from 'jquery'
import {useStore} from "vuex";

const store = useStore();
const myTasks = ref([])
const refreshMyTasks = () => {
  $.ajax({
    url: "http://localhost:3001/getTasks",
    type: "get",
    data: { // 使用 data 传递查询参数
      userId: store.state.user.id, // 从 store 中获取 userId
    },
    headers: {
      Authorization: "Bearer " + store.state.user.token
    },
    success(resp) {
      if (resp.success === "true") {
        myTasks.value = resp.data;
        
      } else {
        console.log(resp);
        myTasks.value = resp;
        console.log(resp.data);
        console.log("234")
        console.log(myTasks.value);
      }
    },
    error(resp) {
      console.log(resp);
      myTasks.value = resp.data;
      
    }
  })
}

onMounted(()=>{
  refreshMyTasks()
})

</script>

<style scoped>

</style>
