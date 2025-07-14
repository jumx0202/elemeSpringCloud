<script setup lang="ts">
import { ref } from 'vue'
import TabBar from '@/components/TabBar.vue'

const categories = ref([
  { id: 1, name: '微醺小酒馆', icon: '/img/market/wine.png' },
  { id: 2, name: '签到返钱', icon: '/img/market/money.png' },
  { id: 3, name: '冰淇淋零食', icon: '/img/market/ice-cream.png' },
  { id: 4, name: '酒水饮料', icon: '/img/market/drink.png' },
  { id: 5, name: '肉蛋蔬菜', icon: '/img/market/vegetable.png' },
  { id: 6, name: '水果', icon: '/img/market/fruit.png' }
])

const tabs = ref([
  '推荐', '休闲零食', '酒水', '水/饮料', '日常用药', '肉蛋'
])

const products = ref([
  {
    id: 1,
    name: '锐澳微醺白桃白兰地味鸡尾酒330ml/罐',
    price: 5.7,
    image: '/img/market/rio.png',
    deliveryTime: '48分钟送达',
    limit: '限份'
  }
])
</script>

<template>
  <div class="market">
    <div class="header">
      <div class="location">云南大学呈贡校区...</div>
      <div class="search-box">
        <input type="text" placeholder="鸡爪">
        <button class="search-btn">搜索</button>
      </div>
    </div>

    <div class="category-list">
      <div v-for="category in categories" :key="category.id" class="category-item">
        <img :src="category.icon" :alt="category.name">
        <div class="category-name">{{ category.name }}</div>
      </div>
    </div>

    <div class="tabs">
      <div v-for="(tab, index) in tabs" 
           :key="index" 
           class="tab-item"
           :class="{ active: index === 0 }">
        {{ tab }}
      </div>
    </div>

    <div class="promotion-banner">
      <img src="/img/night_map.png" alt="24h福利夜">
      <div class="promotion-text">
        <div class="promo-content">
          <span class="promo-text">领40元</span>
          <span class="go-btn">GO</span>
          <span class="promo-text">包</span>
        </div>
        <div class="promo-subtitle">抽福利夜定制礼品</div>
      </div>
    </div>

    <div class="product-list">
      <div v-for="product in products" :key="product.id" class="product-item">
        <img :src="product.image" :alt="product.name">
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <div class="delivery-info">{{ product.deliveryTime }}</div>
          <div class="price-info">
            <span class="price">¥{{ product.price }}</span>
            <span class="limit">{{ product.limit }}</span>
            <button class="add-btn">+</button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <TabBar />
</template>

<style scoped>
.market {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 50px;
}

.header {
  background: linear-gradient(to right, #7828c4, #9f4bea);
  padding: 15px;
  color: white;
}

.location {
  font-size: 18px;
  margin-bottom: 10px;
}

.search-box {
  display: flex;
  background: white;
  border-radius: 20px;
  padding: 8px 15px;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
}

.search-btn {
  background: #0095ff;
  color: white;
  border: none;
  border-radius: 15px;
  padding: 5px 15px;
}

.category-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  padding: 15px;
  background: white;
  gap: 10px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-item img {
  width: 40px;
  height: 40px;
  margin-bottom: 2px;
}

.category-name {
  width: 100%;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  color: #333;
}

.tabs {
  display: flex;
  overflow-x: auto;
  background: white;
  padding: 10px 0;
  margin-top: 10px;
}

.tab-item {
  flex-shrink: 0;
  padding: 5px 15px;
  font-size: 14px;
  color: #666;
}

.tab-item.active {
  color: #0095ff;
  position: relative;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #0095ff;
}

.promotion-banner {
  margin: 10px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  height: 120px;
}

.promotion-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.promotion-text {
  position: absolute;
  bottom: 20px;
  left: 20px;
  color: white;
}

.promo-content {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.promo-text {
  font-size: 20px;
  font-weight: bold;
}

.go-btn {
  background: white;
  color: #0095ff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
}

.promo-subtitle {
  font-size: 14px;
  opacity: 0.8;
}

.product-list {
  background: white;
  margin-top: 10px;
}

.product-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.product-item img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  margin-right: 15px;
}

.product-info {
  flex: 1;
}

.product-info h3 {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.delivery-info {
  font-size: 12px;
  color: #999;
  margin: 8px 0;
}

.price-info {
  display: flex;
  align-items: center;
}

.price {
  color: #ff5339;
  font-size: 16px;
  font-weight: bold;
}

.limit {
  margin-left: 5px;
  font-size: 12px;
  color: #999;
}

.add-btn {
  margin-left: auto;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #0095ff;
  color: white;
  border: none;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 