<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const address = ref({
  location: '云南大学呈贡校区栋苑',
  detail: '',
  contact: '86',
  phone: '17787280204'
})

const deliveryTime = ref('预计21:23-21:38送达')

const order = ref({
  items: [
    {
      name: '大桥大绿-大杯',
      price: 16,
      desc: '大杯/七分糖/热饮/茉莉绿茶',
      quantity: 1
    },
    {
      name: 'QQ美莓奶茶-大杯',
      price: 19,
      desc: '大杯/五分糖/常温/红茶',
      quantity: 1
    }
  ],
  packingFee: 2,
  totalPrice: 27.2,
  discount: 14
})

const submitOrder = () => {
  router.push('/payment')
}
</script>

<template>
  <div class="checkout">
    <div class="header">
      <div class="back" @click="$router.back()">
        <i class="icon-back"></i>
      </div>
      <div class="title">外卖配送</div>
    </div>

    <div class="delivery-info">
      <div class="address">
        <div class="location">{{ address.location }}</div>
        <input type="text" v-model="address.detail" placeholder="门牌号，例:1层102室">
        <div class="contact">
          <input type="text" v-model="address.contact" placeholder="联系人姓名">
          <input type="tel" v-model="address.phone" placeholder="手机号码">
        </div>
        <div class="tags">
          <span class="tag">家</span>
          <span class="tag">公司</span>
          <span class="tag">学校</span>
        </div>
      </div>

      <div class="delivery-time">
        <div class="time-label">送达时间</div>
        <div class="time">{{ deliveryTime }}</div>
      </div>
    </div>

    <div class="order-detail">
      <div class="shop-name">1点点(仕林街店)</div>
      
      <div class="items">
        <div v-for="(item, index) in order.items" :key="index" class="item">
          <div class="item-info">
            <div class="name">{{ item.name }}</div>
            <div class="desc">{{ item.desc }}</div>
          </div>
          <div class="price">¥{{ item.price }}</div>
        </div>
      </div>

      <div class="fees">
        <div class="fee-item">
          <span>打包费</span>
          <span>¥{{ order.packingFee }}</span>
        </div>
      </div>

      <div class="total">
        <span>已优惠¥{{ order.discount }}</span>
        <span class="price">合计 ¥{{ order.totalPrice }}</span>
      </div>
    </div>

    <div class="submit-bar">
      <div class="price">¥{{ order.totalPrice }}</div>
      <button class="submit-btn" @click="submitOrder">提交订单</button>
    </div>
  </div>
</template>

<style scoped>
.checkout {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 50px;
}

.header {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #fff;
}

.back {
  margin-right: 10px;
}

.title {
  font-size: 16px;
}

.delivery-info {
  margin-top: 10px;
  background: #fff;
  padding: 15px;
}

.location {
  font-size: 16px;
  margin-bottom: 10px;
}

.address input {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.tags {
  display: flex;
  gap: 10px;
}

.tag {
  padding: 5px 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  font-size: 12px;
}

.delivery-time {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.order-detail {
  margin-top: 10px;
  background: #fff;
  padding: 15px;
}

.shop-name {
  font-size: 16px;
  margin-bottom: 15px;
}

.item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.item-info .desc {
  font-size: 12px;
  color: #666;
}

.fees {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #fff;
  border-top: 1px solid #eee;
}

.submit-btn {
  background: #0095ff;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
}
</style> 