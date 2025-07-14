<script setup lang="ts">
import { ref } from 'vue'
import TabBar from '@/components/TabBar.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref([])

const goToMarket = () => {
  router.push('/market')
}

const goToHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="cart">
    <div class="header">
      <div class="header-content">
        <span class="title">购物车</span>
        <span class="location">云南大学呈贡校区楸苑 ▼</span>
      </div>
    </div>

    <div class="quick-actions">
      <div class="action-btn" @click="goToHome">
        <i class="icon">🛒</i>
        <span>我常买</span>
      </div>
      <div class="action-btn" @click="goToMarket">
        <i class="icon">🏪</i>
        <span>全能超市</span>
        <i class="arrow">></i>
      </div>
    </div>

    <div class="empty-state" v-if="cartItems.length === 0">
      <img src="/img/cart/empty.png" alt="empty cart">
      <p class="main-text">购物车竟然空空如也</p>
      <p class="sub-text">快去加购喜欢的商品吧</p>
    </div>
  </div>
  <TabBar />
</template>

<style scoped>
.cart {
  min-height: 100vh;
  background: #f0f8ff;
  padding-bottom: 60px;
}

.header {
  background: #f0f8ff;
  padding: 15px;
  margin-bottom: 12px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.location {
  font-size: 14px;
  color: #333;
  position: relative;
  padding-right: 15px;
}

.location::after {
  content: '▼';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #666;
}

.quick-actions {
  display: flex;
  gap: 10px;
  padding: 15px;
}

.action-btn {
  flex: 1;
  background: white;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.icon {
  font-size: 20px;
}

.action-btn span {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.arrow {
  color: #999;
}

.empty-state {
  margin: 40px 15px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  background: white;
  border-radius: 8px;
  padding: 40px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.empty-state img {
  width: 150px;
  height: 150px;
  margin-bottom: 20px;
}

.main-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.sub-text {
  font-size: 14px;
  color: #999;
}
</style> 