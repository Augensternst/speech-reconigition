<template>
  <el-form ref="modelInfor" :model="modelInf" label-width="120px">
    <el-form-item label="模型名称" prop="name">
      <el-input class='input-width'
                autosize
                type="textarea"
                v-model="modelInf.name" placeholder="Model Name"/>
    </el-form-item>
    <el-form-item label="简述" prop="description">
      <el-input autosize
                type="textarea"
                v-model="modelInf.description"
                placeholder="Description"/>
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-select class="select-width" v-model="modelInf.status" placeholder="Status">
        <el-option label="启用" value="1"/>
        <el-option label="禁用" value="0"/>
      </el-select>
    </el-form-item>

    <el-form-item label="模型风格" prop="style">
      <el-select v-model="modelInf.style" placeholder="Select" style="width: 240px">
        <el-option
            v-for="item in modelStyle"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
        <template #footer>
          <el-button v-if="!isAdding" text bg size="small" @click="onAddOption">
            Add an option
          </el-button>
          <template v-else>
            <el-input
                v-model="optionName"
                class="option-input"
                placeholder="input option name"
                size="small"
            />
            <el-button type="primary" size="small" @click="onConfirm">
              confirm
            </el-button>
            <el-button size="small" @click="clear">cancel</el-button>
          </template>
        </template>
      </el-select>
    </el-form-item>
    <el-form-item label="上传模型">
      <el-upload
          :on-success="handleModelSuccess"
          :before-upload="beforeModelUpload"
          :on-remove="handleRemove"
          drag
          :show-file-list="true"
      >
        <el-icon class="el-icon--upload">
          <upload-filled/>
        </el-icon>
        <div class="el-upload__text">
          Drop file here or <em>click to upload</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            choose the model file
          </div>
        </template>
      </el-upload>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm(modelInf)">Submit</el-button>
      <el-button @click="resetForm">Cancel</el-button>
    </el-form-item>
  </el-form>

</template>

<style scoped>
.input-width {
  width: 300px;
}

.select-width {
  width: 100px;
}

.option-input {
  width: 100%;
  margin-bottom: 8px;
}
</style>


<script lang="ts" setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router';
import {CheckboxValueType, ElMessage} from 'element-plus'
import axiosInstance from "../../axios";
const isAdding = ref(false)
const value = ref<CheckboxValueType[]>([])
const optionName = ref('')
const modelFile =ref()
const modelStyle = ref([
  {
    value: 'reinforcement-learning',
    label: '强化学习',
  },
  {
    value: 'CNN',
    label: 'CNN',
  },

])
const router = useRouter();
const resetForm = () => {
  console.log('reset')

  router.go(-1);
};

const onAddOption = () => {
  isAdding.value = true
}

const onConfirm = () => {
  if (optionName.value) {
    modelStyle.value.push({
      label: optionName.value,
      value: optionName.value,
    })
    clear()
  }
}

const clear = () => {
  optionName.value = ''
  isAdding.value = false
}

const modelInf = ref({
  name: '',
  style: '',
  description: '',
  status: '',
})

const submitForm = (modelInf: any) => {
  const url = '/model/'
  const formData = new FormData()
  formData.append('file', modelFile.value as File)
  formData.append('name', modelInf.name)
  formData.append('style', modelInf.style)
  formData.append('status', modelInf.status)
  formData.append('description', modelInf.description)
  for (let [key, value] of formData.entries()) {
    console.log(key, value);
  }
  axiosInstance.post(url, formData).then((res) => {
    if (res.data.code === 200) {
      ElMessage.success('上传成功')
      resetForm()
    } else {
      ElMessage.error('上传失败: ' + res.data.message)
      console.log(res.data)
    }
  })
}
const handleModelSuccess = (res: any, file: any) => {
  console.log("Ok!")
}
const beforeModelUpload = (file: any) => {
  console.log(file)
  modelFile.value = file
  return false
}
const handleRemove = (file: any, fileList: any) => {
  console.log(file, fileList)
}

</script>
