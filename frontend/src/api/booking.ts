import request from '@/utils/request'

// ======================== 类型定义 ========================

export interface EquipSelection {
  equipId: number
  count: number
}

export interface BookingCheckParams {
  typeId: number
  checkIn: string
  checkOut: string
  equipments: EquipSelection[]
}

export interface BookingCreateParams extends BookingCheckParams {
  userId: number
  guestName: string
  guestPhone: string
}

export interface BookingCheckResponse {
  isAvailable: boolean
  msg: string
  totalPrice: number
  priceDetail?: {
    basePrice: number
    dailyPrices: Array<{ date: string; price: number }>
    equipmentPrice: number
    nights: number
  }
}

export interface BookingCreateResponse {
  bookingId: number
  siteNo: string
  totalPrice: number
  status: number
}

export interface BookingInfo {
  bookingId: number
  userId: number
  typeId: number
  siteNo: string
  checkIn: string
  checkOut: string
  guestName: string
  guestPhone: string
  totalPrice: number
  status: number // 0: 待支付, 1: 已支付, 2: 已取消
  createTime: string
  updateTime: string
  equipments?: Array<{
    equipId: number
    equipName: string
    count: number
    price: number
  }>
}

export interface PayParams {
  bookingId: number
}

// ======================== API 方法 ========================

export const bookingApi = {
  /**
   * 预订前检查 - 验证库存和计算价格
   * @param data 检查参数
   * @returns 检查结果 {isAvailable, msg, totalPrice, priceDetail}
   */
  check: (data: BookingCheckParams) => {
    return request.post('/booking/check', data)
  },

  /**
   * 创建订单 - 核心预订接口
   * 包含事务控制：校验库存 -> 计算价格 -> 分配营位 -> 保存订单
   * @param data 创建订单参数
   * @returns 订单结果 {bookingId, siteNo, totalPrice}
   */
  create: (data: BookingCreateParams) => {
    return request.post('/booking/create', data)
  },

  /**
   * 支付订单
   * @param bookingId 订单ID
   * @returns 支付结果
   */
  pay: (bookingId: number) => {
    return request.post('/booking/pay', { bookingId })
  },

  /**
   * 获取我的订单列表
   * @param userId 用户ID
   * @param status 订单状态 (可选, 0: 待支付, 1: 已支付, 2: 已取消)
   * @returns 订单列表
   */
  getMyList: (userId: number, status?: number) => {
    const params: any = { userId }
    if (status !== undefined) {
      params.status = status
    }
    return request.get('/booking/my', { params })
  },

  /**
   * 获取订单详情
   * @param bookingId 订单ID
   * @returns 订单详细信息
   */
  getDetail: (bookingId: number) => {
    return request.get(`/booking/${bookingId}`)
  },

  /**
   * 取消订单 - 自动释放装备和营位库存
   * @param bookingId 订单ID
   * @returns 取消结果
   */
  cancel: (bookingId: number) => {
    return request.post('/booking/cancel', { bookingId })
  },

  /**
   * 获取订单的装备列表
   * @param bookingId 订单ID
   * @returns 装备列表
   */
  getEquipments: (bookingId: number) => {
    return request.get(`/booking/${bookingId}/equipments`)
  }
}