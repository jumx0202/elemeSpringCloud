<script setup lang="ts">
import { ref } from 'vue'
import TabBar from '@/components/TabBar.vue'

const messages = ref([])
</script>

<template>
  <div class="message">
    <div class="header">
      <div class="title">消息中心</div>
      <div class="icons">
        <i class="icon-clean">&#x1F9F9;</i>
        <i class="icon-settings">&#x2699;</i>
      </div>
    </div>

    <div class="section-title">
      <span>平台消息</span>
      <span class="view-all">查看全部 ></span>
    </div>

    <div class="year-report">
      <div class="report-content">
        <img src="/img/message/report-icon.png" alt="report" class="report-icon">
        <div class="report-text">
          <div class="report-title">
            <span class="tag">年度</span>
            请查收你的年终报告
          </div>
          <div class="report-subtitle">年度报告携红包向你走来</div>
        </div>
        <div class="close-btn">×</div>
      </div>
    </div>

    <div class="section-title">
      <span>聊天动态</span>
    </div>

    <div class="chat-card">
      <div class="empty-state" v-if="messages.length === 0">
        <img src="/img/message/empty.png" alt="no messages">
        <p class="main-text">竟然没有消息</p>
        <p class="sub-text">你知道吗 我永远是你召之即来的朋友</p>
      </div>
    </div>
  </div>
  <TabBar />
</template>

<style scoped>
.message {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.header {
  background: white;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.icons {
  display: flex;
  gap: 15px;
}

.icon-clean,
.icon-settings {
  font-size: 20px;
  color: #333;
  cursor: pointer;
}

.section-title {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  color: #333;
}

.view-all {
  color: #999;
  font-size: 14px;
}

.year-report {
  background: linear-gradient(180deg, #e8f3ff 0%, #ffffff 100%);
  padding: 15px;
  margin: 0 15px;
  border-radius: 8px;
}

.report-content {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.report-icon {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

.report-text {
  flex: 1;
}

.report-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.tag {
  background: #409EFF;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.report-subtitle {
  font-size: 12px;
  color: #999;
}

.close-btn {
  position: absolute;
  right: 0;
  top: -5px;
  color: #999;
  font-size: 20px;
  cursor: pointer;
}

.chat-card {
  background: white;
  margin: 0 15px;
  border-radius: 8px;
  min-height: calc(100vh - 320px);
  margin-bottom: 15px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}

.empty-state img {
  width: 120px;
  height: 120px;
  margin-bottom: 15px;
}

.main-text {
  font-size: 16px;
  color: #333;
  margin: 5px 0;
}

.sub-text {
  font-size: 12px;
  color: #999;
  margin: 5px 0;
}
</style> 