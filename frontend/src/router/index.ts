import { createRouter, createWebHashHistory } from 'vue-router'
import SiteList from '@/views/SiteList.vue'
import BookingConfirm from '@/views/BookingConfirm.vue'
import AdminDashboard from '@/views/AdminDashboard.vue'
import MyBookings from '@/views/MyBookings.vue'
import BookingDetail from '@/views/BookingDetail.vue'

const routes = [
  { path: '/', redirect: '/site-list' },
  { path: '/site-list', component: SiteList },
  { path: '/booking-confirm', component: BookingConfirm },
  { path: '/my-bookings', component: MyBookings },
  { path: '/booking/:id', component: BookingDetail },
  { path: '/admin', component: AdminDashboard }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
