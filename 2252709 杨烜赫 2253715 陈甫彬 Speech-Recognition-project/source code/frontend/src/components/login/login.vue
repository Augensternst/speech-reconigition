<template>
  <div>
    <el-card class="box-card" style="width: 400px; margin: 100px auto;">
      <h2>登录</h2>
      <el-form label-width="80px">
        <el-form-item label="用户名" for="username">
          <el-input v-model="username" id="username" placeholder="请输入用户名" type="text"></el-input>
        </el-form-item>
        <el-form-item label="密码" for="password">
          <el-input v-model="password" id="password" placeholder="请输入密码" type="password"></el-input>
        </el-form-item>
        <div class="error-message">{{ error_message }}</div><br>
        <div class="btnGroup">
          <el-button type="primary" @click="login">登录</el-button>
          <router-link to="/register">
            <el-button style="margin-left: 10px">注册</el-button>
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="js">
import { ref } from "vue";
import {useStore} from "vuex";
import { ElFormItem, ElForm, ElCard, ElButton, ElInput, ElMessage } from "element-plus";
import router from '../../routers/routers.ts'

const store = useStore();
const username = ref("");
const password = ref("");
const error_message = ref("");

const login = () => {
  error_message.value = ""
  store.dispatch("login", {
    username: username.value,
    password: password.value,
    success(){                       //  成功跳转至主页
      store.dispatch("getInfo", {
        success(){
          ElMessage.success("登录成功，即将跳转首页");
          setTimeout(()=>{
            router.push('/home')
          }, 2000)
        },
      })
    },
    error(){
      error_message.value = "用户名或密码错误"
    }
  })
};
</script>

<style scoped>
div.error-message{
  color:red;
}

</style>
