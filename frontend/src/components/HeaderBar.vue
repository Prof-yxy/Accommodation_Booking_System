<template>
  <header class="header-bar">
    <div class="brand">露营预订系统</div>
    <nav class="links">
      <router-link to="/site-list">房型</router-link>
      <router-link to="/booking-confirm">预订</router-link>
      <router-link to="/my-bookings">我的预订</router-link>
      <router-link to="/admin">管理</router-link>
    </nav>
  </header>
</template>

<script setup lang="ts">
// 轻量导航头部组件
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
</style>
