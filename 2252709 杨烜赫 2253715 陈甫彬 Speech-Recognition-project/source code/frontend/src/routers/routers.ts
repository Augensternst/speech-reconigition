import {createRouter, createWebHistory} from 'vue-router'
import {ElMessage} from "element-plus";
//@ts-ignore
import {useStore} from 'vuex'
import store from '../store/index.js'

const _routes = [
    {
        path: '/home',
        component: () => import('../components/home/Home.vue'),
        children: [
            {
                path: '', // home的默认孩子路由
                name: 'home-default',
                component: () => import('../pages/personalCenter/MyTasks.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: 'overview',
                name: 'home-overview',
                component: () => import('../pages/TaskDetails/TaskDetails.vue'),
                meta: {
                    requestAuth: true,
                }
            }
        ],
    },
    {   
        path: '/task',
        component: () => import('../components/home/Home.vue'),
        children: [
            {
                path: '', // taskSelector的默认孩子路由
                name: 'taskSelector',
                component: () => import('../pages/TaskSelector/TaskSelector.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: ':task_id', // 动态路由定义
                name: 'taskDetail',
                component: () => import('../pages/TaskDetails/TaskDetails.vue'), // 假设有一个 TaskDetail 页面
                meta: {
                    requestAuth: true,
                },
            },
        ]
    },

    {
        path:'/login',
        component:()=>import('../components/login/login.vue'),
        name:'login',
        meta: {
            requestAuth: false,
        }
    },
    {
        path: '/',
        redirect: '/login', //将根路径重定向到 /login
    },
    {
        path: '/model',
        component: () => import('../components/home/Home.vue'),
        children: [
            {
                path: 'overview',
                name: 'model-recorder',
                component: () => import('../components/modelCenter/ModelCenter.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: 'modeladd',
                name: 'model-add',
                component: () => import('../components/modelCenter/ModelAdd.vue'),
                meta: {
                    requestAuth: true,
                }
            }
        ],
    },
    {
        path: '/data',
        component: () => import('../components/home/Home.vue'),
        children: [
            {
                path: 'overview',
                name: 'data-recorder',
                component: () => import('../components/dataCenter/DataCenter.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: 'notification',
                name: 'target-notification',
                component: () => import('../components/home/Home.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            { // 专门刷新页面的路由
                path: 'refreshtarget',
                name: 'target-refreshtarget',
                component: () => import('../components/home/Home.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: 'refreshnotification',
                name: 'target-refreshnotification',
                component: () => import('../components/home/Home.vue'),
                meta: {
                    requestAuth: true,
                }
            }
        ]
    },
    {
        path: '/analyse',
        component: () => import('../components/home/Home.vue'),
        children: [
            {
                path: 'trans',
                name: 'analyse-trans',
                component: () => import('../components/componentCenter/LifeForecast.vue'),
                meta: {
                    requestAuth: true,
                }
            },
            {
                path: 'componentadd',
                name: 'component-add',
                component: () => import('../components/componentCenter/ComponentAdd.vue'),
                meta: {
                    requestAuth: true,
                }
            },
        ]
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../components/register/register.vue'),
        meta: {
            requestAuth: false,
        }
    },
    {
        path: '/resetPasswd',
        component: () => import('../components/home/Home.vue'),
        meta: {
            requestAuth: true,
        }
    },
    {
        path: '/personalCenter',
        name: 'personal-center',
        component: () => import('../pages/personalCenter/PersonalCenter.vue'),
        meta: {
            requestAuth: true,
        },
        children: [
            {
                path: '',
                name: 'myTasks',
                component: () => import('../pages/personalCenter/MyTasks.vue'),
                meta: {
                    requestAuth: true,
                }
            },
        ]
    },
]
const router = createRouter({
    routes: _routes,
    history: createWebHistory(),
})

router.beforeEach((to: any, _from: any, next: any) => {

    const jwt_token = localStorage.getItem("jwt_token");

    if (jwt_token) {
        store.commit("updateToken", jwt_token);
        store.dispatch("getInfo", {
            success() {
                next()
            },
        })
    } else {
        if(to.meta.requestAuth){
            //console.log(store.state.is_login)
            ElMessage.error('请先登录')
            next("/login")
        } else {
            next()
        }
    }
})

export default router
