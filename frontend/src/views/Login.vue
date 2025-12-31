<template>
  <div class="login-page">
    <div class="auth-card">
      <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input 
            v-model="form.username" 
            type="text" 
            required 
            placeholder="请输入用户名"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="form.password" 
            type="password" 
            required 
            placeholder="请输入密码"
          />
        </div>

        <div v-if="!isLogin" class="form-group">
          <label>手机号</label>
          <input 
            v-model="form.phone" 
            type="tel" 
            required 
            placeholder="请输入手机号"
          />
        </div>

        <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
        </button>
      </form>

      <div class="toggle-mode">
        <span v-if="isLogin">
          还没有账号？<a href="#" @click.prevent="toggleMode">去注册</a>
        </span>
        <span v-else>
          已有账号？<a href="#" @click.prevent="toggleMode">去登录</a>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { setUser, setToken } from '@/stores'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  phone: ''
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  errorMsg.value = ''
  form.username = ''
  form.password = ''
  form.phone = ''
}

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '用户名和密码不能为空'
    return
  }
  if (!isLogin.value && !form.phone) {
    errorMsg.value = '手机号不能为空'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    if (isLogin.value) {
      // 登录
      const res: any = await userApi.login({
        username: form.username,
        password: form.password
      })
      
      if (res.code === 1 || res.code === 200 || res.success) { // 兼容不同的后端返回格式
        const data = res.data
        // 保存状态
        setToken(data.token)
        setUser({
          userId: data.userId,
          username: data.username,
          role: data.role
        })
        
        // 跳转
        router.push('/')
      } else {
        errorMsg.value = res.msg || '登录失败'
      }
    } else {
      // 注册
      const res: any = await userApi.register({
        username: form.username,
        password: form.password,
        phone: form.phone
      })

      if (res.code === 1 || res.code === 200 || res.success) {
        alert('注册成功，请登录')
        toggleMode()
      } else {
        errorMsg.value = res.msg || '注册失败'
      }
    }
  } catch (err: any) {
    console.error(err)
    errorMsg.value = err.message || '请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  background-color: #f5f7fa;
}

.auth-card {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #409eff;
  outline: none;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}

.btn-submit:hover {
  background-color: #66b1ff;
}

.btn-submit:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.toggle-mode {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.toggle-mode a {
  color: #409eff;
  text-decoration: none;
}

.error-msg {
  color: #f56c6c;
  margin-bottom: 16px;
  font-size: 14px;
  text-align: center;
}
</style>
