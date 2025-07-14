<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { api } from '@/utils/api'

const router = useRouter()
const payInfo = ref(null)
const remainingTime = ref('14:29')
const selectedMethod = ref('alipay')
let timer: any = null
let orderId = ref(null)

onMounted(() => {
  const payInfoString = localStorage.getItem('payInfo')
  if (payInfoString) {
    payInfo.value = JSON.parse(payInfoString)
    orderId.value = payInfo.value.id
    
    // 获取订单创建时间并计算剩余时间
    fetchOrderTime()
  } else {
    router.push('/shopList')
  }
})

const fetchOrderTime = async () => {
  try {
    const response = await api.order.getOrderTime(orderId.value)
    console.log('Order time response:', response.data)  // 调试日志
    
    if (response.data && response.data.data) {
      const createdAt = new Date(response.data.data)
      const now = new Date()
      const diffMinutes = Math.floor((now.getTime() - createdAt.getTime()) / (1000 * 60))
      const remainingMinutes = 15 - diffMinutes
      
      if (remainingMinutes <= 0) {
        // 订单超时，返回商家页面
        router.push(`/shop/${payInfo.value.businessId}`)
        return
      }
      
      startCountdown(remainingMinutes * 60)
    } else {
      console.error('Get order time failed:', response.data)
      // 如果获取时间失败，使用默认15分钟
      startCountdown(15 * 60)
    }
  } catch (error) {
    console.error('获取订单时间失败:', error)
    // 如果获取时间失败，使用默认15分钟
    startCountdown(15 * 60)
  }
}

const startCountdown = (seconds: number) => {
  let remaining = seconds
  timer = setInterval(() => {
    remaining--
    if (remaining <= 0) {
      clearInterval(timer)
      router.push(`/shop/${payInfo.value.businessId}`)
      return
    }
    const minutes = Math.floor(remaining / 60)
    const secs = remaining % 60
    remainingTime.value = `${minutes}:${secs.toString().padStart(2, '0')}`
  }, 1000)
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const handlePayment = async () => {
  try {
    if (!payInfo.value || !payInfo.value.id) {
      throw new Error('Invalid payment info')
    }
    // 调用支付完成API
    await api.order.markPaid(payInfo.value.id)
    // 支付成功后清除支付信息
    localStorage.removeItem('payInfo')
    router.push('/order')
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}
</script>

<template>
  <div class="payment-page" v-if="payInfo">
    <div class="countdown">
      支付剩余时间 {{ remainingTime }}
    </div>

    <div class="header">
      <h1>支付订单</h1>
    </div>

    <div class="payment-info">
      <div class="amount">
        <span>支付金额</span>
        <span class="price">￥{{ payInfo.price.toFixed(2) }}</span>
      </div>
      
      <div class="shop-info">
        <span>商家名称</span>
        <span>{{ payInfo.shopName }}</span>
      </div>
      
      <div class="order-id">
        <span>订单编号</span>
        <span>{{ payInfo.id }}</span>
      </div>
    </div>

    <div class="payment-methods">
      <div class="method-item" @click="selectedMethod = 'alipay'">
        <img src="/img/alipay.png" alt="支付宝支付">
        <span>支付宝支付</span>
        <div class="radio" :class="{ active: selectedMethod === 'alipay' }"></div>
      </div>
      <div class="method-item" @click="selectedMethod = 'wechat'">
        <img src="/img/wechatpay.png" alt="微信支付">
        <span>微信支付</span>
        <div class="radio" :class="{ active: selectedMethod === 'wechat' }"></div>
      </div>
    </div>

    <div class="submit-bar">
      <van-button type="primary" block @click="handlePayment">
        确认支付 ￥{{ payInfo.price.toFixed(2) }}
      </van-button>
    </div>
  </div>
</template>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.header {
  background: white;
  padding: 16px;
  text-align: center;
  margin-bottom: 16px;
}

.payment-info {
  background: white;
  padding: 16px;
  margin-bottom: 16px;
}

.amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color: #f00;
}

.shop-info,
.order-id {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  color: #666;
}

.payment-methods {
  background: white;
  margin-top: 12px;
  padding: 0 16px;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.method-item:last-child {
  border-bottom: none;
}

.method-item img {
  width: 24px;
  height: 24px;
  margin-right: 12px;
}

.method-item span {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.radio {
  width: 20px;
  height: 20px;
  border: 1px solid #ddd;
  border-radius: 50%;
  position: relative;
}

.radio.active {
  border-color: #0095ff;
}

.radio.active::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  background: #0095ff;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
}

.countdown {
  text-align: center;
  padding: 12px;
  color: #666;
  font-size: 14px;
}
</style> 