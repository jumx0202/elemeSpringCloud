<template>
  <div class="shop-detail">
    <!-- 店铺头部信息 -->
    <div class="shop-header">
      <div class="shop-info">
        <img :src="shop.image" :alt="shop.name" class="shop-logo">
        <div class="shop-basic">
          <h1>{{ shop.name }}</h1>
          <div class="shop-stats">
            <span>评分 {{ shop.rating }}</span>
            <span>月售{{ shop.monthSales }}</span>
            <span>{{ shop.time }}</span>
          </div>
          <div class="shop-notice" v-if="shop.notice">
            <van-notice-bar :text="shop.notice" />
          </div>
        </div>
      </div>
      
      <!-- 优惠信息 -->
      <div class="promotions" v-if="shop.promotions && shop.promotions.length">
        <div v-for="(promo, index) in shop.promotions" 
             :key="index" 
             class="promo-item">
          <van-tag type="danger">{{ promo }}</van-tag>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 导航标签 -->
      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="点餐" name="menu">
          <div class="menu-container">
            <!-- 左侧分类导航 -->
            <div class="category-list">
              <div v-for="category in shop.categories" 
                   :key="category.id"
                   :class="['category-item', { active: currentCategory === category }]"
                   @click="selectCategory(category)">
                {{ category.name }}
              </div>
            </div>
            
            <!-- 右侧商品列表 -->
            <div class="product-list">
              <div v-for="category in shop.categories" 
                   :key="category.id" 
                   class="category-section">
                <h2 class="category-title">{{ category.name }}</h2>
                <div v-for="product in category.products" 
                     :key="product.id" 
                     class="product-item">
                  <img :src="product.image" :alt="product.name" class="product-img">
                  <div class="product-info">
                    <h3 class="product-name">{{ product.name }}</h3>
                    <p class="product-desc">{{ product.description }}</p>
                    <div class="product-sales">月售 {{ product.monthSales }}</div>
                    <div class="product-price-info">
                      <div class="price">
                        <span class="current">¥{{ product.price }}</span>
                        <span v-if="product.originalPrice" class="original">¥{{ product.originalPrice }}</span>
                      </div>
                      <van-button type="primary" size="small" @click="addToCart(product)">
                        选规格
                      </van-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-tab>
        <van-tab title="评价" :badge="shop.commentCount" name="comments">
          <!-- 评价内容 -->
        </van-tab>
        <van-tab title="商家" name="merchant">
          <!-- 商家信息 -->
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { showToast } from 'vant'

interface Product {
  id: number;
  name: string;
  image: string;
  price: number;
  originalPrice?: number;
  description: string;
  monthSales: number;
  isPreferred: boolean;
  preferredPrice?: number;
}

interface Category {
  id: number;
  name: string;
  icon: string;
  products: Product[];
}

interface Shop {
  id: number;
  name: string;
  rating: number;
  monthSales: string;
  distance: string;
  time: string;
  startPrice: number;
  deliveryPrice: number;
  image: string;
  notice: string;
  commentCount: number;
  tags: string[];
  promotions: string[];
  categories: Category[];
}

const route = useRoute()
const shop = ref<Shop>({} as Shop)
const activeTab = ref('menu')
const currentCategory = ref<Category | null>(null)

const selectCategory = (category: Category) => {
  currentCategory.value = category
}

const fetchShopDetail = async () => {
  try {
    const response = await axios.get<Shop>(`http://localhost:8080/api/shops/${route.params.id}/detail`)
    console.log('Raw response:', response)
    console.log('Shop data:', response.data)
    shop.value = response.data
    console.log('Categories:', shop.value.categories)
    if (shop.value.categories?.length) {
      currentCategory.value = shop.value.categories[0]
      console.log('Selected category:', currentCategory.value)
    }
  } catch (error) {
    console.error('Error fetching shop details:', error)
    showToast('获取店铺信息失败')
  }
}

const addToCart = (product: Product) => {
  showToast('已添加到购物车')
}

onMounted(() => {
  fetchShopDetail()
})
</script>

<style scoped>
.shop-detail {
  min-height: 100vh;
  background: #f5f5f5;
}

.shop-header {
  background: #fff;
  padding: 15px;
}

.shop-info {
  display: flex;
  gap: 15px;
}

.shop-logo {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.shop-basic h1 {
  font-size: 18px;
  margin-bottom: 8px;
}

.shop-stats {
  font-size: 14px;
  color: #666;
  display: flex;
  gap: 15px;
}

.promotions {
  margin-top: 10px;
}

.promo-item {
  margin-bottom: 5px;
}

.main-content {
  height: calc(100vh - 120px);
  overflow: hidden;
}

.menu-container {
  display: flex;
  height: 100%;
  background: #fff;
}

.category-list {
  width: 85px;
  background: #f8f8f8;
  overflow-y: auto;
  flex-shrink: 0;
}

.category-item {
  padding: 15px 8px;
  font-size: 14px;
  color: #666;
  text-align: center;
  border-left: 3px solid transparent;
}

.category-item.active {
  background: #fff;
  color: #0097ff;
  border-left-color: #0097ff;
}

.product-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.category-section {
  margin-bottom: 20px;
}

.category-title {
  font-size: 16px;
  color: #333;
  padding: 10px 0;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}

.product-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.product-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 16px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.product-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.product-sales {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.product-price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price .current {
  font-size: 18px;
  color: #f60;
  margin-right: 8px;
}

.price .original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}
</style> 