import { reactive, readonly } from 'vue'

// 简单全局状态管理（不依赖 Pinia）
const storedUser = localStorage.getItem('user')
const state = reactive({
  user: storedUser ? JSON.parse(storedUser) : null as null | { userId: number; username: string; role?: string },
  token: localStorage.getItem('token') || null
})

export function setUser(user: { userId: number; username: string; role?: string } | null) {
  state.user = user
  if (!user) {
    localStorage.removeItem('user')
  } else {
    localStorage.setItem('user', JSON.stringify(user))
  }
}

export function setToken(token: string | null) {
  state.token = token
  if (token) localStorage.setItem('token', token)
  else localStorage.removeItem('token')
}

export function useStore() {
  return {
    state: readonly(state),
    setUser,
    setToken
  }
}

export default useStore()
