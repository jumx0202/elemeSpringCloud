<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { api } from '@/utils/api'

interface FoodItem {
  id: number
  name: string
  text: string
  amount: string
  discountList: string[]
  redPrice: number
  grayPrice: string
  img: string
  foodName: string
}

interface ShopInfo {
  id: number
  businessName: string
  rating: string
  sales: string
  distance: string
  delivery: string
  comment: string
  discountsList: string[]
  foodList: FoodItem[]
  notice: string
  sidebarItemsList: string[] | string
  imgLogo: string
}

const route = useRoute()
const router = useRouter()
const shopId = route.params.id
const shopInfo = ref<ShopInfo | null>(null)
const selectedItems = ref<(FoodItem & { quantity: number })[]>([])
const totalPrice = ref(0)
const activeCategory = ref(0)
const sidebarItems = ref<string[]>([])
const showCartItems = ref(false)

const getShopInfo = async () => {
  console.log('🏪 [ShopInfoView] 开始获取商家详细信息, shopId:', shopId)
  try {
    console.log('📡 [ShopInfoView] 正在调用 API: api.business.getById')
    const response = await api.business.getById(Number(shopId))
    console.log('✅ [ShopInfoView] API响应成功:', response)
    console.log('📊 [ShopInfoView] 响应数据结构:', response.data)
    console.log('🔍 [ShopInfoView] response.data类型:', typeof response.data)
    console.log('🔍 [ShopInfoView] response.data内容:', JSON.stringify(response.data, null, 2))
    
    // 检查数据结构
    if (response.data && response.data.data) {
      console.log('📦 [ShopInfoView] 找到 response.data.data:', response.data.data)
      const data = response.data.data
      console.log('🏢 [ShopInfoView] 商家数据:', JSON.stringify(data, null, 2))
      
      if (data.foodList) {
        console.log('🍔 [ShopInfoView] 找到食物列表，长度:', data.foodList.length)
        console.log('🍔 [ShopInfoView] 食物列表详情:', data.foodList)
        
        data.foodList = data.foodList.map((food: any) => ({
          ...food,
          redPrice: parseFloat(food.redPrice || '0'),
          discountList: Array.isArray(food.discountList) ? food.discountList : []
        }))
      } else {
        console.warn('⚠️ [ShopInfoView] 没有找到foodList字段')
      }
      
      shopInfo.value = data
      console.log('🎯 [ShopInfoView] shopInfo已更新:', shopInfo.value)
    } else if (response.data) {
      console.log('📦 [ShopInfoView] 直接使用 response.data:', response.data)
      shopInfo.value = response.data
    } else {
      console.error('❌ [ShopInfoView] 响应数据结构异常:', response)
    }
    
    if (shopInfo.value) {
      const sidebarItemsStr = shopInfo.value.sidebarItemsList || ''
      console.log('📋 [ShopInfoView] 原始侧边栏字符串:', sidebarItemsStr)
      
      if (typeof sidebarItemsStr === 'string') {
        sidebarItems.value = sidebarItemsStr
          .split('-')
          .map((item: string) => item.trim())
          .filter((item: string) => item.length > 0)
      } else {
        sidebarItems.value = Array.isArray(sidebarItemsStr) ? sidebarItemsStr : []
      }
      
      console.log('📋 [ShopInfoView] 处理后的侧边栏数组:', sidebarItems.value)
    }
  } catch (error: any) {
    console.error('❌ [ShopInfoView] 获取商家信息失败:', error)
    console.error('🚨 [ShopInfoView] 错误详情:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    })
    sidebarItems.value = ['热销', '优惠', '套餐', '主食', '小吃', '饮品']
  }
}

const addToCart = (item: FoodItem) => {
  const existingItem = selectedItems.value.find(i => i.id === item.id)
  if (existingItem) {
    existingItem.quantity++
  } else {
    selectedItems.value.push({
      ...item,
      quantity: 1
    })
  }
  calculateTotal()
}

const removeFromCart = (item: FoodItem) => {
  const existingItem = selectedItems.value.find(i => i.id === item.id)
  if (existingItem) {
    if (existingItem.quantity > 1) {
      existingItem.quantity--
    } else {
      selectedItems.value = selectedItems.value.filter(i => i.id !== item.id)
    }
  }
  calculateTotal()
}

const calculateTotal = () => {
  totalPrice.value = selectedItems.value.reduce((total, item) => {
    return total + (item.redPrice * item.quantity)
  }, 0)
}

const toConfirm = () => {
  const orderDetail = {
    id: shopId,
    merchantData: {
      id: shopInfo.value.id,
      businessName: shopInfo.value.businessName,
      delivery: shopInfo.value.delivery,
      imgLogo: shopInfo.value.imgLogo
    },
    selectedItems: selectedItems.value,
    price: totalPrice.value
  }
  localStorage.setItem('orderDetail', JSON.stringify(orderDetail))
  router.push('/confirm-order')
}

const clearCart = () => {
  selectedItems.value = []
  calculateTotal()
  showCartItems.value = false
}

onMounted(() => {
  getShopInfo()
})
</script>

<template>
  <div class="shop-info" v-if="shopInfo">
    <div class="header-image">
      <img src="/img/mdl/head.webp" alt="header" class="head-img">
    </div>

    <div class="shop-card">
      <div class="shop-basic">
        <div class="shop-logo">
          <img :src="shopInfo.imgLogo" alt="shop logo">
        </div>
        <div class="shop-content">
          <h1>{{ shopInfo.businessName }}</h1>
          <div class="shop-stats">
            <span class="rating">{{ shopInfo.rating }}</span>
            <span class="sales">{{ shopInfo.sales }}</span>
            <span class="distance">{{ shopInfo.distance }}</span>
          </div>
          <div class="delivery-info">
            <span>{{ shopInfo.delivery }}</span>
          </div>
          <div class="notice" v-if="shopInfo.notice">
            <van-notice-bar :text="shopInfo.notice" scrollable />
          </div>
        </div>
      </div>
      <div class="discounts" v-if="shopInfo.discountsList?.length">
        <van-tag 
          v-for="(discount, index) in shopInfo.discountsList" 
          :key="index" 
          class="discount-tag"
          type="danger"
          plain>
          {{ discount }}
        </van-tag>
      </div>
    </div>

    <div class="menu-container">
      <div class="sidebar">
        <div v-for="(item, index) in sidebarItems" 
             :key="index"
             :class="['sidebar-item', { active: activeCategory === index }]"
             @click="activeCategory = index">
          {{ item }}
        </div>
      </div>

      <div class="food-list">
        <div v-for="item in shopInfo.foodList" 
             :key="item.id" 
             class="food-item">
          <img :src="item.img" :alt="item.name" class="food-img">
          <div class="food-info">
            <h3>{{ item.name }}</h3>
            <p class="food-desc">{{ item.text }}</p>
            <div class="food-sales">{{ item.amount }}</div>
            <div class="discount-list" v-if="item.discountList && item.discountList.length">
              <span v-for="(discount, index) in item.discountList" 
                    :key="index" 
                    class="discount-item">
                {{ discount }}
              </span>
            </div>
            <div class="price-row">
              <div class="price-info">
                <span class="current-price">￥{{ item.redPrice }}</span>
                <span class="original-price">{{ item.grayPrice }}</span>
              </div>
              <div class="action-buttons">
                <van-button 
                  v-if="selectedItems.find(i => i.id === item.id)"
                  size="mini" 
                  class="control-btn"
                  @click="removeFromCart(item)">-</van-button>
                <span v-if="selectedItems.find(i => i.id === item.id)">
                  {{ selectedItems.find(i => i.id === item.id)?.quantity }}
                </span>
                <van-button 
                  size="mini" 
                  class="add-btn"
                  @click="addToCart(item)">+</van-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="cart-panel">
      <div class="cart-info">
        <div class="cart-left" @click="showCartItems = !showCartItems">
          <div :class="['cart-icon', { active: selectedItems.length > 0 }]">
            <van-icon name="shopping-cart-o" size="24" />
            <div v-if="selectedItems.length > 0" class="cart-badge">
              {{ selectedItems.length }}
            </div>
          </div>
          <div class="total-price">
            <template v-if="selectedItems.length > 0">
              <span>总计:</span>
              <span class="price">￥{{ totalPrice.toFixed(2) }}</span>
            </template>
          </div>
        </div>
        <van-button 
          :class="['checkout-btn', { 'active': selectedItems.length > 0 }]"
          :disabled="selectedItems.length === 0"
          @click="toConfirm">
          {{ selectedItems.length > 0 ? '去结算' : shopInfo?.minOrder }}
        </van-button>
      </div>

      <van-popup
        v-model:show="showCartItems"
        position="bottom"
        round
        class="cart-popup">
        <div class="popup-header">
          <span>已选商品</span>
          <van-button size="small" plain @click="clearCart">清空购物车</van-button>
        </div>
        <div class="cart-items">
          <div v-for="item in selectedItems" :key="item.id" class="cart-item">
            <div class="cart-item-left">
              <img :src="item.img" :alt="item.name" class="cart-item-img">
              <div class="item-info">
                <span class="item-name">{{ item.name }}</span>
                <div class="item-desc">{{ item.text }}</div>
                <div class="discount-list" v-if="item.discountList && item.discountList.length">
                  <span v-for="(discount, index) in item.discountList" 
                        :key="index" 
                        class="discount-item">
                    {{ discount }}
                  </span>
                </div>
                <div class="item-amount">{{ item.amount }}</div>
                <span class="item-price">￥{{ item.redPrice }}</span>
                <span class="item-original-price">{{ item.grayPrice }}</span>
              </div>
            </div>
            <div class="item-controls">
              <van-button 
                size="mini" 
                class="control-btn"
                @click="removeFromCart(item)">-</van-button>
              <span class="quantity">{{ item.quantity }}</span>
              <van-button 
                size="mini" 
                class="add-btn"
                @click="addToCart(item)">+</van-button>
            </div>
          </div>
        </div>
      </van-popup>
    </div>
  </div>
</template>

<style scoped>
.shop-info {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 50px;
}

.header-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
}

.header-image .head-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-card {
  background: white;
  margin: -20px 12px 0;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: relative;
  z-index: 1;
}

.shop-basic {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.shop-logo {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.shop-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.shop-content {
  flex: 1;
}

.shop-basic h1 {
  font-size: 18px;
  margin-bottom: 8px;
}

.menu-container {
  display: flex;
  background: white;
  margin: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.sidebar {
  width: 85px;
  background: #f8f8f8;
  border-right: 1px solid #eee;
  padding: 0;
}

.sidebar-item {
  padding: 16px 8px;
  font-size: 13px;
  color: #333;
  text-align: center;
  position: relative;
  background: #f8f8f8;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.3;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sidebar-item:hover {
  background: #f0f0f0;
}

.sidebar-item.active {
  background: white;
  color: #0095ff;
  font-weight: bold;
  position: relative;
}

.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: #0095ff;
}

.food-list {
  flex: 1;
  padding: 12px 16px;
  background: white;
}

.food-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.food-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 12px;
  border-radius: 4px;
}

.food-info {
  flex: 1;
}

.food-info h3 {
  font-size: 16px;
  margin-bottom: 4px;
}

.food-desc {
  color: #999;
  font-size: 12px;
  margin: 4px 0;
  line-height: 1.4;
}

.food-sales {
  color: #999;
  font-size: 12px;
  margin: 4px 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.current-price {
  color: #ff4e00;
  font-size: 16px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  background-color: #e60012 !important;
  color: white !important;
  border: none !important;
  border-radius: 50% !important;
  width: 20px !important;
  height: 20px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 16px !important;
}

.control-btn {
  background-color: #f5f5f5 !important;
  color: #666 !important;
  border: none !important;
  border-radius: 50% !important;
  width: 20px !important;
  height: 20px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 16px !important;
}

.cart-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  border-radius: 16px 16px 0 0;
}

.cart-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-price {
  font-size: 16px;
  min-width: 100px;
}

.total-price .price {
  color: #ff4e00;
  font-weight: bold;
  margin-left: 4px;
}

.discounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0;
}

.discount-tag {
  font-size: 12px;
  padding: 4px 8px;
}

.checkout-btn {
  height: 40px !important;
  padding: 0 20px !important;
  border-radius: 20px !important;
  font-size: 16px !important;
  background-color: #cccccc !important;
  border: none !important;
  color: white !important;
}

.checkout-btn.active {
  background-color: #e60012 !important;
}

.checkout-btn:disabled {
  opacity: 1 !important;
}

.cart-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.cart-icon {
  position: relative;
  color: #999;
}

.cart-icon.active {
  color: #e60012;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #e60012;
  color: white;
  font-size: 12px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.cart-popup {
  max-height: 60vh;
  width: 100%;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  font-weight: bold;
}

.cart-items {
  padding: 16px;
  max-height: calc(60vh - 60px);
  overflow-y: auto;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.cart-item-left {
  display: flex;
  gap: 12px;
  flex: 1;
}

.cart-item-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 14px;
  font-weight: bold;
}

.item-desc {
  font-size: 12px;
  color: #666;
}

.item-amount {
  font-size: 12px;
  color: #999;
}

.item-price {
  color: #e60012;
  font-weight: bold;
  font-size: 16px;
}

.item-original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
  margin-left: 4px;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.quantity {
  min-width: 24px;
  text-align: center;
}

.discount-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin: 4px 0;
}

.discount-item {
  color: #ff6b00;
  font-size: 12px;
  padding: 2px 4px;
  border: 1px solid #ff6b00;
  border-radius: 2px;
  background-color: #fff7e8;
}
</style> 