import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

// API基础配置
const API_BASE_URL = 'http://localhost:8888' // 网关地址

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    console.log('📤 [API] 发送请求:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      baseURL: config.baseURL,
      fullURL: `${config.baseURL}${config.url}`,
      data: config.data,
      params: config.params,
      headers: config.headers
    })
    
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
      console.log('🔐 [API] 已添加认证token')
    }
    return config
  },
  (error) => {
    console.error('❌ [API] 请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    console.log('📥 [API] 收到响应:', {
      status: response.status,
      statusText: response.statusText,
      url: response.config.url,
      data: response.data,
      headers: response.headers
    })
    return response
  },
  (error) => {
    console.error('❌ [API] 响应错误:', error)
    console.error('🚨 [API] 错误详情:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      data: error.response?.data,
      headers: error.response?.headers
    })
    
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器内部错误')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    
    return Promise.reject(error)
  }
)

// API接口封装
export const api = {
  // 用户相关
  user: {
    login: (data: any) => apiClient.post('/api/user/login', data),
    register: (data: any) => apiClient.post('/api/user/register', data),
    sendVerifyCode: (data: any) => apiClient.post('/api/user/sendVerifyCode', data),
    getUserInfo: () => apiClient.get('/api/user/info')
  },
  
  // 商家相关
  business: {
    getAll: () => {
      console.log('🏪 [API] 准备获取所有商家数据...')
      return apiClient.post('/api/business/getAll')
    },
    getById: (id: number) => {
      console.log('🏪 [API] 准备获取商家详情，ID:', id)
      return apiClient.post('/api/business/getBusinessById', { ID: id })
    },
    search: (keyword: string) => {
      console.log('🏪 [API] 准备搜索商家，关键词:', keyword)
      return apiClient.get(`/api/business/search?keyword=${keyword}`)
    }
  },
  
  // 食物相关
  food: {
    getById: (id: number) => apiClient.post('/api/food/getFoodById', { ID: id }),
    getByBusinessId: (businessId: number) => apiClient.get(`/api/food/business/${businessId}`)
  },
  
  // 订单相关
  order: {
    create: (data: any) => apiClient.post('/api/order/addUserOrder', data),
    getUserOrders: (userPhone: string) => apiClient.post('/api/order/getAllUserOrder', { userPhone }),
    getUserOrderDetails: (userPhone: string) => apiClient.post('/api/order/getAllUserOrderDetails', { userPhone }),
    getOrderTime: (orderId: number) => apiClient.get(`/api/order/${orderId}/time`),
    markPaid: (orderId: number) => apiClient.post('/api/order/havePayed', { ID: orderId })
  },
  
  // 验证码相关
  captcha: {
    generate: (type: string = 'IMAGE') => apiClient.post('/api/captcha/generate', { type }),
    validate: (data: any) => apiClient.post('/api/captcha/validate', data)
  },
  
  // 通知相关
  notification: {
    sendEmail: (data: any) => apiClient.post('/api/notification/send-email', data),
    sendSms: (data: any) => apiClient.post('/api/notification/send-sms', data)
  }
}

export default apiClient 