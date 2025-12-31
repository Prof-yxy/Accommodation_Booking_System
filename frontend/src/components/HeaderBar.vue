<template>
  <header class="header-bar">
    <div class="brand">露营预订系统</div>
    <nav class="links">
      <router-link to="/site-list">房型</router-link>
      <router-link to="/booking-confirm" v-if="isUser">预订</router-link>
      <router-link to="/my-bookings" v-if="isUser">我的预订</router-link>
      <router-link to="/admin" v-if="isAdmin">管理</router-link>
      
      <span v-if="user" class="user-info">
        欢迎, {{ user.username }}
        <a href="#" @click.prevent="logout" class="logout-link">退出</a>
      </span>
      <router-link v-else to="/login">登录/注册</router-link>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import store from '@/stores'

const router = useRouter()
const user = computed(() => store.state.user)
const isAdmin = computed(() => user.value?.role === 'admin')
const isUser = computed(() => user.value?.role === 'user')

const logout = () => {
  store.setUser(null)
  store.setToken(null)
  router.push('/login')
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #1976d2;
  color: #fff;
}
.header-bar .brand {
  font-weight: 600;
}
.header-bar .links a {
  color: #fff;
  margin-left: 12px;
  text-decoration: none;
}
.user-info {
  margin-left: 12px;
  font-size: 14px;
}
.logout-link {
  margin-left: 8px;
  font-size: 12px;
  opacity: 0.8;
}
.logout-link:hover {
  opacity: 1;
  text-decoration: underline;
}
</style>
