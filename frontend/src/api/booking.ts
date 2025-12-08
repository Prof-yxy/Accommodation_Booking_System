import request from '@/utils/request' // 假设已封装 axios

// 类型定义
export interface EquipSelection {
  equipId: number;
  count: number;
}

export interface BookingCheckParams {
  typeId: number;
  checkIn: string;
  checkOut: string;
  equipments: EquipSelection[];
}

export interface BookingCreateParams extends BookingCheckParams {
  userId: number;
  guestName: string;
  guestPhone: string;
}

// API 方法
export const bookingApi = {
  // 预订前检查
  check: (data: BookingCheckParams) => {
    return request.post('/booking/check', data);
  },

  // 提交订单
  create: (data: BookingCreateParams) => {
    return request.post('/booking/create', data);
  },

  // 支付
  pay: (bookingId: number) => {
    return request.post('/booking/pay', { bookingId });
  },

  // 获取我的订单
  getMyList: (userId: number) => {
    return request.get('/booking/my', { params: { userId } });
  },

  // 取消订单
  cancel: (bookingId: number) => {
    return request.post('/booking/cancel', { bookingId });
  }
}