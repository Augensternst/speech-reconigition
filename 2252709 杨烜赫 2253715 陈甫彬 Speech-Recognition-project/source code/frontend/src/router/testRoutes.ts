import { RouteRecordRaw } from 'vue-router';
const TestRoutes: RouteRecordRaw[] = [
  {
    path: '/TaskChallenge',
    name: 'TaskChallenge',
    meta: {
      title: '任务挑战',
      renderMenu: false,
      //icon: 'CreditCardOutlined',
    },
    component: () => import('@/pages/TaskChallenge/TaskChallenge.vue'),
    // children: [
    //   {
    //     path: '',
    //     redirect: '/TaskChallenge', // Modify this if needed
    //   },
    // ],
  },
];

export default TestRoutes;