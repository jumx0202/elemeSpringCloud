<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentPath = ref(route.path)

const tabs = [
  {
    name: '首页',
    icon: 'home-o',
    path: '/'
  },
  {
    name: '全能超市',
    icon: 'shop-o',
    path: '/market'
  },
  {
    name: '消息',
    icon: 'chat-o',
    path: '/message'
  },
  {
    name: '购物车',
    icon: 'shopping-cart-o',
    path: '/cart'
  },
  {
    name: '我的',
    icon: 'user-o',
    path: '/profile'
  }
]
</script>

<template>
  <div class="tab-bar">
    <router-link 
      v-for="tab in tabs" 
      :key="tab.path"
      :to="tab.path"
      class="tab-item"
      :class="{ active: currentPath === tab.path }"
    >
      <van-icon :name="tab.icon" />
      <span>{{ tab.name }}</span>
    </router-link>
  </div>
</template>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 5px 0;
  background: #fff;
  border-top: 1px solid #eee;
  width: 100%;
  margin: 0;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #666;
  font-size: 12px;
  text-decoration: none;
}

.tab-item .van-icon {
  font-size: 20px;
}

.tab-item.active {
  color: #0095ff;
}
</style> 