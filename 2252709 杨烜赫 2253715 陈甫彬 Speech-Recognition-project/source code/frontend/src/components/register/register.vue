<template>
  <div>
    <el-card class="box-card" style="width: 500px;margin: 0 auto;">
      <el-form label-width="80px">
        <el-form-item label="用户名" for="username">
          <el-input v-model="username" id="username" placeholder="请输入用户名" type="text"></el-input>
        </el-form-item>
        <el-form-item label="密码" for="password">
          <el-input v-model="password" id="password" placeholder="请输入密码" type="password"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" for="confirmedPassword">
          <el-input v-model="confirmedPassword" id="confirmedPassword" placeholder="请再次输入密码" type="password"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" for="email">
          <el-input v-model="email" id="email" placeholder="请输入邮箱" type="text"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-upload
              class="upload-demo"
              :onSuccess="handleAvatarSuccess"
              :beforeUpload="beforeAvatarUpload"
              :show-file-list="false"
              :onRemove="handleRemove"
          >
            <el-avatar :src="avatar" shape="circle" size="large">user</el-avatar>
          </el-upload>
        </el-form-item>
        <div class="error-message">{{ error_message }}</div><br>
        <div class="btnGroup">
          <el-button type="primary" @click="register">注册</el-button>
          <router-link to="/login">
            <el-button style="margin-left:10px">登录</el-button>
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>

</template>
<script setup>
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElCard,
  ElButton,
  ElMessage,
  ElAvatar,
  ElUpload,
} from "element-plus"
import { ref } from 'vue'
import $ from 'jquery'
import router from '../../routers/routers.ts'

const username = ref("")
const password = ref("")
const confirmedPassword = ref("")
const email = ref("")
const avatar = ref("")
const error_message = ref("")
const register = () => {
  error_message.value = ""
  $.ajax({
    url: "http://localhost:3001/user/account/register/",
    type: "post",
    data: {
      username: username.value,
      password: password.value,
      confirmedPassword: confirmedPassword.value,
      email: email.value,
      avatar: avatar.value,
    },
    success(resp){
      console.log(resp)
      error_message.value = resp.error_msg
      setTimeout(() => {
        router.push("/login")
      }, 2000)
    },
    error(resp){
      error_message.value = resp.error_msg
    }
  })
}

const handleAvatarSuccess = (res, file) => {
  avatar.value = URL.createObjectURL(file.raw)
}
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isGIF = file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG && !isGIF) {
    ElMessage.error('上传头像图片只能是 JPG/PNG/GIF 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  if((isJPG || isPNG || isGIF) && isLt2M){
    const render = new FileReader();
    render.onload = (e) => {
      avatar.value = e.target?.result
    }
    render.readAsDataURL(file)
    console.log("src: ",avatar.value)
    avafile.value = file
    return false;
  }
  return false;
}
const handleRemove = () => {
  avatar.value = ""
}

</script>

<style scoped>
div.error-message{
  color:red;
}
</style>
