<script setup lang="ts">
import { ref, onMounted } from 'vue'
import TabBar from '@/components/TabBar.vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userPhone = ref('')

// 格式化手机号，中间用星号代替
const formatPhoneNumber = (phone: string) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 获取用户信息
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    userPhone.value = formatPhoneNumber(userInfo.phoneNumber)
  } else {
    router.push('/login')
  }
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('userInfo')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  getUserInfo()
})

const orderTabs = [
  { icon: 'https://img.icons8.com/ios/50/000000/list.png', text: '全部' },
  { icon: 'https://img.icons8.com/ios/50/000000/in-transit.png', text: '进行中' },
  { icon: 'https://img.icons8.com/ios/50/000000/rating.png', text: '待评价' },
  { icon: 'https://img.icons8.com/ios/50/000000/refund.png', text: '退款' }
]

const handleOrderTabClick = (tabText: string) => {
  router.push({
    path: '/order',
    query: { 
      type: tabText === '全部' ? 'all' : 
            tabText === '进行中' ? 'ongoing' :
            tabText === '待评价' ? 'pending' : 'refund'
    }
  })
}

const walletItems = [
  { amount: '17.8万', unit: '元', label: '借钱' },
  { amount: '420', unit: '元', label: '外卖红包' },
  { amount: '48', unit: 'h', label: '笔笔返现', tag: '有效' },
  { amount: '10', unit: '元', label: '银行福利' }
]
</script>

<template>
  <div class="profile">
    <div class="header">
      <div class="user-info">
        <img src="https://img.icons8.com/pastel-glyph/64/000000/user-male-circle.png" class="avatar" />
        <span class="phone">{{ userPhone }}</span>
      </div>
      <img 
        src="https://img.icons8.com/ios/50/000000/settings.png" 
        class="settings-icon"
        @click="handleLogout"
      />
    </div>

    <div class="vip-card">
      <div class="vip-info">
        <img src="https://img.icons8.com/ios/50/ffffff/crown.png" class="vip-icon" />
        <span class="vip-text">白银会员 | 1月会员特权已到账</span>
      </div>
      <button class="unlock-btn">去解锁</button>
    </div>

    <div class="benefits">
      <div class="benefit-item">
        <img src="https://img.icons8.com/ios/50/000000/card-security.png" class="benefit-icon" />
        <span>超级吃货卡</span>
        <span class="subtitle">无门槛大额红包</span>
      </div>
      <div class="benefit-item">
        <img src="https://img.icons8.com/ios/50/000000/coins.png" class="benefit-icon" />
        <span>吃货豆</span>
        <span class="subtitle">0豆可用</span>
      </div>
      <div class="benefit-item">
        <img src="https://img.icons8.com/ios/50/000000/gift.png" class="benefit-icon" />
        <span>红包卡券</span>
        <span class="subtitle">2张红包可用</span>
      </div>
    </div>

    <div class="section-title">我的订单</div>
    <div class="order-tabs">
      <div v-for="tab in orderTabs" 
           :key="tab.text" 
           class="tab-item"
           @click="handleOrderTabClick(tab.text)"
      >
        <img :src="tab.icon" class="tab-icon" />
        <span>{{ tab.text }}</span>
      </div>
    </div>

    <div class="annual-report">
      <img src="https://img.icons8.com/ios/50/000000/statistics.png" class="report-icon" />
      <div class="report-text">
        <div class="report-title">2024年度账单出炉啦！</div>
        <div class="report-subtitle">测测你的外卖网感</div>
      </div>
      <button class="check-btn">去看看</button>
    </div>

    <div class="wallet">
      <div class="section-header">
        <span>我的钱包</span>
        <span class="enter">进入 ></span>
      </div>
      <div class="wallet-items">
        <div v-for="item in walletItems" :key="item.label" class="wallet-item">
          <div class="amount">
            {{ item.amount }}
            <span class="unit">{{ item.unit }}</span>
          </div>
          <div class="label">
            {{ item.label }}
            <span v-if="item.tag" class="tag">{{ item.tag }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="favorites">
      <div class="section-header">
        <span>我的关注</span>
        <span class="arrow">></span>
      </div>
      <div class="empty-state">
        <img src="https://img.icons8.com/ios/100/000000/like.png" class="empty-icon" />
        <div class="empty-text">暂无关注</div>
        <div class="empty-subtext">添加店铺，找店不迷路</div>
      </div>
    </div>
  </div>
  <TabBar />
</template>

<style scoped>
.profile {
  min-height: 100vh;
  background: #f0f8ff;
  padding-bottom: 60px;
}

.header {
  padding: 20px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
}

.phone {
  font-size: 18px;
  font-weight: bold;
}

.settings-icon {
  width: 24px;
  height: 24px;
}

.vip-card {
  margin: 0 15px;
  padding: 15px;
  background: #2c3e50;
  border-radius: 8px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vip-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vip-icon {
  width: 20px;
  height: 20px;
}

.unlock-btn {
  padding: 6px 12px;
  border-radius: 15px;
  border: 1px solid white;
  background: transparent;
  color: white;
  font-size: 14px;
}

.benefits {
  margin: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  display: flex;
  justify-content: space-around;
}

.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.benefit-icon {
  width: 30px;
  height: 30px;
}

.subtitle {
  font-size: 12px;
  color: #999;
}

.section-title {
  padding: 15px;
  font-size: 16px;
  font-weight: bold;
}

.order-tabs {
  background: white;
  padding: 15px;
  display: flex;
  justify-content: space-around;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.tab-item:hover {
  opacity: 0.8;
}

.tab-icon {
  width: 24px;
  height: 24px;
}

.annual-report {
  margin: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-icon {
  width: 40px;
  height: 40px;
}

.report-text {
  flex: 1;
}

.report-title {
  font-size: 16px;
  font-weight: bold;
}

.report-subtitle {
  font-size: 12px;
  color: #666;
}

.check-btn {
  padding: 6px 15px;
  border-radius: 20px;
  background: #0095ff;
  color: white;
  border: none;
}

.wallet {
  margin: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
}

.enter {
  color: #999;
  font-size: 14px;
}

.wallet-items {
  display: flex;
  justify-content: space-between;
}

.wallet-item {
  text-align: center;
}

.amount {
  font-size: 20px;
  font-weight: bold;
}

.unit {
  font-size: 14px;
  font-weight: normal;
}

.label {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.tag {
  color: #0095ff;
  margin-left: 4px;
}

.favorites {
  margin: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.empty-state {
  padding: 30px 0;
  text-align: center;
}

.empty-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 10px;
}

.empty-text {
  font-size: 14px;
  color: #333;
}

.empty-subtext {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style> 