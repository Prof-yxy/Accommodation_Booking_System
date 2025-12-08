/**
 * API 统一导出模块
 * 提供所有前端与后端通信的接口定义
 */

export * from './user'
export * from './booking'
export * from './resource'
export * from './admin'

// 为了更方便的使用，也可以按需导入
export { userApi } from './user'
export { bookingApi } from './booking'
export { resourceApi } from './resource'
export { adminApi } from './admin'
