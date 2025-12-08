import request from '@/utils/request'

// 类型定义
export interface UserRegisterParams {
  username: string
  password: string
  phone: string
}

export interface UserLoginParams {
  username: string
  password: string
}

export interface LoginResponse {
  userId: number
  username: string
  token: string
  role: string
}

export interface UserInfo {
  userId: number
  username: string
  phone: string
  role: string
  createTime: string
}

// API 方法
export const userApi = {
  /**
   * 用户注册
   * @param data 注册参数 (username, password, phone)
   * @returns 注册结果
   */
  register: (data: UserRegisterParams) => {
    return request.post('/user/register', data)
  },

  /**
   * 用户登录
   * @param data 登录参数 (username, password)
   * @returns 登录结果 {userId, username, token, role}
   */
  login: (data: UserLoginParams) => {
    return request.post('/user/login', data)
  },

  /**
   * 获取当前用户信息
   * @returns 用户信息
   */
  getCurrentUser: () => {
    return request.get('/user/info')
  },

  /**
   * 用户登出
   * @returns 登出结果
   */
  logout: () => {
    return request.post('/user/logout', {})
  },

  /**
   * 更新用户信息
   * @param data 要更新的用户信息
   * @returns 更新结果
   */
  updateUserInfo: (data: Partial<UserInfo>) => {
    return request.put('/user/info', data)
  },

  /**
   * 修改密码
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   * @returns 修改结果
   */
  changePassword: (oldPassword: string, newPassword: string) => {
    return request.post('/user/password', { oldPassword, newPassword })
  }
}
