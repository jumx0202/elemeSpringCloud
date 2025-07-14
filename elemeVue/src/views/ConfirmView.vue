<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'
import { api } from '@/utils/api'

const router = useRouter()
const orderDetail = ref(null)
const merchantData = ref(null)
const selectedItems = ref([])
const totalPrice = ref(0)

const loadOrderDetail = async () => {
  const orderDetailString = localStorage.getItem('orderDetail')
  if (orderDetailString) {
    orderDetail.value = JSON.parse(orderDetailString)
    try {
      const response = await api.business.getById(Number(orderDetail.value.id))
      merchantData.value = response.data
      if (merchantData.value) {
        selectedItems.value = merchantData.value.foodList.filter(
          item => orderDetail.value.selectedItemIds.includes(item.id)
        )
        totalPrice.value = orderDetail.value.price + 3.8 + 3
      }
    } catch (error) {
      console.error('获取商家信息失败:', error)
    }
  }
}

const toPay = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo'))
  try {
    const response = await api.order.create(orderDetail.value)
    
    const payInfo = {
      price: totalPrice.value,
      shopName: merchantData.value.businessName,
      id: response.data
    }
    localStorage.setItem('payInfo', JSON.stringify(payInfo))
    router.push('/payment')
  } catch (error) {
    console.error('创建订单失败:', error)
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="confirm-page" v-if="orderDetail && merchantData">
    <div class="header">确认订单</div>
    
    <div class="address-card">
      <div class="address">云南大学呈贡校区国家示范性软件学院1219</div>
      <div class="contact">郭（先生） 13173475244</div>
      <div class="delivery-time">
        <span>立即送出</span>
        <span>预计12:33送达</span>
      </div>
    </div>

    <div class="order-details">
      <h2>{{ merchantData.businessName }}</h2>
      
      <div class="items">
        <div v-for="item in selectedItems" :key="item.id" class="item">
          <img :src="item.img" :alt="item.name">
          <div class="item-info">
            <span>{{ item.name }}</span>
            <span>￥{{ item.redPrice }}</span>
          </div>
        </div>
      </div>

      <div class="fees">
        <div class="fee-item">
          <span>打包费</span>
          <span>￥3.8</span>
        </div>
        <div class="fee-item">
          <span>配送费</span>
          <span>￥3</span>
        </div>
      </div>

      <div class="total">
        <div class="discount">已优惠 ￥5.91</div>
        <div class="final-price">合计 ￥{{ totalPrice.toFixed(2) }}</div>
      </div>
    </div>

    <div class="submit-bar">
      <div class="price-info">
        <div class="total-price">￥{{ totalPrice.toFixed(2) }}</div>
        <div class="discount-info">已优惠￥5.91</div>
      </div>
      <van-button type="primary" @click="toPay">提交订单</van-button>
    </div>
  </div>
</template>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.header {
  background: #E60012;
  color: white;
  padding: 16px;
  text-align: center;
}

.address-card {
  background: white;
  margin: 16px;
  padding: 16px;
  border-radius: 8px;
}

.address {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.contact {
  color: #666;
  margin-bottom: 8px;
}

.delivery-time {
  display: flex;
  justify-content: space-between;
  color: #999;
}

.order-details {
  background: white;
  margin: 16px;
  padding: 16px;
  border-radius: 8px;
}

.item {
  display: flex;
  margin: 12px 0;
}

.item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  margin-right: 12px;
}

.item-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fees {
  margin: 16px 0;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  color: #666;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
}

.total-price {
  font-size: 18px;
  font-weight: bold;
}

.discount-info {
  color: #999;
  font-size: 12px;
}
</style> 