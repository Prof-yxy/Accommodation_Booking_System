import request from '@/utils/request'

// ======================== 类型定义 ========================

export interface SiteType {
  typeId: number
  typeName: string
  basePrice: number
  maxGuests: number
  description?: string
  imageUrl?: string
}

export interface DailyPrice {
  specificDate: string
  typeId: number
  price: number
  remark?: string
}

export interface Equipment {
  equipId: number
  equipName: string
  unitPrice: number
  totalStock: number
  availableStock?: number
  description?: string
  category?: string
}

export interface CalendarItem {
  date: string
  price: number
  available: boolean
  stock: number
}

export interface PriceCalendarResponse {
  typeId: number
  typeName: string
  basePrice: number
  calendarData: CalendarItem[]
}

// ======================== API 方法 ========================

export const resourceApi = {
  /**
   * 获取所有房型列表
   * @returns 房型列表
   */
  getSiteTypes: () => {
    return request.get('/type/list')
  },

  /**
   * 获取指定房型的详细信息
   * @param typeId 房型ID
   * @returns 房型详情
   */
  getSiteTypeDetail: (typeId: number) => {
    return request.get(`/type/${typeId}`)
  },

  /**
   * 获取价格日历
   * 返回指定日期范围内，每天的价格和库存情况
   * @param typeId 房型ID
   * @param startDate 开始日期 (格式: yyyy-MM-dd)
   * @param endDate 结束日期 (格式: yyyy-MM-dd)
   * @returns 价格日历数据
   */
  getCalendar: (typeId: number, startDate: string, endDate: string) => {
    return request.get('/type/calendar', {
      params: {
        typeId,
        startDate,
        endDate
      }
    })
  },

  /**
   * 获取指定日期范围内的日价
   * @param typeId 房型ID
   * @param dates 日期数组 (格式: yyyy-MM-dd)
   * @returns 日价信息
   */
  getDailyPrices: (typeId: number, dates: string[]) => {
    return request.post('/type/prices', {
      typeId,
      dates
    })
  },

  /**
   * 获取所有装备列表
   * @returns 装备列表
   */
  getEquipments: () => {
    return request.get('/equip/list')
  },

  /**
   * 获取装备详情
   * @param equipId 装备ID
   * @returns 装备详细信息
   */
  getEquipmentDetail: (equipId: number) => {
    return request.get(`/equip/${equipId}`)
  },

  /**
   * 获取装备库存信息
   * @param equipIds 装备ID数组
   * @returns 装备库存信息
   */
  getEquipmentStock: (equipIds: number[]) => {
    return request.post('/equip/stock', { equipIds })
  },

  /**
   * 获取装备分类列表
   * @returns 分类列表
   */
  getEquipmentCategories: () => {
    return request.get('/equip/categories')
  },

  /**
   * 根据分类获取装备列表
   * @param category 分类名称
   * @returns 该分类下的装备列表
   */
  getEquipmentsByCategory: (category: string) => {
    return request.get(`/equip/category/${category}`)
  }
}