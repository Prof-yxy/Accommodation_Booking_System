import request from '@/utils/request'

// ======================== 类型定义 ========================

export interface PriceSetParams {
  typeId: number
  dates: string[] // 日期数组，格式: yyyy-MM-dd
  price: number
}

export interface DailyRevenue {
  date: string
  revenue: number
  bookingCount: number
  occupancyRate: number
}

export interface RevenueReport {
  startDate: string
  endDate: string
  totalRevenue: number
  totalBookings: number
  averageDailyRevenue: number
  dailyData: DailyRevenue[]
}

export interface OperationLog {
  logId: number
  operation: string
  operatorId: number
  operatorName?: string
  description: string
  logTime: string
  details?: Record<string, any>
}

export interface SiteInfo {
  siteId: number
  typeId: number
  siteNo: string
  status: number // 1: 正常, 0: 维护中
  createTime: string
  updateTime: string
}

export interface BookingStats {
  totalBookings: number
  paidBookings: number
  pendingPaymentBookings: number
  canceledBookings: number
  totalRevenue: number
}

export interface TypeStats {
  typeId: number
  typeName: string
  basePrice: number
  totalSites: number
  availableSites: number
  occupiedSites: number
  occupancyRate: number
  revenue: number
}
 
// 房型信息（用于增删改）
export interface SiteTypeEdit {
  typeId?: number // 新增时可不传
  typeName: string
  basePrice: number
  maxGuests: number
  description?: string
  imageUrl?: string
}

// 装备信息（用于增删改）
export interface EquipmentEdit {
  equipId?: number // 新增时可不传
  equipName: string
  unitPrice: number
  totalStock: number
  description?: string
  category?: string
}

// ======================== API 方法 ========================

export const adminApi = {
  /**
   * 设置日价格
   * 管理员可以为指定房型的特定日期设置价格
   * @param data 价格设置参数
   * @returns 设置结果
   */
  setDailyPrice: (data: PriceSetParams) => {
    return request.post('/admin/price/set', data)
  },

  /**
   * 新增房型
   */
  createSiteType: (data: SiteTypeEdit) => {
    return request.post('/admin/type', data)
  },

  /**
   * 修改房型
   */
  updateSiteType: (typeId: number, data: Partial<SiteTypeEdit>) => {
    return request.put(`/admin/type/${typeId}`, data)
  },

  /**
   * 删除房型
   */
  deleteSiteType: (typeId: number) => {
    return request.delete(`/admin/type/${typeId}`)
  },

  /**
   * 新增装备
   */
  createEquipment: (data: EquipmentEdit) => {
    return request.post('/admin/equipment', data)
  },

  /**
   * 修改装备
   */
  updateEquipment: (equipId: number, data: Partial<EquipmentEdit>) => {
    return request.put(`/admin/equipment/${equipId}`, data)
  },

  /**
   * 删除装备
   */
  deleteEquipment: (equipId: number) => {
    return request.delete(`/admin/equipment/${equipId}`)
  },

  /**
   * 批量设置日价格（日期范围）
   * @param typeId 房型ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param price 价格
   * @returns 批量设置结果
   */
  setDailyPricesBatch: (typeId: number, startDate: string, endDate: string, price: number) => {
    return request.post('/admin/price/batch', { typeId, startDate, endDate, price })
  },

  /**
   * 获取日收入报表
   * @param startDate 开始日期 (格式: yyyy-MM-dd)
   * @param endDate 结束日期 (格式: yyyy-MM-dd)
   * @returns 日收入报表数据
   */
  getDailyReport: (startDate: string, endDate: string) => {
    return request.get('/admin/report/daily', {
      params: { startDate, endDate }
    })
  },

  /**
   * 获取房型收入报表
   * @param startDate 开始日期 (格式: yyyy-MM-dd)
   * @param endDate 结束日期 (格式: yyyy-MM-dd)
   * @returns 房型收入报表
   */
  getTypeReport: (startDate: string, endDate: string) => {
    return request.get('/admin/report/type', {
      params: { startDate, endDate }
    })
  },

  /**
   * 获取预订统计信息
   * @returns 预订相关的统计数据
   */
  getBookingStats: () => {
    return request.get('/admin/stats/booking')
  },

  /**
   * 获取房型统计信息
   * @returns 各房型的统计数据
   */
  getTypeStats: () => {
    return request.get('/admin/stats/type')
  },

  /**
   * 获取操作日志
   * @param page 页码
   * @param pageSize 每页数量
   * @param operation 操作类型 (可选)
   * @returns 操作日志列表
   */
  getOperationLogs: (page: number = 1, pageSize: number = 20, operation?: string) => {
    const params: any = { page, pageSize }
    if (operation) {
      params.operation = operation
    }
    return request.get('/admin/logs/operation', { params })
  },

  /**
   * 获取所有营位信息
   * @param typeId 房型ID (可选，指定则返回该房型的营位)
   * @returns 营位列表
   */
  getAllSites: (typeId?: number) => {
    const params: any = {}
    if (typeId) {
      params.typeId = typeId
    }
    return request.get('/admin/sites', { params })
  },

  /**
   * 获取营位详情
   * @param siteId 营位ID
   * @returns 营位详细信息
   */
  getSiteDetail: (siteId: number) => {
    return request.get(`/admin/site/${siteId}`)
  },

  /**
   * 更新营位状态
   * @param siteId 营位ID
   * @param status 状态值 (1: 正常, 0: 维护中)
   * @returns 更新结果
   */
  updateSiteStatus: (siteId: number, status: number) => {
    return request.put(`/admin/site/${siteId}/status`, { status })
  },

  /**
   * 获取指定日期的占用情况
   * @param date 日期 (格式: yyyy-MM-dd)
   * @param typeId 房型ID (可选)
   * @returns 占用情况数据
   */
  getOccupancyByDate: (date: string, typeId?: number) => {
    const params: any = { date }
    if (typeId) {
      params.typeId = typeId
    }
    return request.get('/admin/occupancy/date', { params })
  },

  /**
   * 获取收益趋势数据
   * @param startDate 开始日期 (格式: yyyy-MM-dd)
   * @param endDate 结束日期 (格式: yyyy-MM-dd)
   * @returns 收益趋势数据
   */
  getRevenueTrend: (startDate: string, endDate: string) => {
    return request.get('/admin/revenue/trend', { params: { startDate, endDate } })
  },

  /**
   * 手动调整订单价格 (仅管理员)
   * @param bookingId 订单ID
   * @param newPrice 新价格
   * @param remark 调整备注
   * @returns 调整结果
   */
  adjustBookingPrice: (bookingId: number, newPrice: number, remark?: string) => {
    return request.put(`/admin/booking/${bookingId}/price`, {
      price: newPrice,
      remark
    })
  },

  /**
   * 查询用户行为记录
   * @param userId 用户ID (可选)
   * @param page 页码
   * @param pageSize 每页数量
   * @returns 用户操作记录
   */
  getUserBehaviorLog: (userId?: number, page: number = 1, pageSize: number = 20) => {
    const params: any = { page, pageSize }
    if (userId) {
      params.userId = userId
    }
    return request.get('/admin/logs/user-behavior', { params })
  }
}
