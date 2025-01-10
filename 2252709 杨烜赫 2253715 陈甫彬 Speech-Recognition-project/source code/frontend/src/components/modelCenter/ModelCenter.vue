<template>
  <div style="float: right;">
    <el-button type="primary" @click="add_model">添加模型</el-button>
    <el-button type="primary" @click="getModelData">刷新</el-button>
  </div>

  <el-table :data="pagedData">
    <el-table-column prop="id" label="模型编号"></el-table-column>
    <el-table-column prop="name" label="模型名称"></el-table-column>
    <el-table-column prop="style" label="模型风格"></el-table-column>
    <el-table-column prop="status" label="模型状态"></el-table-column>
    <el-table-column prop="description" label="模型描述"></el-table-column>
    <el-table-column prop="uploaded_time" label="创建时间"></el-table-column>
    <el-table-column label="操作">
      <template #default="scope">
        <el-button type="success" @click="handleEdit(scope.$index, scope.row)">修改</el-button>
        <el-button type="danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-dialog title="修改模型信息" v-model="dialogVisible" width="40%" :before-close="handleClose">
    <el-form :model="modelForm" :rules="formRules" ref="formRef" label-width="100px">
      <el-form-item label="模型编号" prop="id">
        <el-input v-model="modelForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="模型名称" prop="name">
        <el-input v-model="modelForm.name"></el-input>
      </el-form-item>
      <el-form-item label="模型风格" prop="style">
        <el-input v-model="modelForm.style"></el-input>
      </el-form-item>
      <el-form-item label="模型状态" prop="status">
        <el-input v-model="modelForm.status"></el-input>
      </el-form-item>
      <el-form-item label="模型描述" prop="description">
        <el-input v-model="modelForm.description"></el-input>
      </el-form-item>
      <el-form-item label="模型文件" prop="file">
        <el-upload drag accept=".zip" style="width: 100%;" :auto-upload="false" ref="uploadRef" :limit="1" v-model:file-list="fileList">
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
              * 上传模型文件
            </div>
          </template>
        </el-upload>
      </el-form-item>

    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
    </span>
  </el-dialog>

  <el-dialog title="删除模型" v-model="deleteVisible" width="30%" :before-close="handleClose">
    <el-card class="box-card">
      <p>确定删除id为{{modelForm.id}} 的模型?</p>
    </el-card>
    <span slot="footer" class="dialog-footer">
      <el-button @click="deleteVisible = false">取 消</el-button>
      <el-button type="primary" @click="deleteRow">确 定</el-button>
    </span>
  </el-dialog>
  <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-size="pageSize"
      :hide-on-single-page="true"
      layout="total, prev, pager, next, jumper"
      :total="tableData.length">
  </el-pagination>
</template>

<script setup lang="ts">
import {useRouter} from 'vue-router';
import {ref,onMounted} from 'vue'
import axiosInstance from "../../axios";
import {ElMessage, type FormInstance, type UploadUserFile} from "element-plus";
//改成组合式
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const pagedData = ref([]);
const router = useRouter();
const dialogVisible = ref(false);
const deleteVisible = ref(false);
const fileList = ref<UploadUserFile[]>([])
const modelForm = ref({
  name: '',
  style: '',
  status: '',
  description: '',
  file: '',
  id: ''
});
const formRules = ref({
  name: [
    {required: true, message: '请输入模型名称', trigger: 'blur'}
  ],
  style: [
    {required: true, message: '请输入模型风格', trigger: 'blur'}
  ],
  status: [
    {required: true, message: '请输入模型状态', trigger: 'blur'}
  ],
  description: [
    {required: true, message: '请输入模型描述', trigger: 'blur'}
  ],
});
const formRef = ref<FormInstance | null>(null)

function handleSizeChange(val: any) {
  pageSize.value = val;
  updatePagedData();
}

function handleCurrentChange(val: any) {
  currentPage.value = val;
  updatePagedData();
}

function updatePagedData() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  pagedData.value = tableData.value.slice(start, end);
}

function add_model() {
  router.push({name: 'model-add'});
}


const getModelData = async () => {
  const url = '/model/all'
  await axiosInstance.get(url).then((res) => {
    if (res.data.code === 200) {
      tableData.value = res.data.data
      updatePagedData()
      return
    } else {
      ElMessage.error('获取模型失败: ' + res.data.message)
      return
    }
  })
};

const handleEdit = (index: number, row: any) => {
  console.log(index, row);
  modelForm.value = row;
  dialogVisible.value = true;
};
onMounted(async () => {
  await getModelData()
})
const handleClose = (done: any) => {
  deleteVisible.value = false;
  dialogVisible.value = false;
  done();
};

const handleDelete = async (index: number, row: any) => {
  console.log(index, row);
  modelForm.value = row;
  deleteVisible.value = true;
};
const updateRow = async () => {
  if (!formRef.value) {
    ElMessage.error('请输入模型信息')
    return
  }
  await formRef.value?.validate((valid: any, _) => {
    if (valid) {
      const url = '/model/update'
      const data = {
        id: modelForm.value.id,
        name: modelForm.value.name,
        style: modelForm.value.style,
        status: modelForm.value.status,
        description: modelForm.value.description,
      }
      const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
      axiosInstance.post(url, data, {headers}).then((res) => {
        if (res.data.code === 200) {
          ElMessage.success('修改成功')
          getModelData()
          dialogVisible.value = false
        } else {
          ElMessage.error('修改失败: ' + res.data.message)
          console.log(res.data)
        }
      })
    } else {
      ElMessage.error('修改失败，请检查模型信息')
    }
  })
}
const deleteRow = async () => {
  const url = '/model/'+modelForm.value.id
  await axiosInstance.delete(url).then((res) => {
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      getModelData()
      deleteVisible.value = false
    } else {
      ElMessage.error('删除失败: ' + res.data.message)
      console.log(res.data)
    }
  })
}
</script>