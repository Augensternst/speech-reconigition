<script setup lang="ts">
import { ElMenu, ElSubMenu, ElIcon, ElMenuItem, ElText } from 'element-plus'
import { ref } from 'vue'
import { useRouter } from 'vue-router';

const router = useRouter()
const MenuList = ref([
    
    {
        index: '2',
        title: '任务中心',
        icon: 'Menu',
    },
    {
        index: '3',
        title: '个人中心',
        icon: 'Histogram',
    },
])
//@ts-ignore
const handleSelect = (index, indexPath) => {
    // 上级html路由为：<el-main><router-view name = "home-main" /></el-main>，我们只对home-main部分进行路由跳转
    console.log(index, indexPath)
    switch (index) {
        case '1':
            router.push({ name: 'home-overview'});
            break;
        case '2':
            router.push({ name: 'taskSelector' });
            break;
        case '3':
            router.push({ name: 'personal-center' });
            break;
        default:
            break;
    }
}

</script>

<template>
    <el-menu default-active="1" @select="handleSelect">
        <template v-for="(item, i) in MenuList" :key="i">
            <!-- 如果有 children，则使用子菜单 -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.index">
                <template #title>
                    <el-icon size="32">
                        <component :is="item.icon"></component>
                    </el-icon>
                    <h3>{{ item.title }}</h3>
                </template>
                <el-menu-item
                    v-for="(child, j) in item.children"
                    :key="j"
                    :index="child.index"
                >
                    <el-text size="large">
                        <h4>{{ child.title }}</h4>
                    </el-text>
                </el-menu-item>
            </el-sub-menu>
            <!-- 如果没有 children，则使用普通菜单项 -->
            <el-menu-item v-else :index="item.index">
                <el-icon size="32">
                    <component :is="item.icon"></component>
                </el-icon>
                <h3>{{ item.title }}</h3>
            </el-menu-item>
        </template>
    </el-menu>
</template>