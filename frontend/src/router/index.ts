import { createRouter, createWebHashHistory } from 'vue-router'
import SiteList from '@/views/SiteList.vue'
import BookingConfirm from '@/views/BookingConfirm.vue'
import AdminDashboard from '@/views/AdminDashboard.vue'
import MyBookings from '@/views/MyBookings.vue'
import BookingDetail from '@/views/BookingDetail.vue'
import Login from '@/views/Login.vue'
import store from '@/stores'

const routes = [
  { path: '/', redirect: '/site-list' },
  { path: '/site-list', component: SiteList },
  { path: '/booking-confirm', component: BookingConfirm },
  { path: '/admin', component: AdminDashboard },
  { path: '/my-bookings', component: MyBookings},
  { path: '/booking/:id', component: BookingDetail },
  { path: '/login', component: Login }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = store.state.user
  const role = user?.role

  // 未登录用户访问需要权限的页面 -> 跳转登录
  const authRequired = ['/admin', '/my-bookings', '/booking-confirm']
  if (!user && authRequired.includes(to.path)) {
    next('/login')
    return
  }

  // Admin 角色限制：不能访问预订相关页面
  if (role === 'admin') {
    const adminForbidden = ['/booking-confirm', '/my-bookings']
    if (adminForbidden.includes(to.path)) {
      next('/')
      return
    }
  }

  // User 角色限制：不能访问管理页面
  if (role === 'user') {
    if (to.path === '/admin') {
      next('/')
      return
    }
  }

  next()
})

export default router
