<template>
  <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
    <el-button type="primary" @click="add_component">添加组件</el-button>
  </div>

  <el-space direction="vertical" alignment="flex-start" style="margin-bottom: 20px;">
    <el-skeleton style="width: 240px" :loading=false animated :count="3">

      <template #default>
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in lists" :key="item.name">
            <el-card :body-style="{ padding: '0px', marginBottom: '1px' }">
              <img :src="item.imgUrl" class="image multi-content" style="height: 250px; object-fit: contain;" />
              <div style="padding: 14px">
                <span>{{ item.name }}</span>
                <div class="bottom card-header">
                  <el-countdown title="life forecast" :value="item.life_forecast" />
                  <el-button text class="button" bg @click="handleEdit(item)">编辑</el-button>
                  <el-button text class="button" type="primary" @click="handlePredict(item)" bg>预测</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>

    </el-skeleton>
  </el-space>


  <el-drawer
    v-model="drawer"
    :title="currentItem?.name"
    direction="rtl"

  >
    <el-form label-width="120px">
      <el-form-item label="ID">
        <el-input disabled v-model="currentItem.id"></el-input>
      </el-form-item>

      <el-form-item label="Life Forecast">
        <el-input disabled v-model="currentItem.life_forecast" />
      </el-form-item>

      <el-form-item label="Location">
        <el-input v-model="uploadItem.location" />
      </el-form-item>
      <el-form-item label="Status">
        <el-input v-model="uploadItem.status" />
      </el-form-item>

      <el-form-item label="Name">
        <el-input v-model="uploadItem.name" />
      </el-form-item>
      <el-form-item label="Model">
        <!-- 使用modelList，显示当前model的name -->
        <el-select v-model="uploadItem.model__name" placeholder="请选择" class="select-width">
          <el-option
            v-for="item in modelList"
            :key="item.model__id"
            :label="item.model__name"
            :value="item.model__id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Image">
        <img :src="currentItem.imgUrl" style="max-width: 200px; width: auto; height: auto;" />
        <el-upload action="https://jsonplaceholder.typicode.com/posts/">
          <el-button slot="trigger" size="small" type="primary">选择照片</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item style="display: flex; justify-content: space-between;">
        <el-button type="danger" @click="handleRemove()">移除</el-button>
        <el-button type="primary" @click="drawer = false">Save</el-button>
      </el-form-item>
    </el-form>

  </el-drawer>
  <el-dialog v-model="dialogVisible"  title="Predict">
    <el-upload
      class="upload-demo"
      drag
      action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
      multiple
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        Drop file here or <em>click to upload</em>
      </div>
    </el-upload>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">Cancel</el-button>
      <el-button type="primary" @click="Predict">Confirm</el-button>
    </span>
  </el-dialog>

</template>

<script lang="ts" setup>
import axiosInstance from "../../axios.ts";
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router';

const router = useRouter();
interface ListItem {
  id: number
  imgUrl: string
  name: string
  model__id: number
  model__name: string
  life_forecast: number 
  updated_time: string
  location: string
  status: string
}
interface model{
  model__name: string
  model__id: number
}

interface uploadInfor{
  component_id:number
  name:string
  status: string
  location:string
  description:string
  imgUrl:string
  model__id: number
  model__name: string
}

const dialogVisible = ref(false);
const drawer = ref(false)
const loading = ref(true)
const lists = ref<ListItem[]>([])
const currentItem = ref<ListItem | null>(null);
const uploadItem = ref<uploadInfor | null>(null);


function add_component() {
  router.push({ name: 'component-add' });
}

//model数组
const modelList = ref<model[]>([])

//预测函数
const handlePredict = (item: ListItem) => {
  dialogVisible.value = true;
  currentItem.value = item;
}

const Predict = () => {

  //正在预测
  ElMessageBox.alert('预测成功')
  currentItem.value!.life_forecast = Date.now() + Math.random() * 1000 * 60 * 60 * 24;
  dialogVisible.value = false;
};
const handleRemove = () => {
  ElMessageBox.confirm('Are you sure you want to remove this?')
    .then(() => {
      lists.value = lists.value.filter((item) => item.id !== currentItem.value?.id)
      const url = '/component/delete/' + currentItem.value?.id
      axiosInstance.delete(url).then(() => {
        drawer.value = false
        ElMessageBox.alert('删除成功')
      })
        .catch(() => {
          // catch error
          ElMessageBox.alert('删除失败')
        })
    })
    .catch(() => {
      // catch error
    })
}


const handleEdit = (item: ListItem) => {
  drawer.value = true;
  currentItem.value = item;
  uploadItem.value = {
    component_id: item.id,
    name: item.name,
    status: item.status,
    location: item.location,
    description: item.model__name,
    imgUrl: item.imgUrl,
    model__id: item.model__id,
    model__name: item.model__name
  }
};

//获取所有组件信息，/component/all_user
const getComponentList = async () => {
  const url = '/component/all_user'
  axiosInstance.get(url).then((res) => {
    console.log("组件信息"+res.data.data)
    lists.value = res.data.data
    //给每个lists的life_forecast本身值加上Date.now()
    lists.value.forEach((item) => {
      item.life_forecast = Date.now() + item.life_forecast
    })
    //会返回model__name和model__id,赋值给modelList,
    modelList.value = res.data.data.map((item: ListItem) => {
      return {
        model__name: item.model__name,
        model__id: item.model__id
      }
    })
    //去重
    modelList.value = modelList.value.filter((item, index, self) =>
      index === self.findIndex((t) => (
        t.model__id === item.model__id
      ))
    )
    lists.value.forEach((item) => {
       getComponentPic(item.id)
    })
  }).catch((err) => {
    console.log(err)
  })
}

const getComponentPic = async (id: number) => {
  const url = '/component/pic/'+id
  axiosInstance.get(url, { responseType: 'blob' }).then((res) => {
    lists.value.forEach((item) => {
    const blob = new Blob([res.data], { type: res.headers['content-type'] });
    if(item.id === id){
        item.imgUrl = URL.createObjectURL(blob);
      }
    })
  }).catch((err) => {
    console.log(err)
  })
}



onMounted(() => {
  loading.value = false
  getComponentList()
})
</script>
<style scoped>
.image {
  width: 100%;
  height: auto;
}
body {
  margin: 0;
}
.example-showcase .el-loading-mask {
  z-index: 9;
}
</style>
