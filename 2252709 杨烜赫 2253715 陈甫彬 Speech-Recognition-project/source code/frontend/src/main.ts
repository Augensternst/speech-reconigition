import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './routers/routers.ts'
import ECharts from 'vue-echarts'
import VueECharts from 'vue-echarts'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'echarts'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import store from './store/index.js'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(router).use(store).use(ElementPlus)
app.component('v-chart', VueECharts).component('e-chart', ECharts)
app.mount('#app')
