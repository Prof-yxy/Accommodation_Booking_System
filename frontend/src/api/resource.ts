import request from '@/utils/request'

export const resourceApi = {
  // 获取房型列表
  getSiteTypes: () => {
    return request.get('/type/list');
  },

  // 获取价格日历
  getCalendar: (typeId: number, startDate: string, endDate: string) => {
    return request.get('/type/calendar', { params: { typeId, startDate, endDate } });
  },

  // 获取装备列表
  getEquipments: () => {
    return request.get('/equip/list');
  }
}