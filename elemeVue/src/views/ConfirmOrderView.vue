<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { api } from '@/utils/api'

interface Address {
  id: number
  address: string
  detail: string
  contact: string
  phone: string
  tag: string
}

interface DeliveryTime {
  time: string
  fee: number
  label: string
}

interface OrderDetail {
  id: number
  merchantData: {
    id: number
    businessName: string
    delivery: string
    imgLogo: string
  }
  selectedItems: any[]
  price: number
}

const router = useRouter()
const showAddressPopup = ref(false)
const showNewAddressForm = ref(false)
const showTimePopup = ref(false)
const showRemarkPopup = ref(false)
const selectedAddress = ref<Address>({
  id: 1,
  address: '云南大学呈贡校区国家示范性软件学院',
  detail: '1219',
  contact: '郭（先生）',
  phone: '13173475244',
  tag: '学校'
})

const addresses = ref<Address[]>([
  {
    id: 1,
    address: '云南大学呈贡校区国家示范性软件学院',
    detail: '1219',
    contact: '郭（先生）',
    phone: '13173475244',
    tag: '学校'
  },
  {
    id: 2,
    address: '云南大学呈贡校区(南1门)南1门放桌子上',
    contact: '郭',
    phone: '15615813119',
    detail: '',
    tag: '学校'
  }
])

const newAddress = ref({
  address: '',
  detail: '',
  contact: '',
  phone: '',
  tag: '学校'
})

// 生成送达时间选项
const deliveryTimes = computed(() => {
  const times: DeliveryTime[] = []
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  
  // 从当前时间开始,每20分钟一个时间段
  for(let i = 0; i < 6; i++) {
    const time = new Date(now.getTime() + (i * 20 + 20) * 60000)
    const hour = time.getHours()
    const minute = time.getMinutes()
    times.push({
      time: `${hour}:${minute.toString().padStart(2, '0')}`,
      fee: 3,
      label: i === 0 ? '尽快送达' : `${hour}:${minute.toString().padStart(2, '0')}`
    })
  }
  return times
})

const selectedTime = ref(deliveryTimes.value[0])
const remark = ref('')
const tableware = ref('1')
const needInvoice = ref(false)
const invoiceInfo = ref({
  type: '个人',
  title: '',
  taxNumber: ''
})

const merchantData = ref(null)
const selectedItems = ref([])
const orderDetail = ref<OrderDetail | null>(null)
const packagingFee = ref(1.6)  // 打包费
const originalDeliveryFee = ref(6.0)  // 原配送费
const deliveryDiscount = ref(3.0)  // 配送费优惠
const finalDeliveryFee = computed(() => originalDeliveryFee.value - deliveryDiscount.value)  // 最终配送费

const itemsTotal = ref(0)  // 添加商品总价的ref
const totalPrice = computed(() => {
  const total = itemsTotal.value + packagingFee.value + finalDeliveryFee.value
  return Number(total.toFixed(2))
})

const userStore = useUserStore()

onMounted(() => {
  try {
    const orderDetailString = localStorage.getItem('orderDetail')
    if (orderDetailString) {
      orderDetail.value = JSON.parse(orderDetailString)
      merchantData.value = orderDetail.value.merchantData
      selectedItems.value = orderDetail.value.selectedItems
      
      // 计算商品总价
      itemsTotal.value = selectedItems.value.reduce((sum, item) => sum + item.redPrice, 0)
    } else {
      console.warn('No order detail found in localStorage')
      ElMessage.warning('订单信息不完整')
      router.push('/')
    }
  } catch (error) {
    console.error('Error parsing order detail:', error)
    ElMessage.error('订单数据错误')
    router.push('/')
  }
})

const selectAddress = (address: Address) => {
  selectedAddress.value = address
  showAddressPopup.value = false
}

const addNewAddress = () => {
  // 这里应该调用API保存新地址
  addresses.value.push({
    id: addresses.value.length + 1,
    ...newAddress.value
  })
  showNewAddressForm.value = false
  showAddressPopup.value = false
}

const submitOrder = async () => {
  try {
    // 构建订单数据
    const orderData = {
      businessID: merchantData.value.id,
      userPhone: userStore.userInfo.phoneNumber,
      orderList: selectedItems.value.map(item => item.id),
      price: Number(totalPrice.value.toFixed(2)),  // 移除重复的配送费加算
      deliveryAddress: null,
      receiverName: null,
      receiverPhone: null,
      remark: null,
      deliveryFee: finalDeliveryFee.value  // 使用实际显示的配送费
    }

    console.log('准备提交订单:', orderData)
    console.log('当前用户信息:', userStore.userInfo)
    console.log('商家信息:', merchantData.value)

    // 调用后端API创建订单
    const response = await api.order.create(orderData)
    
    console.log('Order creation response:', response)
    console.log('Response data:', response.data)
    console.log('Response status:', response.status)
    console.log('Response headers:', response.headers)
    
    if (response.data && response.data.data) {
      // 准备支付信息
      const payInfo = {
        price: orderData.price,
        shopName: merchantData.value.businessName,
        id: response.data.data  // 修复：使用 response.data.data 获取订单ID
      }
      
      console.log('Payment info:', payInfo)  // 调试日志
      localStorage.setItem('payInfo', JSON.stringify(payInfo))
      
      // 跳转到支付页面
      await router.push({
        path: '/payment',
        query: { timestamp: Date.now() }
      })
    } else {
      console.error('Order creation failed:', response.data)
      console.error('Response structure:', JSON.stringify(response.data, null, 2))
      ElMessage.error('创建订单失败')
    }
  } catch (error) {
    console.error('提交订单时发生错误:', error)
    console.error('Error response:', error.response)
    console.error('Error request:', error.request)
    console.error('Error message:', error.message)
    ElMessage.error('提交订单失败，请重试')
  }
}
</script>

<template>
  <div class="confirm-order">
    <div class="header">
      <span class="header-title">确认订单</span>
    </div>

    <!-- 地址选择 -->
    <div class="address-card" @click="showAddressPopup = true">
      <div class="address-info">
        <div class="address text-ellipsis">{{ selectedAddress.address }} {{ selectedAddress.detail }}</div>
        <div class="contact">
          <span class="contact-info">{{ selectedAddress.contact }} {{ selectedAddress.phone }}</span>
          <van-icon name="copy" class="copy-icon" />
        </div>
        <div class="auto-select">根据当前可配地址自动选择</div>
      </div>
      <van-icon name="arrow" />
    </div>

    <!-- 送达时间 -->
    <div class="delivery-time" @click="showTimePopup = true">
      <span>送达时间</span>
      <span>{{ selectedTime.label }}</span>
    </div>

    <!-- 订单详情 -->
    <div class="order-details">
      <div class="shop-name">
        <img :src="merchantData?.imgLogo" class="shop-logo" alt="shop logo">
        <span>{{ merchantData?.businessName }}</span>
      </div>
      <div class="food-items">
        <div v-for="item in selectedItems" :key="item.id" class="food-item">
          <div class="food-item-left">
            <img :src="item.img" :alt="item.name" class="food-img">
            <div class="food-info">
              <div class="food-name">{{ item.name }}</div>
              <div class="food-quantity">× 1</div>
            </div>
          </div>
          <div class="food-price">
            <span class="original-price">¥{{ item.grayPrice }}</span>
            <span class="current-price">¥{{ item.redPrice }}</span>
          </div>
        </div>
      </div>
      
      <div class="fee-details">
        <div class="fee-item">
          <span>打包费</span>
          <span>¥{{ packagingFee.toFixed(2) }}</span>
        </div>
        <div class="fee-item">
          <span>配送费</span>
          <div class="delivery-fee">
            <span class="original">¥{{ originalDeliveryFee.toFixed(2) }}</span>
            <span class="current">¥{{ finalDeliveryFee.toFixed(2) }}</span>
            <span class="discount-tip">惊喜减{{ deliveryDiscount.toFixed(0) }}元配送费</span>
          </div>
        </div>
      </div>
      
      <div class="discount-items">
        <div class="discount-item">
          <div class="discount-left">
            <van-icon name="cash-back-record" color="#f85" />
            <span>红包/抵用券</span>
          </div>
          <div class="discount-right">
            <span>无可用红包</span>
            <van-icon name="arrow" />
          </div>
        </div>
        <div class="discount-item">
          <div class="discount-left">
            <van-icon name="point-gift-o" color="#f85" />
            <span>下单返豆</span>
          </div>
          <div class="discount-right">
            <span class="reward-beans">返44吃货豆</span>
            <van-icon name="arrow" />
          </div>
        </div>
      </div>
    </div>

    <!-- 其他选项 -->
    <div class="order-options">
      <div class="option-item" @click="showRemarkPopup = true">
        <span>备注</span>
        <span>{{ remark || '口味、偏好等要求' }}</span>
      </div>
      
      <div class="option-item">
        <span>餐具数量</span>
        <van-stepper v-model="tableware" min="0" max="10" />
      </div>

      <div class="option-item">
        <span>发票</span>
        <van-switch v-model="needInvoice" />
      </div>
    </div>

    <!-- 底部提交栏 -->
    <div class="submit-bar">
      <div class="cart-left">
        <div class="total-price">
          <span>合计:</span>
          <span class="price">￥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>
      <van-button 
        class="checkout-btn active"
        @click="submitOrder">
        提交订单
      </van-button>
    </div>

    <!-- 地址选择弹窗 -->
    <van-popup v-model:show="showAddressPopup" position="bottom" round>
      <div class="address-popup">
        <div class="popup-header">
          <h3>选择收货地址</h3>
          <van-button plain size="small" @click="showNewAddressForm = true">新增地址</van-button>
        </div>
        <div class="address-list">
          <div 
            v-for="address in addresses" 
            :key="address.id"
            class="address-item"
            @click="selectAddress(address)"
          >
            <div class="address-content">
              <div class="address-main">{{ address.address }} {{ address.detail }}</div>
              <div class="address-sub">{{ address.contact }} {{ address.phone }}</div>
            </div>
            <van-tag type="primary" v-if="address.tag">{{ address.tag }}</van-tag>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 新增地址表单 -->
    <van-popup v-model:show="showNewAddressForm" position="bottom" round>
      <div class="address-form">
        <div class="form-header">
          <h3>新增地址</h3>
        </div>
        <van-form @submit="addNewAddress">
          <van-field
            v-model="newAddress.address"
            label="地址"
            placeholder="请输入收货地址"
            required
          />
          <van-field
            v-model="newAddress.detail"
            label="门牌号"
            placeholder="例：1号楼102"
          />
          <van-field
            v-model="newAddress.contact"
            label="联系人"
            placeholder="请输入联系人姓名"
            required
          />
          <van-field
            v-model="newAddress.phone"
            label="手机号"
            placeholder="请输入手机号"
            required
          />
          <van-field name="tag" label="标签">
            <template #input>
              <van-radio-group v-model="newAddress.tag" direction="horizontal">
                <van-radio name="家">家</van-radio>
                <van-radio name="公司">公司</van-radio>
                <van-radio name="学校">学校</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
              保存
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 送达时间选择 -->
    <van-popup v-model:show="showTimePopup" position="bottom" round>
      <div class="time-popup">
        <div class="popup-header">
          <h3>选择送达时间</h3>
        </div>
        <div class="time-list">
          <div 
            v-for="time in deliveryTimes"
            :key="time.time"
            class="time-item"
            @click="selectedTime = time; showTimePopup = false"
          >
            <span>{{ time.label }}</span>
            <span class="fee">配送费 ￥{{ time.fee }}</span>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 备注弹窗 -->
    <van-popup v-model:show="showRemarkPopup" position="bottom" round>
      <div class="remark-popup">
        <div class="popup-header">
          <h3>添加备注</h3>
        </div>
        <van-field
          v-model="remark"
          type="textarea"
          placeholder="请输入备注内容"
          rows="3"
        />
        <div class="popup-footer">
          <van-button block type="primary" @click="showRemarkPopup = false">
            确认
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.confirm-order {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 50px;
}

.header {
  background: #e60012;
  color: white;
  text-align: center;
  padding: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
}

.address-card {
  background: white;
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-info {
  flex: 1;
  margin-right: 12px;
}

.address {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 8px;
  line-height: 1.4;
}

.text-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.contact-info {
  color: #666;
  font-size: 14px;
}

.copy-icon {
  color: #666;
  font-size: 14px;
}

.auto-select {
  color: #1989fa;
  font-size: 12px;
}

.delivery-time {
  background: white;
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
}

.order-options {
  background: white;
  margin: 12px;
  border-radius: 8px;
}

.option-item {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
}

.cart-left {
  display: flex;
  align-items: center;
  gap: 12px;
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

/* 弹窗样式 */
.popup-header {
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-list,
.time-list {
  max-height: 60vh;
  overflow-y: auto;
}

.address-item,
.time-item {
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.address-form {
  padding: 16px;
}

.remark-popup {
  padding: 16px;
}

.popup-footer {
  padding: 16px;
}

.order-details {
  background: white;
  margin: 12px;
  border-radius: 8px;
}

.shop-name {
  padding: 16px;
  font-size: 16px;
  font-weight: 500;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-logo {
  width: 20px;
  height: 20px;
  object-fit: cover;
  border-radius: 4px;
}

.food-items {
  padding: 0 16px;
}

.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.food-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.food-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.food-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.food-name {
  font-size: 14px;
  color: #333;
}

.food-quantity {
  font-size: 12px;
  color: #999;
}

.food-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.current-price {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.fee-details {
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.delivery-fee {
  display: flex;
  align-items: center;
  gap: 8px;
}

.delivery-fee .original {
  color: #999;
  text-decoration: line-through;
  font-size: 12px;
}

.delivery-fee .current {
  color: #333;
}

.delivery-fee .discount-tip {
  color: #f85;
  font-size: 12px;
}

.subtotal {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.discount-items {
  padding: 0 16px;
}

.discount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.discount-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}

.discount-right {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #999;
}

.reward-beans {
  color: #f85;
}
</style> 