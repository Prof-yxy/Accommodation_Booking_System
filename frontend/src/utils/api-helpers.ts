/**
 * API 相关的实用工具函数
 * 提供数据转换、验证、格式化等功能
 */

import type { EquipSelection, BookingCheckParams } from '@/api'
import { formatDate, daysBetween } from '@/types'

// ======================== 预订相关工具 ========================

/**
 * 构建预订检查参数
 * 方便在组件中创建参数对象
 */
export const buildBookingCheckParams = (
  typeId: number,
  checkIn: string,
  checkOut: string,
  equipments: EquipSelection[] = []
): BookingCheckParams => {
  return {
    typeId,
    checkIn,
    checkOut,
    equipments
  }
}

/**
 * 验证日期范围是否有效
 */
export const isValidDateRange = (checkIn: string, checkOut: string): boolean => {
  if (!checkIn || !checkOut) return false
  
  const start = new Date(checkIn)
  const end = new Date(checkOut)
  
  // 检出日期必须在检入日期之后
  return end > start
}

/**
 * 验证至少提前多少天预订
 */
export const isValidBookingAdvance = (checkIn: string, minDaysInAdvance: number = 0): boolean => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const checkInDate = new Date(checkIn)
  checkInDate.setHours(0, 0, 0, 0)
  
  const daysUntilCheckIn = Math.ceil((checkInDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  
  return daysUntilCheckIn >= minDaysInAdvance
}

/**
 * 计算预订的总天数
 */
export const calculateNights = (checkIn: string, checkOut: string): number => {
  return daysBetween(checkIn, checkOut)
}

/**
 * 检查日期是否冲突
 */
export const hasDateConflict = (
  checkIn: string,
  checkOut: string,
  existingCheckIn: string,
  existingCheckOut: string
): boolean => {
  const start1 = new Date(checkIn)
  const end1 = new Date(checkOut)
  const start2 = new Date(existingCheckIn)
  const end2 = new Date(existingCheckOut)
  
  // 检查时间段是否有交集
  return start1 < end2 && end1 > start2
}

// ======================== 装备相关工具 ========================

/**
 * 验证装备选择是否有效
 */
export const isValidEquipmentSelection = (equipments: EquipSelection[]): boolean => {
  if (!Array.isArray(equipments) || equipments.length === 0) {
    return true // 允许不选装备
  }
  
  return equipments.every(e => {
    return typeof e.equipId === 'number' && 
           e.equipId > 0 && 
           typeof e.count === 'number' && 
           e.count > 0
  })
}

/**
 * 获取选中装备的总数量
 */
export const getTotalEquipmentCount = (equipments: EquipSelection[]): number => {
  return equipments.reduce((total, e) => total + e.count, 0)
}

/**
 * 检查装备数量是否超过限制
 */
export const isEquipmentCountExceeded = (equipments: EquipSelection[], maxCount: number): boolean => {
  return getTotalEquipmentCount(equipments) > maxCount
}

// ======================== 价格相关工具 ========================

/**
 * 计算基础房价
 * 基础房价 = 房型基础价格 × 夜数
 */
export const calculateBasePrice = (
  basePrice: number,
  nights: number
): number => {
  return basePrice * nights
}

/**
 * 计算动态价格总额
 * 当存在浮动价格时，使用每日价格求和
 */
export const calculateDynamicPrice = (
  dailyPrices: Record<string, number>,
  checkIn: string,
  checkOut: string
): number => {
  const dates = getDatesBetween(checkIn, checkOut)
  
  let totalPrice = 0
  dates.forEach(date => {
    totalPrice += dailyPrices[date] || 0
  })
  
  return totalPrice
}

/**
 * 计算装备费用
 * 装备费用 = 单价 × 数量 × 夜数
 */
export const calculateEquipmentPrice = (
  unitPrice: number,
  quantity: number,
  nights: number
): number => {
  return unitPrice * quantity * nights
}

/**
 * 计算总价 (含服务费等)
 */
export const calculateTotalPrice = (
  basePrice: number,
  equipmentPrice: number = 0,
  serviceFeeRate: number = 0.1 // 服务费 10%
): number => {
  const subtotal = basePrice + equipmentPrice
  const serviceFee = subtotal * serviceFeeRate
  return Math.round((subtotal + serviceFee) * 100) / 100 // 保留两位小数
}

// ======================== 日期工具 ========================

/**
 * 获取两个日期之间的所有日期
 */
export const getDatesBetween = (startDate: string, endDate: string): string[] => {
  const dates: string[] = []
  const current = new Date(startDate)
  const end = new Date(endDate)
  
  while (current < end) {
    dates.push(formatDate(current))
    current.setDate(current.getDate() + 1)
  }
  
  return dates
}

/**
 * 检查日期是否为周末
 */
export const isWeekend = (date: string): boolean => {
  const d = new Date(date)
  const day = d.getDay()
  return day === 0 || day === 6
}

/**
 * 获取日期的季节
 */
export const getSeason = (date: string): 'spring' | 'summer' | 'autumn' | 'winter' => {
  const d = new Date(date)
  const month = d.getMonth() + 1
  
  if (month >= 3 && month <= 5) return 'spring'
  if (month >= 6 && month <= 8) return 'summer'
  if (month >= 9 && month <= 11) return 'autumn'
  return 'winter'
}

/**
 * 检查日期是否为节假日 (中国)
 */
export const isChineseHoliday = (date: string): boolean => {
  // 这里仅为示例，实际应该使用更完整的节假日库
  const d = new Date(date)
  const month = d.getMonth() + 1
  const day = d.getDate()
  
  // 春节、清明、端午、中秋、国庆等
  const holidays = [
    [1, 1],   // 元旦
    [2, 14],  // 情人节
    [3, 8],   // 妇女节
    [5, 1],   // 劳动节
    [10, 1],  // 国庆
    [12, 25]  // 圣诞节
  ]
  
  return holidays.some(([m, d_]) => m === month && d_ === day)
}

// ======================== 验证工具 ========================

/**
 * 验证电话号码格式
 */
export const isValidPhone = (phone: string): boolean => {
  // 中国手机号格式
  const pattern = /^1[3-9]\d{9}$/
  return pattern.test(phone)
}

/**
 * 验证身份证号格式
 */
export const isValidIdCard = (id: string): boolean => {
  // 简单的身份证号验证
  return id.length === 18 || id.length === 15
}

/**
 * 验证用户名格式
 */
export const isValidUsername = (username: string): boolean => {
  // 用户名长度 3-20，只能包含字母数字下划线
  return /^[a-zA-Z0-9_]{3,20}$/.test(username)
}

/**
 * 验证密码强度
 * 返回强度等级: 'weak' | 'medium' | 'strong'
 */
export const validatePasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  if (password.length < 6) return 'weak'
  
  let strength = 0
  
  if (/[a-z]/.test(password)) strength++  // 小写字母
  if (/[A-Z]/.test(password)) strength++  // 大写字母
  if (/\d/.test(password)) strength++     // 数字
  if (/[^a-zA-Z0-9]/.test(password)) strength++  // 特殊字符
  
  if (password.length >= 12) strength++
  
  if (strength >= 4) return 'strong'
  if (strength >= 2) return 'medium'
  return 'weak'
}

// ======================== 数据转换工具 ========================

/**
 * 转换装备选择为展示格式
 */
export const formatEquipmentDisplay = (
  equipments: EquipSelection[],
  equipmentMap: Record<number, { name: string; price: number }>
): string => {
  return equipments
    .map(e => `${equipmentMap[e.equipId]?.name || '未知装备'} × ${e.count}`)
    .join('，')
}

/**
 * 转换订单数据为导出格式 (CSV)
 */
export const convertBookingToCSV = (
  booking: any
): string => {
  const headers = ['订单ID', '用户ID', '房型', '营位', '检入', '检出', '总价', '状态']
  const values = [
    booking.bookingId,
    booking.userId,
    booking.typeId,
    booking.siteNo,
    booking.checkIn,
    booking.checkOut,
    booking.totalPrice,
    booking.status
  ]
  
  return [headers.join(','), values.join(',')].join('\n')
}

// ======================== 缓存工具 ========================

/**
 * 缓存管理器
 */
class CacheManager {
  private static instance: CacheManager
  private cache: Map<string, { value: any; expireTime: number }>
  
  private constructor() {
    this.cache = new Map()
  }
  
  static getInstance(): CacheManager {
    if (!CacheManager.instance) {
      CacheManager.instance = new CacheManager()
    }
    return CacheManager.instance
  }
  
  /**
   * 设置缓存
   * @param key 缓存键
   * @param value 缓存值
   * @param ttl 有效期 (秒)，默认 5 分钟
   */
  set(key: string, value: any, ttl: number = 300): void {
    this.cache.set(key, {
      value,
      expireTime: Date.now() + ttl * 1000
    })
  }
  
  /**
   * 获取缓存
   */
  get(key: string): any {
    const item = this.cache.get(key)
    
    if (!item) return null
    
    if (Date.now() > item.expireTime) {
      this.cache.delete(key)
      return null
    }
    
    return item.value
  }
  
  /**
   * 删除缓存
   */
  delete(key: string): void {
    this.cache.delete(key)
  }
  
  /**
   * 清空所有缓存
   */
  clear(): void {
    this.cache.clear()
  }
}

export const cacheManager = CacheManager.getInstance()
