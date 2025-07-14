<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { api } from '@/utils/api'

const router = useRouter()
const countryCode = ref('+86')
const phoneNumber = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const verifyCode = ref('')
const agreedToTerms = ref(false)
const countdown = ref(0)
const timer = ref<number | null>(null)
const isLoading = ref(false)

const isButtonActive = computed(() => {
  return phoneNumber.value && 
         password.value && 
         confirmPassword.value && 
         email.value &&
         verifyCode.value &&
         agreedToTerms.value
})

const validateEmail = (email: string) => {
  return /^[A-Za-z0-9+_.-]+@(.+)$/.test(email)
}

const handleRegister = async () => {
  if (!isButtonActive.value) return
  
  if (password.value !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  try {
    isLoading.value = true
    const response = await api.user.register({
      phoneNumber: phoneNumber.value,
      password: password.value,
      confirmPassword: confirmPassword.value,
      name: phoneNumber.value,  // 使用手机号作为默认用户名
      email: email.value,
      verifyCode: verifyCode.value
    })

    switch(response.data) {
      case -1:
        ElMessage.error('验证码错误')
        break
      case 0:
        ElMessage.success('注册成功')
        router.push('/login')
        break
      case 1:
        ElMessage.error('两次密码不一致')
        break
      case 2:
        ElMessage.error('该手机号已被注册')
        break
      case 3:
        ElMessage.error('密码长度需要大于6位')
        break
      case 4:
        ElMessage.error('邮箱格式不正确')
        break
      default:
        ElMessage.error('注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败')
    console.error('注册失败:', error)
  } finally {
    isLoading.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  timer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
      }
    }
  }, 1000)
}

const sendVerifyCode = async () => {
  if (!email.value) {
    ElMessage.error('请输入邮箱地址')
    return
  }
  
  if (!validateEmail(email.value)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    isLoading.value = true
    const response = await api.user.sendVerifyCode({
      email: email.value
    })
    
    if (response.status === 200) {
      ElMessage.success('验证码已发送，请查收邮件')
      startCountdown()
    } else {
      ElMessage.error(response.data || '发送验证码失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})

const toLogin = () => {
  router.push('/login')
}

const toggleAgreement = () => {
  agreedToTerms.value = !agreedToTerms.value
}
</script>

<template>
  <div class="wrapper2">
    <div class="header">
      <div class="back" @click="router.back()">
        <van-icon name="arrow-left" />
      </div>
      <div class="help">帮助</div>
    </div>

    <div class="logo">
      <img src="/img/elm-logo.png" alt="饿了么" />
      <div class="slogan">放心点 准时达</div>
    </div>

    <div class="input-box">
      <div class="input-group">
        <div class="country-code">
          {{ countryCode }} <van-icon name="arrow-down" />
        </div>
        <input 
          type="text" 
          v-model="phoneNumber" 
          placeholder="请输入手机号" 
        />
        <van-icon 
          v-if="phoneNumber" 
          name="cross" 
          class="clear-icon"
          @click="phoneNumber = ''"
        />
      </div>

      <div class="input-group">
        <input 
          type="password" 
          v-model="password" 
          placeholder="请输入密码" 
        />
      </div>

      <div class="input-group">
        <input 
          type="password" 
          v-model="confirmPassword" 
          placeholder="请确认密码" 
        />
      </div>

      <div class="input-group">
        <input 
          type="email" 
          v-model="email" 
          placeholder="请输入邮箱" 
        />
      </div>

      <div class="input-group verify-group">
        <input 
          type="text" 
          v-model="verifyCode" 
          placeholder="请输入验证码" 
        />
        <button 
          class="verify-button" 
          :disabled="countdown > 0"
          @click="sendVerifyCode"
        >
          {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
        </button>
      </div>
    </div>

    <div 
      class="register-button"
      :class="{ active: isButtonActive }"
      @click="handleRegister"
    >
      注册
    </div>

    <div class="clause">
      <div 
        class="custom-checkbox" 
        :class="{ checked: agreedToTerms }"
        @click="toggleAgreement"
      >
        <van-icon name="success" v-show="agreedToTerms" />
      </div>
      <div class="clause-text">
        <span>我已阅读并同意</span>
        <span class="blue">《用户服务协议》</span>
        <span>及</span>
        <span class="blue">《隐私政策》</span>
      </div>
    </div>

    <div class="login-link" @click="toLogin">
      返回登录
    </div>
  </div>
</template>

<style scoped>
.wrapper2 {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 16px;
}

.logo {
  margin: 40px 0;
  text-align: center;
}

.logo img {
  width: 60px;
  height: 60px;
}

.slogan {
  color: #0095ff;
  font-size: 20px;
  margin-top: 16px;
}

.input-box {
  width: 100%;
  padding: 0 16px;
}

.input-group {
  background: #f7f8fa;
  border-radius: 12px;
  padding: 4px 12px;
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.country-code {
  color: #333;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 12px;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.input-group input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: #333;
  height: 48px;
  padding: 0 8px;
}

.clear-icon {
  color: #999;
  cursor: pointer;
}

.verify-group {
  padding-right: 4px;
}

.verify-button {
  border: none;
  background: #0095ff;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  height: 36px;
  margin: 6px;
}

.verify-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.register-button {
  margin: 24px 16px;
  border-radius: 25px;
  background-color: #EEEEEE;
  padding: 12px;
  width: calc(100% - 32px);
  text-align: center;
  color: #999999;
  cursor: pointer;
  font-size: 16px;
}

.register-button.active {
  background-color: #0095ff;
  color: white;
}

.clause {
  margin: 16px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  display: flex;
  align-items: center;
  padding: 0 16px;
}

.clause-text {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.custom-checkbox {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ddd;
  margin-right: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.custom-checkbox.checked {
  background-color: #0095ff;
  border-color: #0095ff;
}

.custom-checkbox .van-icon {
  color: white;
  font-size: 12px;
}

.blue {
  color: #0095ff;
  margin: 0 2px;
}

.login-link {
  margin-top: 16px;
  color: #0095ff;
  font-size: 14px;
  cursor: pointer;
  text-align: center;
}

.clause span {
  display: inline-block;
}
</style> 