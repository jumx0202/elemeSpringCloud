<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { api } from '@/utils/api'
import TabBar from '@/components/TabBar.vue'

interface Order {
  id: number
  businessID: number
  state: number
  price: number
  orderList: string
  userPhone: string
  createdAt: string
}

// 添加商家信息接口
interface Business {
  id: number
  businessName: string
  address: string
  phone: string
  rating: string
  imgLogo: string
  description: string
  discounts: string
  minOrder: string
  delivery: string
  sales: string
  notice: string
  type: string
}

// 扩展订单显示接口
interface OrderDisplay extends Order {
  businessName?: string
  imgLogo?: string
  orderItems?: OrderItem[]
  amount?: number
}

interface OrderItem {
  id: number
  name: string
  img: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const orders = ref<OrderDisplay[]>([])
const activeTab = ref('all')
const allOrders = ref<OrderDisplay[]>([])  // 存储所有订单数据
const loading = ref(false)  // 加载状态

const toShop = (id: number) => {
  router.push(`/shop/${id}`)
}

// 添加tab切换函数
const switchTab = (tabType: string) => {
  activeTab.value = tabType
  filterOrders()
}

// 添加订单过滤函数
const filterOrders = () => {
  const filteredOrders = allOrders.value.filter((order: OrderDisplay) => {
    switch (activeTab.value) {
      case 'ongoing':
        return order.state === 0  // 未支付
      case 'pending':
        return order.state === 1  // 已支付待评价
      case 'refund':
        return order.state === 2  // 退款
      default:
        return true  // 全部
    }
  })
  orders.value = filteredOrders
  console.log('Filtered orders for tab:', activeTab.value, filteredOrders)
}

// 修复订单状态显示函数
const getOrderStatusText = (state: number) => {
  switch (state) {
    case 0:
      return '未支付'
    case 1:
      return '已支付'
    case 2:
      return '退款中'
    case 3:
      return '已完成'
    default:
      return '未知状态'
  }
}

const getOrderItems = async (orderList: string) => {
  const itemIds = orderList.split('-')
  const items = await Promise.all(
    itemIds.map(async (id) => {
      try {
        const response = await api.food.getById(Number(id))
        return response.data
      } catch (error) {
        console.error(`Failed to fetch food item ${id}:`, error)
        return null
      }
    })
  )
  return items.filter(item => item !== null)
}

const getBusinessInfo = async (businessID: number) => {
  try {
    console.log('Fetching business info for ID:', businessID)
    const response = await api.business.getById(businessID)
    console.log('Full business response:', response)
    console.log('Business response data:', response.data)
    console.log('Business response data type:', typeof response.data)
    if (response.data && response.data.data) {
      console.log('Business data from response:', response.data.data)
      return response.data.data
    } else if (response.data) {
      console.log('Direct business data:', response.data)
      return response.data
    }
    throw new Error('Invalid business data structure')
  } catch (error) {
    console.error(`Failed to fetch business info for ID ${businessID}:`, error)
    return null
  }
}

onMounted(async () => {
  const type = route.query.type as string
  if (type) {
    activeTab.value = type
  }

  try {
    loading.value = true
    
    if (!userStore.userInfo) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const res = await api.order.getUserOrderDetails(userStore.userInfo.phoneNumber)

    console.log('Orders response:', res.data)
    console.log('Orders response structure:', JSON.stringify(res.data, null, 2))

    // 检查API返回的数据结构
    let ordersData: OrderDisplay[] = []
    if (res.data && res.data.data && Array.isArray(res.data.data)) {
      ordersData = res.data.data
    } else if (res.data && Array.isArray(res.data)) {
      ordersData = res.data
    } else {
      console.error('Unexpected API response structure:', res.data)
      ElMessage.error('订单数据格式异常')
      return
    }

    console.log('Filtered orders data:', ordersData)
    console.log('Orders data length:', ordersData.length)

    // 处理订单数据，从business对象中提取字段到订单顶层
    const processedOrders = ordersData.map((order: any) => {
      const processedOrder: OrderDisplay = {
        id: order.id,
        businessID: order.businessID,
        userPhone: order.userPhone,
        orderList: order.orderList,
        price: order.price,
        state: order.state,
        createdAt: order.createdAt,
        // 从business对象中提取信息
        businessName: order.business?.businessName || '未知商家',
        imgLogo: order.business?.imgLogo || '/img/default-shop.png',
        // 从orderItems中提取信息
        orderItems: order.orderItems || [],
        amount: order.orderItems ? order.orderItems.length : (order.orderList ? order.orderList.split('-').length : 0)
      }
      return processedOrder
    })

    console.log('Processed orders:', processedOrders)
    allOrders.value = processedOrders
    filterOrders()  // 应用当前tab过滤
  } catch (error) {
    console.error("Failed to fetch user orders", error)
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="wrapper3">
    <div class="header">
      <div class="selector">
        <div class="item1" @click="switchTab('all')">
          全部
          <div :class="activeTab === 'all' ? 'selected' : 'not-selected'"></div>
        </div>
        <div class="item1" @click="switchTab('ongoing')">
          进行中
          <div :class="activeTab === 'ongoing' ? 'selected' : 'not-selected'"></div>
        </div>
        <div class="item1" @click="switchTab('pending')">
          待评价
          <div :class="activeTab === 'pending' ? 'selected' : 'not-selected'"></div>
        </div>
        <div class="item1" @click="switchTab('refund')">
          退款
          <div :class="activeTab === 'refund' ? 'selected' : 'not-selected'"></div>
        </div>
      </div>
    </div>

    <div class="order-list">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载订单...</p>
      </div>
      <div v-else-if="orders.length === 0" class="empty-state">
        <img src="/img/message/empty.png" alt="empty" class="empty-img" />
        <p>暂无订单信息</p>
      </div>
      <div v-for="(item, index) in orders" :key="index" class="order-card">
        <div class="shop-header">
          <img :src="item.imgLogo || '/img/default-shop.png'" alt="shop image" class="shop-img"/>
          <div class="shop-info">
            <div class="business-name">{{ item.businessName || '未知商家' }} ></div>
            <div class="order-status">{{ getOrderStatusText(item.state) }}</div>
          </div>
        </div>

        <div class="order-items">
          <div class="items-container">
            <template v-if="item.orderItems && item.orderItems.length === 1">
              <img :src="item.orderItems[0].img" class="food-img" />
              <span class="food-name">{{ item.orderItems[0].name }}</span>
            </template>
            <template v-else-if="item.orderItems && item.orderItems.length > 1">
              <div class="multi-items">
                <img 
                  v-for="(food, idx) in item.orderItems" 
                  :key="idx"
                  :src="food.img" 
                  class="food-img"
                />
              </div>
            </template>
            <template v-else>
              <div class="no-items">暂无商品信息</div>
            </template>
          </div>
          <div class="order-info">
            <div class="price">¥{{ item.price.toFixed(2) }}</div>
            <div class="count">共{{ item.amount || 0 }}件</div>
          </div>
        </div>

        <div class="order-time">{{ new Date(item.createdAt).toLocaleString() }}</div>
        <div class="action-buttons">
          <button class="similar-shop">相似商家</button>
          <button class="reorder-btn" @click="toShop(item.businessID)">再来一单</button>
        </div>
      </div>
    </div>

    <div class="tip">仅显示最近一年的订单</div>
    <TabBar />
  </div>
</template>

<style scoped>
.wrapper3 {
  min-height: 100vh;
  width: 100%;
  background: #F5F5F5;
  padding-bottom: 60px;
}

.header {
  background: white;
  padding: 15px;
}

.selector {
  display: flex;
  justify-content: space-around;
  border-bottom: 1px solid #f5f5f5;
}

.item1 {
  position: relative;
  padding: 12px 0;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.item1:hover {
  color: #0095ff;
}

.selected {
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #0095ff;
}

.not-selected {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: transparent;
}

.order-card {
  background: white;
  margin-bottom: 10px;
  padding: 16px;
}

.shop-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.shop-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
}

.shop-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.business-name {
  font-size: 16px;
  font-weight: 500;
}

.order-status {
  color: #666;
  font-size: 14px;
}

.order-details {
  margin: 12px 0;
}

.price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.count {
  color: #999;
}

.order-time {
  color: #999;
  font-size: 12px;
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.similar-shop,
.reorder-btn {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 14px;
  cursor: pointer;
}

.similar-shop {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.reorder-btn {
  background: #0095ff;
  color: white;
  border: none;
}

.tip {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 15px 0;
}

.order-items {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0;
  padding: 12px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.items-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.food-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.food-name {
  font-size: 14px;
  color: #333;
}

.multi-items {
  display: flex;
  gap: 8px;
}

.order-info {
  text-align: right;
}

.price {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.count {
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-img {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
}

.no-items {
  color: #999;
  font-size: 14px;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #0095ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 