/**
 * 通用类型定义
 * 项目中共享的类型定义
 */

// ======================== 通用类型 ========================

/**
 * 统一 API 响应格式
 * @template T 数据类型
 */
export interface ApiResponse<T = any> {
  /** 状态码: 1 表示成功, 0 表示失败 */
  code: number
  /** 返回消息 */
  msg: string
  /** 返回数据 */
  data: T
}

/**
 * 分页请求参数
 */
export interface PaginationParams {
  page: number
  pageSize: number
}

/**
 * 分页响应数据
 * @template T 列表项类型
 */
export interface PaginationResponse<T = any> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

/**
 * 时间范围查询参数
 */
export interface DateRangeParams {
  startDate: string
  endDate: string
}

// ======================== 业务常量 ========================

/**
 * 预订订单状态
 */
export enum BookingStatus {
  /** 待支付 */
  PENDING = 0,
  /** 已支付 */
  PAID = 1,
  /** 已取消 */
  CANCELED = 2
}

/**
 * 营位状态
 */
export enum SiteStatus {
  /** 正常 */
  NORMAL = 1,
  /** 维护中 */
  MAINTENANCE = 0
}

/**
 * 操作类型
 */
export enum OperationType {
  NEW_ORDER = 'NEW_ORDER',
  CANCEL_ORDER = 'CANCEL_ORDER',
  PAY_ORDER = 'PAY_ORDER',
  SET_PRICE = 'SET_PRICE',
  UPDATE_SITE = 'UPDATE_SITE'
}

/**
 * 用户角色
 */
export enum UserRole {
  /** 普通用户 */
  USER = 'USER',
  /** 管理员 */
  ADMIN = 'ADMIN'
}

// ======================== 日期工具 ========================

/**
 * 格式化日期为 yyyy-MM-dd
 */
export const formatDate = (date: Date | string): string => {
  const d = new Date(date)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${month}-${day}`
}

/**
 * 获取日期范围内的所有日期
 */
export const getDateRange = (startDate: string, endDate: string): string[] => {
  const dates: string[] = []
  const current = new Date(startDate)
  const end = new Date(endDate)
  
  while (current <= end) {
    dates.push(formatDate(current))
    current.setDate(current.getDate() + 1)
  }
  
  return dates
}

/**
 * 计算两个日期之间的天数
 */
export const daysBetween = (startDate: string, endDate: string): number => {
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diffTime = Math.abs(end.getTime() - start.getTime())
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

// ======================== 金额相关 ========================

/**
 * 格式化金额为人民币
 */
export const formatCurrency = (amount: number): string => {
  return `¥${amount.toFixed(2)}`
}

/**
 * 从字符串解析金额 (移除 ¥ 符号)
 */
export const parseCurrency = (str: string): number => {
  return parseFloat(str.replace('¥', '').trim())
}

// ======================== 订单相关 ========================

/**
 * 获取订单状态的中文描述
 */
export const getBookingStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取订单状态的样式类
 */
export const getBookingStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    0: 'status-pending',
    1: 'status-paid',
    2: 'status-canceled'
  }
  return classMap[status] || ''
}
