import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', role: 'ADMIN' }
      },
      {
        path: 'part',
        name: 'Part',
        component: () => import('../views/part/PartList.vue'),
        meta: { title: '配件管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/part/CategoryList.vue'),
        meta: { title: '配件分类管理' }
      },
      {
        path: 'inbound',
        name: 'Inbound',
        component: () => import('../views/inbound/InboundList.vue'),
        meta: { title: '入库管理' }
      },
      {
        path: 'outbound',
        name: 'Outbound',
        component: () => import('../views/outbound/OutboundList.vue'),
        meta: { title: '出库管理' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('../views/stock/StockList.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'stock-check',
        name: 'StockCheck',
        component: () => import('../views/stock/StockCheckList.vue'),
        meta: { title: '库存盘点' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/UserProfile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('../views/supplier/SupplierList.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('../views/customer/CustomerList.vue'),
        meta: { title: '客户管理' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
