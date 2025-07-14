<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { api } from '@/utils/api'

// 定义商家数据类型
interface Business {
  id: number
  businessName: string
  rating: string
  sales: string
  distance: string
  delivery: string
  imgLogo: string
  comment: string
  discountsList: string[]
}

const router = useRouter()
const shopList = ref<Business[]>([])

// 获取商家数据
const fetchBusinessList = async () => {
  console.log('🔍 [ShopListView] 开始获取商家列表...')
  try {
    console.log('📡 [ShopListView] 正在调用 API: api.business.getAll()')
    const response = await api.business.getAll()
    console.log('✅ [ShopListView] API响应成功:', response)
    console.log('📊 [ShopListView] 响应数据:', response.data)
    console.log('📈 [ShopListView] 商家数量:', response.data?.data?.length || 0)
    
    // 检查响应数据结构
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      shopList.value = response.data.data
      console.log('🎯 [ShopListView] 商家数据已更新:', shopList.value)
      
      // 打印每个商家的详细信息
      shopList.value.forEach((business, index) => {
        console.log(`🏪 [ShopListView] 商家 ${index + 1}:`, {
          id: business.id,
          name: business.businessName,
          rating: business.rating,
          sales: business.sales,
          distance: business.distance,
          delivery: business.delivery
        })
      })
    } else {
      console.warn('⚠️ [ShopListView] 响应数据格式不正确:', response.data)
      console.warn('⚠️ [ShopListView] 期望格式: {data: {data: [...]}}, 实际格式:', typeof response.data)
    }
  } catch (error: any) {
    console.error('❌ [ShopListView] 获取商家列表失败:', error)
    console.error('🚨 [ShopListView] 错误详情:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    })
  }
}

const toShopInfo = (id: number) => {
  router.push(`/shop/${id}`)
}

onMounted(() => {
  console.log('🚀 [ShopListView] 组件已挂载，开始初始化...')
  fetchBusinessList()
})
</script>

<template>
  <!-- 商家列表 -->
  <div class="shop-list">
    <div v-if="shopList.length === 0" class="empty-state">
      <p>🔍 正在加载商家数据...</p>
    </div>
    
    <router-link
      v-for="business in shopList"
      :key="business.id"
      :to="`/shop/${business.id}`"
      class="shop-item"
    >
        <img :src="business.imgLogo" :alt="business.businessName" class="shop-logo">
        <div class="shop-info">
          <h2>{{ business.businessName }}</h2>
          <div class="shop-rating">
            <span>{{ business.rating }}</span>
            <span>{{ business.sales }}</span>
          </div>
          <div class="shop-delivery">
            <span>{{ business.distance }}</span>
            <span>{{ business.delivery }}</span>
          </div>
        </div>
      </router-link>
    </div>
  </template>

<style scoped>
.shop-list {
  padding: 16px;
  background: #f5f5f5;
}

.header {
  margin-bottom: 16px;
}

.shop-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.shop-item {
  display: flex;
  padding: 16px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
}

.shop-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
}

.shop-info {
  flex: 1;
}

.shop-rating {
  margin: 8px 0;
  color: #666;
}

.shop-delivery {
  color: #999;
  font-size: 14px;
}
</style> 