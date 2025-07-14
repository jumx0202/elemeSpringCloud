<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { vant } from 'vant'
import {
  Wap_home as WapHome,
  Apps_o as AppsO,
  Envelop_o as EnvelopO,
  Shopping_cart_o as ShoppingCartO,
  Manager_o as ManagerO,
  Exchange as ExchangeO,
  Location_o as LocationO,
  Arrow_down as ArrowDown
} from '@vant/icons'
import 'vant/lib/index.css';
import TabBar from '@/components/TabBar.vue'
import axios from 'axios'
import { api } from '@/utils/api'

// 定义基础URL
const OSS_URL = 'https://org-elemenew.oss-cn-beijing.aliyuncs.com/homepage'

// 定义商家数据类型
interface Business {
  id: number
  businessName: string
  rating: string          // 改为string，因为API返回 "4.8"
  sales: string          // 改为string，因为API返回 "月售4500+"
  distance: string       // 改为string，因为API返回 "1.8km 约35分钟"
  minOrder: string       // 改为string，因为API返回 "起送￥30"
  delivery: string       // 改为string，因为API返回 "配送费￥6"
  imgLogo: string
  comment: string
  discountsList: string[]
}

const businesses = ref<Business[]>([])

// 获取商家数据
const fetchBusinessList = async () => {
  console.log('🔍 [HomeView] 开始获取商家列表...')
  try {
    console.log('📡 [HomeView] 正在调用 API: api.business.getAll()')
    const response = await api.business.getAll()
    console.log('✅ [HomeView] API响应成功:', response)
    console.log('📊 [HomeView] 响应数据:', response.data)
    console.log('📈 [HomeView] 商家数量:', response.data?.data?.length || 0)
    
    // 检查响应数据结构
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      businesses.value = response.data.data
      console.log('🎯 [HomeView] 商家数据已更新:', businesses.value)
      
      // 打印每个商家的详细信息
      businesses.value.forEach((business, index) => {
        console.log(`🏪 [HomeView] 商家 ${index + 1}:`, {
          id: business.id,
          name: business.businessName,
          rating: business.rating,
          sales: business.sales,
          distance: business.distance,
          delivery: business.delivery,
          minOrder: business.minOrder
        })
      })
    } else {
      console.warn('⚠️ [HomeView] 响应数据格式不正确:', response.data)
      console.warn('⚠️ [HomeView] 期望格式: {data: {data: [...]}}, 实际格式:', typeof response.data)
    }
      } catch (error: any) {
      console.error('❌ [HomeView] 获取商家列表失败:', error)
      console.error('🚨 [HomeView] 错误详情:', {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data
      })
    }
}

onMounted(() => {
  console.log('🚀 [HomeView] 组件已挂载，开始初始化...')
  fetchBusinessList()
})

const categories = ref([
  { id: 1, name: '美食外卖', icon: `${OSS_URL}/food.png` },
  { id: 2, name: '超市便利', icon: `${OSS_URL}/market.png` },
  { id: 3, name: '鲜花礼品', icon: `${OSS_URL}/flower.png` },
  { id: 4, name: '水果买菜', icon: `${OSS_URL}/fruit.png` },
  { id: 5, name: '看病买药', icon: `${OSS_URL}/medicine.png` },
  { id: 6, name: '甜品饮品', icon: `${OSS_URL}/drink.png` },
  { id: 7, name: '天天爆红包', icon: `${OSS_URL}/redpacket.png` },
  { id: 8, name: '0元领水果', icon: `${OSS_URL}/tree.png` },
  { id: 9, name: '跑腿', icon: `${OSS_URL}/run.png` },
  { id: 10, name: '炸鸡小吃', icon: `${OSS_URL}/snack.png` }
])

const activeTab = ref('常点')
</script>

<template>
  <div class="home">
    <div class="header-section">
      <!-- 上半部分：背景图片和渐变遮罩 -->
      <div class="header-bg">
        <div class="bg-image" :style="{backgroundImage: `url(${OSS_URL}/headbg.jpeg)`}"></div>
        <div class="bg-overlay"></div>
        <div class="header-content">
          <!-- 标签切换 -->
          <div class="nav-tabs">
            <div class="tab-wrapper">
              <div 
                class="tab"
                :class="{ active: activeTab === '常点' }"
                @click="activeTab = '常点'"
              >常点</div>
              <div class="tab-divider">|</div>
              <div 
                class="tab"
                :class="{ active: activeTab === '推荐' }"
                @click="activeTab = '推荐'"
              >推荐</div>
            </div>
            <div class="location">
              <van-icon name="location-o" />
              <span>云南大学呈贡校区楸苑</span>
              <van-icon name="arrow-down" />
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索框部分 -->
      <div class="search-section">
        <div class="search-box">
          <div class="search-icon">
            <van-icon name="exchange" />
          </div>
          <input 
            type="text" 
            placeholder="chagee霸王茶姬|塔斯汀中国汉堡"
          >
          <div class="search-button">搜索</div>
        </div>
      </div>
    </div>

    <!-- 分类按钮区域 -->
    <div class="category-section">
      <div class="category-grid">
        <div 
          v-for="category in categories" 
          :key="category.id"
          class="category-item"
        >
          <img :src="category.icon" :alt="category.name">
          <span>{{ category.name }}</span>
        </div>
      </div>
    </div>

    <!-- 优惠标签 -->
    <div class="promotion-tags-wrapper">
      <div class="promotion-tags">
        <div class="tag">天天爆红包</div>
        <div class="tag">减配送费</div>
        <div class="tag">无门槛红包</div>
        <div class="tag">满减优惠</div>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="shop-list">
      <div v-if="businesses.length === 0" class="empty-state">
        <p>🔍 正在加载商家数据...</p>
      </div>
      
      <router-link
        v-for="business in businesses"
        :key="business.id"
        :to="`/shop/${business.id}`"
        class="shop-item"
      >
        <img :src="business.imgLogo" class="shop-img">
        <div class="shop-info">
          <h3 class="shop-name">{{ business.businessName }}</h3>
          <div class="shop-rating">
            <span class="rating">{{ business.rating }}</span>
            <span class="sales">{{ business.sales }}</span>
          </div>
          <div class="shop-delivery">
            <span>{{ business.distance }}</span>
            <span>{{ business.comment }}</span>
          </div>
                      <div class="shop-price">
              <span>{{ business.minOrder }}</span>
              <span>{{ business.delivery }}</span>
            </div>
          <div class="shop-promotions">
            <span 
              v-for="(discount, index) in business.discountsList" 
              :key="index"
              class="promotion-tag"
            >
              {{ discount }}
            </span>
          </div>
        </div>
      </router-link>
    </div>
    <TabBar />
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(to bottom, #ffffff, #f0f7ff);
  padding-bottom: 50px;
  margin: 0;
  width: 100%;
}

.header-section {
  background: transparent;
  width: 100%;
  margin: 0;
}

.header-bg {
  position: relative;
  height: 56px;
  overflow: hidden;
  width: 100%;
  margin: 0;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  opacity: 0.6; /* 调整图片透明度 */
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(255,255,255,0.8), rgba(255,255,255,0.95));
}

.header-content {
  position: relative;
  z-index: 1;
  padding: 10px 15px;
}

.nav-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  cursor: pointer;
  padding: 0 4px;
  position: relative;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 2px;
  background: #0095ff;
}

.tab-divider {
  color: #999;
  font-weight: normal;
}

.location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #333;
}

.search-section {
  padding: 8px 12px;
  background: #fff;
  width: 100%;
  margin: 0;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 100px;
  padding: 4px;
  height: 36px;
}

.search-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  color: #999;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  padding: 0 8px;
}

.search-box input::placeholder {
  color: #999;
}

.search-button {
  background: #0095ff;
  color: #fff;
  padding: 4px 16px;
  border-radius: 100px;
  font-size: 14px;
  margin-left: 8px;
}

.category-section {
  padding: 15px;
  width: 100%;
  margin: 0;
  background: transparent;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
  z-index: 1;
}

.category-item img {
  width: 40px;
  height: 40px;
}

.category-item span {
  font-size: 12px;
  color: #333;
}

.promotion-tags-wrapper {
  padding: 0 12px;
  margin-top: 10px;
  background: transparent;
}

.promotion-tags {
  display: flex;
  padding: 12px 15px;
  gap: 10px;
}

.tag {
  padding: 6px 12px;
  background: #fff;
  border-radius: 16px;
  font-size: 12px;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shop-list {
  margin-top: 8px;
  width: 100%;
  padding: 0 12px;
  background: transparent;
}

.shop-item {
  display: flex;
  padding: 15px;
  background: #fff;
  text-decoration: none;
  color: #333;
  margin-bottom: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shop-item:last-child {
  margin-bottom: 0;
}

.shop-item {
  border-bottom: none;
}

.shop-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  margin-right: 12px;
}

.shop-info {
  flex: 1;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px;
}

.shop-rating {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.shop-delivery {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.shop-price {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.shop-promotions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.promotion-tag {
  padding: 2px 4px;
  background: #fff5f5;
  color: #ff4e4e;
  font-size: 12px;
  border-radius: 2px;
}

.van-icon {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
}
</style>
