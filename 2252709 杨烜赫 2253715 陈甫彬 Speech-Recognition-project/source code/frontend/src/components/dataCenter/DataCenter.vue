<!-- <template>
  <div style="height: 90%">
    <div style="display:flex; right: 0;justify-content: flex-end; top: 0; z-index: 1;">
      <el-button-group>
        <el-button type="primary" round @click="importDataset">导入数据集</el-button>
        <el-button type="primary" round @click="getdataMsg">刷新数据表</el-button>
      </el-button-group>
    </div>
    <el-auto-resizer>
      <template #default="{ height, width }">
        <el-table-v2 :data="dataMsg" :width="width"
                     :height="height"
                     :columns="columns"
                     :fixed="true">
        </el-table-v2>
      </template>
    </el-auto-resizer>
  </div>
  <el-dialog
      v-model="dialogVisible"
      title="导入数据"
      width="30%"
      :before-close="handleClose"
  >
    <el-card class="box-card">
      <el-form
          :ref="formRef"
          :model="uploadForm"
          :rules="uploadRules"
          label-width="100px"
          size="default"
      >
        <el-form-item label="数据集名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入数据集名称"></el-input>
        </el-form-item>
        <el-form-item label="机器名称" prop="mechineName">
          <el-input v-model="uploadForm.mechineName" placeholder="请输入机器名称"></el-input>
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-select v-model="uploadForm.component" placeholder="请选择组件" style="width: 115px">
            <el-option
                v-for="item in ComponentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="personInCharge">
          <el-input v-model="uploadForm.personInCharge" placeholder="请输入负责人"></el-input>
        </el-form-item>
      </el-form>
    </el-card>
    <el-upload drag accept=".xlsx" :auto-upload="false" ref="uploadRef" :limit="1" v-model:file-list="fileList">
      <template #trigger>
        <el-icon class="el-icon--upload">
          <upload-filled/>
        </el-icon>
        <div class="el-upload__text">
          拖拽到此处或 <em>点击此处上传</em>
        </div>
      </template>
      <template #tip>
        <div class="el-upload__tip">
          * 数据需要符合相应格式，否则无法处理
        </div>
      </template>
    </el-upload>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetUploadForm">取消</el-button>
        <el-button type="primary" @click="submitUpload">
          上传
        </el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog
      v-model="deleteVisible"
      title="删除数据"
      width="30%"
      :before-close="handleClose"
  >
    <el-card class="box-card" shadow="hover">
      <p>确定删除id为{{deleteRowData}} 的数据?</p>
    </el-card>
    <span slot="footer" class="dialog-footer">
      <el-button @click="deleteVisible = false">取 消</el-button>
      <el-button type="primary" @click="deleteRequest">确 定</el-button>
    </span>
  </el-dialog>
</template>
<script lang="tsx" setup>
import {computed, onMounted, ref} from 'vue'
import type {UploadUserFile} from 'element-plus'
import {ElAutoResizer, ElMessage, FormInstance, UploadInstance,ElButton} from 'element-plus'
import axiosInstance from "../../axios";

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])
const dialogVisible = ref(false)
const deleteVisible = ref(false)
let formRef = ref<FormInstance | null>(null)
const componentSelect = ref('')
const deleteRowData = ref('')
const uploadForm = ref({
  name: '',
  mechineName: '',
  component: '',
  personInCharge: '',
})
const uploadRules = ref({
  name: [
    {required: true, message: '请输入数据集名称', trigger: 'blur'},
  ],
  mechineName: [
    {required: true, message: '请输入机器名称', trigger: 'blur'},
  ],
  component: [
    {required: true, message: '请输入组件', trigger: 'blur'},
  ],
  personInCharge: [
    {required: true, message: '请输入负责人', trigger: 'blur'},
  ],
})

const dataMsg = ref<
    {
      filename: string
      machine_name: string
      component_name: string
      component_type: string
      owner: string
    }[]
>([])


const getdataMsg = async () => {
  const url = '/data/all_user'
  await axiosInstance.get(url).then((res) => {
    if (res.data.code === 200) {
      dataMsg.value = res.data.data
      console.log(dataMsg.value)
      return
    } else {
      ElMessage.error('获取数据失败: ' + res.data.message)
      console.log(res.data)
      return
    }
  })
}
const generateColumnsFromItem = (item: any) => {
  return Object.keys(item).map(key => ({
    key: key,
    dataKey: key,
    title: key.charAt(0).toUpperCase() + key.slice(1).replace(/_/g, ' '), // 格式化标签
    width: 170,
  }));
}
const generateColumns = () => {
  if (dataMsg.value.length === 0) return [];
  // 使用数据的第一项来确定列
  const item = dataMsg.value[0];
  // 添加一个按扭列，用于预测
  const predictColumn = {
    key: 'operation',
    title: '操作',
    width: 160,
    cellRenderer: ({rowData}) => (
      <>
        <ElButton type="success" round  onClick={() => predict(rowData.id)}>预测</ElButton>
        <ElButton type="danger" round onClick={() => deleteRow(rowData.id)}>删除</ElButton>
      </>
    ),
  };
  return [
    ...generateColumnsFromItem(item),
    predictColumn,
  ];
}

const importDataset = () => {
  dialogVisible.value = true
}
const handleClose = (done: any) => {
  resetUploadForm()
  done()
}

const submitUpload = () => {
  if (uploadForm.value.component === '' || uploadForm.value.mechineName === ''
      || uploadForm.value.name === '' || uploadForm.value.personInCharge === ''
      || fileList.value.length === 0) {
    ElMessage({
      message: '请检查输入表单输入',
      type: 'error'
    })
    resetUploadForm()
    return
  } else {
    const file = fileList.value[0].raw

    const formData = new FormData()
    formData.append('file',file as File)
    formData.append('name', uploadForm.value.name)
    formData.append('component', uploadForm.value.component)
    const url = '/data/'
    axiosInstance.post(url, formData).then((res) => {
      if (res.data.code === 200) {
        ElMessage.success('上传成功')
        resetUploadForm()
      } else {
        ElMessage.error('上传失败: ' + res.data.message)
        resetUploadForm()
      }
    })

  }
}
const resetUploadForm = () => {
  uploadForm.value.component = ''
  uploadForm.value.mechineName = ''
  uploadForm.value.name = ''
  uploadForm.value.personInCharge = ''
  componentSelect.value = ''
  fileList.value = []
  dialogVisible.value = false
  deleteVisible.value = false
}
const predict = async (rowData: any) => {
  console.log("row: ", rowData);
  return
}
const deleteRow = async (rowData: any) => {
  console.log("row: ", rowData);
  deleteRowData.value = rowData
  deleteVisible.value = true
  return
}
const deleteRequest = async () => {
  const url ="/data/"+deleteRowData.value
  await axiosInstance.delete(url).then((res) => {
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      getdataMsg()
      deleteVisible.value = false
    } else {
      ElMessage.error('删除失败: ' + res.data.message)
      console.log(res.data)
    }
  })
}

// 挂载函数
onMounted(async () => {
  await getdataMsg();
  await getComponentOptions()
});

// 计算属性，用于根据数据动态生成列
const columns = computed(() => generateColumns());

const getComponentOptions = async () => {
  const url = '/component/all_user'
  await axiosInstance.get(url).then((res) => {
    if (res.data.code === 200) {
      let componentOptions: any[] = []
      res.data.data.map((item: any) => {
        componentOptions.push({
          value: item.id,
          label: item.name
        })
      })
      ComponentOptions = componentOptions
      console.log(ComponentOptions)
    } else {
      ElMessage.error('获取组件失败: ' + res.data.message)
      ComponentOptions = options
    }
  })
}
let ComponentOptions: any[] = []
const options = [
  {
    value: 'Option1',
    label: 'Option1',
  },
  {
    value: 'Option2',
    label: 'Option2',
  },
  {
    value: 'Option3',
    label: 'Option3',
  },
  {
    value: 'Option4',
    label: 'Option4',
  },
  {
    value: 'Option5',
    label: 'Option5',
  },
]
</script> -->

<template>
  <el-table :data="tableData">
    <el-table-column prop="time" label="时间"></el-table-column>
    
    <el-table-column prop="componentName" label="组件名称"></el-table-column>
    <el-table-column prop="remainingLife" label="剩余寿命/mins"></el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';

interface TableData {
  time: string;
  componentId: number;
  componentName: string;

  remainingLife: number;
}

const tableData = ref<TableData[]>([]);
//给 tableData 赋测试值
tableData.value = [
  { time: '2023-11-01-13:20', componentName: 'Engine',componentId: 2, remainingLife: 1021 },
  {
    time: '2024-03-02-14:21', componentName: 'Lathe', componentId: 2, remainingLife: 2024
  },
  {
    time: '2024-03-23-21:30', componentName: 'Controller',componentId:6, remainingLife: 376
  },
];

onMounted(async () => {
      // const response = await axios.get('/api/components');
      // console.log("数据："+response.data);
  // tableData.value = response.data;
    });

</script>
<!-- 
<template>
  <el-table v-loading="loading" :data="tableData" style="width: 100%">
    <el-table-column prop="date" label="Date" width="180" />
    <el-table-column prop="name" label="Name" width="180" />
    <el-table-column prop="address" label="Address" />
  </el-table>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const loading = ref(true)

const tableData = [
  {
    date: '2016-05-02',
    name: 'John Smith',
    address: 'No.1518,  Jinshajiang Road, Putuo District',
  },
  {
    date: '2016-05-04',
    name: 'John Smith',
    address: 'No.1518,  Jinshajiang Road, Putuo District',
  },
  {
    date: '2016-05-01',
    name: 'John Smith',
    address: 'No.1518,  Jinshajiang Road, Putuo District',
  },
]
</script>

<style>
body {
  margin: 0;
}
.example-showcase .el-loading-mask {
  z-index: 9;
}
</style> -->
