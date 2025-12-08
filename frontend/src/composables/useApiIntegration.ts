/**
 * API 集成示例
 * 展示如何在实际项目中整合使用所有 API 接口
 */

import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, bookingApi, resourceApi, adminApi } from '@/api'
import type { 
  SiteType, 
  Equipment, 
  BookingInfo,
  LoginResponse
} from '@/api'
import { 
  buildBookingCheckParams,
  isValidDateRange,
  calculateNights,
  isValidPhone
} from '@/utils/api-helpers'
import { formatDate, getBookingStatusText } from '@/types'

// ======================== 用户模块示例 ========================

export const useUserModule = () => {
  const currentUser = ref<LoginResponse | null>(null)
  const isLoggedIn = computed(() => !!currentUser.value)
  
  // 用户登录
  const login = async (username: string, password: string) => {
    try {
      const response = await userApi.login({ username, password })
      currentUser.value = response.data
      
      // 保存 token 到 localStorage
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userId', response.data.userId.toString())
      
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      ElMessage.error('登录失败')
      return false
    }
  }
  
  // 用户注册
  const register = async (username: string, password: string, phone: string) => {
    // 基本验证
    if (!isValidPhone(phone)) {
      ElMessage.error('请输入有效的电话号码')
      return false
    }
    
    try {
      await userApi.register({ username, password, phone })
      ElMessage.success('注册成功，请登录')
      return true
    } catch (error) {
      ElMessage.error('注册失败')
      return false
    }
  }
  
  // 用户登出
  const logout = async () => {
    try {
      await userApi.logout()
      currentUser.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      ElMessage.success('已登出')
      return true
    } catch (error) {
      ElMessage.error('登出失败')
      return false
    }
  }
  
  // 初始化用户信息
  const initializeUser = async () => {
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const response = await userApi.getCurrentUser()
        currentUser.value = response.data
      } catch (error) {
        localStorage.removeItem('token')
        currentUser.value = null
      }
    }
  }
  
  return {
    currentUser,
    isLoggedIn,
    login,
    register,
    logout,
    initializeUser
  }
}

// ======================== 资源模块示例 ========================

export const useResourceModule = () => {
  const siteTypes = ref<SiteType[]>([])
  const equipments = ref<Equipment[]>([])
  const loading = ref(false)
  
  // 加载房型列表
  const loadSiteTypes = async () => {
    loading.value = true
    try {
      const response = await resourceApi.getSiteTypes()
      siteTypes.value = response.data
    } catch (error) {
      ElMessage.error('加载房型列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 加载装备列表
  const loadEquipments = async () => {
    loading.value = true
    try {
      const response = await resourceApi.getEquipments()
      equipments.value = response.data
    } catch (error) {
      ElMessage.error('加载装备列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 获取房型名称
  const getSiteTypeName = (typeId: number): string => {
    const type = siteTypes.value.find(t => t.typeId === typeId)
    return type?.typeName || '未知房型'
  }
  
  // 获取装备名称和价格
  const getEquipmentInfo = (equipId: number) => {
    const equip = equipments.value.find(e => e.equipId === equipId)
    return equip ? { name: equip.equipName, price: equip.unitPrice } : null
  }
  
  // 初始化
  onMounted(() => {
    loadSiteTypes()
    loadEquipments()
  })
  
  return {
    siteTypes,
    equipments,
    loading,
    loadSiteTypes,
    loadEquipments,
    getSiteTypeName,
    getEquipmentInfo
  }
}

// ======================== 预订模块示例 ========================

export const useBookingModule = () => {
  const myBookings = ref<BookingInfo[]>([])
  const selectedBooking = ref<BookingInfo | null>(null)
  const bookingLoading = ref(false)
  
  // 获取我的订单
  const loadMyBookings = async (userId: number, status?: number) => {
    bookingLoading.value = true
    try {
      const response = await bookingApi.getMyList(userId, status)
      myBookings.value = response.data
    } catch (error) {
      ElMessage.error('加载订单列表失败')
    } finally {
      bookingLoading.value = false
    }
  }
  
  // 预订前检查
  const checkBookingAvailability = async (
    typeId: number,
    checkIn: string,
    checkOut: string,
    equipmentSelections: Record<number, number>
  ) => {
    // 验证日期
    if (!isValidDateRange(checkIn, checkOut)) {
      ElMessage.error('请选择有效的日期范围')
      return null
    }
    
    // 构建装备参数
    const equipments = Object.entries(equipmentSelections)
      .filter(([_, count]) => count > 0)
      .map(([equipId, count]) => ({
        equipId: parseInt(equipId),
        count
      }))
    
    try {
      const params = buildBookingCheckParams(typeId, checkIn, checkOut, equipments)
      const response = await bookingApi.check(params)
      
      if (response.data.isAvailable) {
        ElMessage.success('房间可预订')
        return response.data
      } else {
        ElMessage.warning(response.data.msg)
        return null
      }
    } catch (error) {
      ElMessage.error('检查可用性失败')
      return null
    }
  }
  
  // 创建订单
  const createBooking = async (
    userId: number,
    typeId: number,
    checkIn: string,
    checkOut: string,
    guestName: string,
    guestPhone: string,
    equipmentSelections: Record<number, number>
  ) => {
    // 验证输入
    if (!guestName.trim()) {
      ElMessage.error('请输入客人名称')
      return null
    }
    
    if (!isValidPhone(guestPhone)) {
      ElMessage.error('请输入有效的电话号码')
      return null
    }
    
    const equipments = Object.entries(equipmentSelections)
      .filter(([_, count]) => count > 0)
      .map(([equipId, count]) => ({
        equipId: parseInt(equipId),
        count
      }))
    
    try {
      const response = await bookingApi.create({
        userId,
        typeId,
        checkIn,
        checkOut,
        guestName,
        guestPhone,
        equipments
      })
      
      const { bookingId, siteNo, totalPrice } = response.data
      ElMessage.success(`订单创建成功！订单号: ${bookingId}`)
      
      return response.data
    } catch (error) {
      ElMessage.error('创建订单失败')
      return null
    }
  }
  
  // 支付订单
  const payBooking = async (bookingId: number) => {
    try {
      const response = await bookingApi.pay(bookingId)
      ElMessage.success('支付成功')
      
      // 刷新订单列表
      const userId = parseInt(localStorage.getItem('userId') || '0')
      if (userId) {
        await loadMyBookings(userId)
      }
      
      return response.data
    } catch (error) {
      ElMessage.error('支付失败')
      return null
    }
  }
  
  // 取消订单
  const cancelBooking = async (bookingId: number) => {
    try {
      const response = await bookingApi.cancel(bookingId)
      ElMessage.success('订单已取消')
      
      // 刷新订单列表
      const userId = parseInt(localStorage.getItem('userId') || '0')
      if (userId) {
        await loadMyBookings(userId)
      }
      
      return response.data
    } catch (error) {
      ElMessage.error('取消订单失败')
      return null
    }
  }
  
  // 获取订单详情
  const loadBookingDetail = async (bookingId: number) => {
    try {
      const response = await bookingApi.getDetail(bookingId)
      selectedBooking.value = response.data
      return response.data
    } catch (error) {
      ElMessage.error('获取订单详情失败')
      return null
    }
  }
  
  return {
    myBookings,
    selectedBooking,
    bookingLoading,
    loadMyBookings,
    checkBookingAvailability,
    createBooking,
    payBooking,
    cancelBooking,
    loadBookingDetail
  }
}

// ======================== 管理员模块示例 ========================

export const useAdminModule = () => {
  const revenueReport = ref<any>(null)
  const typeStats = ref<any[]>([])
  const adminLoading = ref(false)
  
  // 设置日价格
  const setDailyPrice = async (
    typeId: number,
    dates: string[],
    price: number
  ) => {
    try {
      const response = await adminApi.setDailyPrice({
        typeId,
        dates,
        price
      })
      ElMessage.success('价格设置成功')
      return response.data
    } catch (error) {
      ElMessage.error('价格设置失败')
      return null
    }
  }
  
  // 获取收入报表
  const loadRevenueReport = async (startDate: string, endDate: string) => {
    adminLoading.value = true
    try {
      const response = await adminApi.getDailyReport(startDate, endDate)
      revenueReport.value = response.data
      return response.data
    } catch (error) {
      ElMessage.error('获取报表失败')
      return null
    } finally {
      adminLoading.value = false
    }
  }
  
  // 获取房型统计
  const loadTypeStats = async () => {
    adminLoading.value = true
    try {
      const response = await adminApi.getTypeStats()
      typeStats.value = response.data
      return response.data
    } catch (error) {
      ElMessage.error('获取统计信息失败')
      return null
    } finally {
      adminLoading.value = false
    }
  }
  
  // 获取预订统计
  const loadBookingStats = async () => {
    try {
      const response = await adminApi.getBookingStats()
      return response.data
    } catch (error) {
      ElMessage.error('获取预订统计失败')
      return null
    }
  }
  
  return {
    revenueReport,
    typeStats,
    adminLoading,
    setDailyPrice,
    loadRevenueReport,
    loadTypeStats,
    loadBookingStats
  }
}

// ======================== 组合使用示例 ========================

/**
 * 完整的预订流程示例
 */
export const useCompleteBookingFlow = () => {
  const userModule = useUserModule()
  const resourceModule = useResourceModule()
  const bookingModule = useBookingModule()
  
  // 第一步：用户登录
  const step1_login = async (username: string, password: string) => {
    return await userModule.login(username, password)
  }
  
  // 第二步：加载资源信息
  const step2_loadResources = async () => {
    await resourceModule.loadSiteTypes()
    await resourceModule.loadEquipments()
  }
  
  // 第三步：检查可用性
  const step3_checkAvailability = async (
    typeId: number,
    checkIn: string,
    checkOut: string,
    equipmentSelections: Record<number, number>
  ) => {
    return await bookingModule.checkBookingAvailability(
      typeId,
      checkIn,
      checkOut,
      equipmentSelections
    )
  }
  
  // 第四步：创建订单
  const step4_createOrder = async (
    typeId: number,
    checkIn: string,
    checkOut: string,
    guestName: string,
    guestPhone: string,
    equipmentSelections: Record<number, number>
  ) => {
    const userId = parseInt(localStorage.getItem('userId') || '0')
    return await bookingModule.createBooking(
      userId,
      typeId,
      checkIn,
      checkOut,
      guestName,
      guestPhone,
      equipmentSelections
    )
  }
  
  // 第五步：支付订单
  const step5_pay = async (bookingId: number) => {
    return await bookingModule.payBooking(bookingId)
  }
  
  return {
    userModule,
    resourceModule,
    bookingModule,
    step1_login,
    step2_loadResources,
    step3_checkAvailability,
    step4_createOrder,
    step5_pay
  }
}
