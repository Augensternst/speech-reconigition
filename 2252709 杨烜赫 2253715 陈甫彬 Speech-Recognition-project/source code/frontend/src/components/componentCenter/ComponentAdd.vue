<template>
  <div>
    <el-row>
      <el-col :span="12">
        <el-form
            :model="componentForm"
            :rules="formRules"
            ref="componentFormRef"
            label-width="100px"
            class="demo-ruleForm"
        >
          <el-form-item label="组件名称" prop="name">
            <el-input v-model="componentForm.name" class="input-width"></el-input>
          </el-form-item>
            
          <el-form-item label="组件状态" prop="status">
            <el-select v-model="componentForm.status" placeholder="请选择" class="select-width">
              <el-option label="崭新" value=0 />
              <el-option label="良好" value=1 />
              <el-option label="一般" value=2 />
              <el-option label="破损" value=3 />
            </el-select>
          </el-form-item>

          <el-form-item label="组件描述" prop="description">
            <el-input v-model="componentForm.description" class="input-width"></el-input>
          </el-form-item>
            
          <el-form-item label="组件位置" prop="location">
            <el-input v-model="componentForm.location" class="input-width"></el-input>
          </el-form-item>
            
          <el-form-item label="模型" prop="model">
            <el-select  v-model="componentForm.modelId" placeholder="请选择" class="select-width" @visible-change="getComponentOptions">
              <el-option
                  v-for="item in ModelOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
            
          <el-form-item label="组件图片" prop="pic">
            <el-upload
                class="upload-demo"
                :onSuccess="handleAvatarSuccess"
                :beforeUpload="beforeAvatarUpload"
                :show-file-list="false"
                :onRemove="handleRemove"
            >
              <el-image
                  style="width: 200px; height: 200px"
                  :src="picUrl"
                  fit="fill"
              >
                <template #placeholder>
                  <div style="width: 200px; height: 200px; line-height: 200px; text-align: center;">点击上传</div>
                </template>
                <template #error>
                  <div style="width: 200px; height: 200px; line-height: 200px; text-align: center; background:lightgrey">点击上传</div>
                </template>
              </el-image>
            </el-upload>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm">提交</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>

      <el-col :span="12">
        <el-collapse v-model="componentForm.modelId">
          <el-collapse-item title="Model Details" :name="componentForm.modelId">
            <div v-for="item in selectedModel" :key="item.id">
              <p>id: {{ item.id }}</p>
              <p>name: {{ item.name }}</p>
              <p>style: {{ item.style }}</p>
              <p>status: {{ item.status }}</p>
              <p>description: {{ item.description }}</p>
              <p>uploaded_time: {{ item.uploaded_time }}</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-col>
    </el-row>
  </div>
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
import { ref ,onMounted,watch} from 'vue'
import {ElMessage} from 'element-plus'
import axiosInstance from "../../axios.ts";


interface ComponentInfor {
  name: string
  status: string
  description: string
  location: string
  modelId: number | null
  life_forecast: number | string
  pic: File
}
interface  ModelInf {
  id: number
  name: string
  style: string
  status: string
  description: string
  uploaded_time: string
  md5: string
}
const picUrl = ref('')
const componentForm = ref<ComponentInfor>({
  name: '',
  status: '',
  description: '',
  location: '',
  modelId: null, //kongzhi
  life_forecast: '',
  pic : new File([''], 'filename')
})
const selectedModel = ref<ModelInf[]>([])

//监视函数当model改变时触发，将选择的model信息赋值给selectedModel
watch(() => componentForm.value.modelId, (val:any) => {
  selectedModel.value = ModelOptions.value.filter((item) => item.id === val)
})


const formRules = {
  name: [
    { required: true, message: '请输入组件名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择组件状态', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入组件描述', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入组件位置', trigger: 'blur' }
  ],
  modelId: [
    { required: true, message: '请选择模型', trigger: 'change' }
  ],
  life_forecast: [
    { required: true, message: '请输入寿命预测', trigger: 'blur' }
  ],
  pic: [
    { required: true, message: '请上传组件图片', trigger: 'change' }
  ]
}

let ModelOptions= ref<ModelInf[]>([])
const defaultModelOpt = [
  {
    id: null,
    name: 'model01',
    style: 'A123',
    status: 'active',
    description: 'this is model01',
    uploaded_time: '2021-10-01',
    md5: '123456'
  },
  {
    id: 1,
    name: 'model02',
    style: 'B456',
    status: 'active',
    description: 'this is model02',
    uploaded_time: '2021-10-02',
    md5: '123456'
  },
  {
    id: 2,
    name: 'model03',
    style: 'C789',
    status: 'active',
    description: 'this is model03',
    uploaded_time: '2021-10-03',
    md5: '123456'
  }
]


async function submitForm() {
  ElMessage.success('添加组件')
  console.log(componentForm.value)
  const formData = new FormData()
  formData.append('name', componentForm.value.name)
  if(componentForm.value.status !== null && componentForm.value.status !== undefined){
    formData.append('status', componentForm.value.status.toString())
  }
  formData.append('description', componentForm.value.description)
  formData.append('location', componentForm.value.location)
  //@ts-ignore
  formData.append('model', componentForm.value.modelId)
  formData.append('pic', componentForm.value.pic)


  await axiosInstance.post('/component/upload', formData).then((res) => {
    if (res.data.code === 200) {
      ElMessage.success('添加组件成功')
      resetForm()
    } else {
      console.log('添加组件失败: ' + res.data.message)
      ElMessage.error('添加组件失败: ' + res.data.message)
    }
  })
}
const resetForm = async () => {
  console.log('reset')
  componentForm.value.name = ''
  componentForm.value.status = ''
  componentForm.value.description = ''
  componentForm.value.location = ''
  componentForm.value.modelId = null
  componentForm.value.life_forecast = ''
  componentForm.value.pic = new File([''], 'filename')
  picUrl.value = ''
};

const handleAvatarSuccess = (_: any, file: any) => {
  console.log("Ok!")
  picUrl.value = URL.createObjectURL(file.raw)
}

const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isGIF = file.type === 'image/gif'
  const isLt6M = file.size / 1024 / 1024 < 6

  if (!isJPG && !isPNG && !isGIF) {
    ElMessage.error('上传头像图片只能是 JPG/PNG/GIF 格式!')
  }
  if (!isLt6M) {
    ElMessage.error('上传头像图片大小不能超过 6MB!')
  }
  if((isJPG || isPNG || isGIF) && isLt6M){
    const render = new FileReader();
    render.onload = (e) => {
      picUrl.value = e.target?.result as string
    }
    render.readAsDataURL(file)
    console.log("src: ",picUrl.value)
    componentForm.value.pic = file
    return false;
  }
  return false;
}
const handleRemove = () => {
  picUrl.value = ''
}

const getComponentOptions =  () => {
  const url = '/model/all'
  axiosInstance.get(url).then((res) => {
    if (res.data.code === 200) {
      ModelOptions.value = res.data.data
      console.log("模型信息"+ModelOptions.value)
    } else {
      ElMessage.error('获取组件失败: ' + res.data.message)
      ModelOptions.value = defaultModelOpt
    }
  })
}

onMounted(async () => {
  getComponentOptions()
});
</script>
