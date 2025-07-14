<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { api } from '@/utils/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const phoneNumber = ref('')
const countryCode = ref('+86')
const password = ref('')
const agreedToTerms = ref(false)
const isButtonActive = ref(false)
const captchaCode = ref('')

const checkButtonStatus = () => {
  isButtonActive.value = Boolean(phoneNumber.value && password.value && agreedToTerms.value)
}

const refreshCaptcha = async () => {
  try {
    const response = await api.captcha.generate()
    console.log('获取验证码响应:', response.data)
    
    if (response.data && response.data.key && response.data.imageBase64) {
      userStore.setCaptcha(response.data.key, response.data.imageBase64)
      console.log('验证码ID:', userStore.captchaId)
    } else {
      console.error('验证码响应格式不正确:', response.data)
      ElMessage.error('获取验证码失败')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请刷新页面重试')
  }
}

const handleLogin = async () => {
  if (!isButtonActive.value) return

  try {
    // 如果需要验证码但未输入
    if (userStore.needsCaptcha && !captchaCode.value) {
      ElMessage.warning('请输入验证码')
      return
    }

    // 验证码验证（如果需要）
    if (userStore.needsCaptcha) {
      try {
        const captchaValid = await api.captcha.validate({
          type: 'image',
          key: userStore.captchaId,
          value: captchaCode.value.toLowerCase()
        })
        
        console.log('验证码验证响应:', captchaValid)
        
        if (!captchaValid.data) {
          ElMessage.error('验证码错误')
          captchaCode.value = ''
          await refreshCaptcha()
          return
        }
      } catch (error) {
        console.error('验证码验证失败:', error)
        ElMessage.error('验证码验证失败')
        return
      }
    }

    // 登录逻辑
    const response = await api.user.login({
      phoneNumber: phoneNumber.value,
      password: password.value
    })

    console.log('Login response:', response.data)

    if (response.data) {
      const { token, user } = response.data.data
      console.log('Received token:', token)
      console.log('Received user:', user)

      if (!user || !user.phoneNumber) {
        console.error('Invalid user data:', user)
        ElMessage.error('登录数据异常')
        return
      }

      // 存储用户信息
      userStore.setToken(token)
      userStore.setUserInfo(user)
      
      // 验证存储是否成功
      console.log('Stored user info:', userStore.userInfo)
      console.log('Stored token:', userStore.token)

      userStore.resetLoginAttempts()
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
    userStore.incrementLoginAttempts()
    
    if (userStore.needsCaptcha) {
      await refreshCaptcha()
      ElMessage.warning('密码错误次数过多，请输入验证码')
    } else {
      ElMessage.error('登录失败，请检查账号密码')
    }
  }
}

const handleOtherLogin = () => {
  router.push('/register')
}

const toRegister = () => {
  router.push('/register')
}

const toggleAgreement = () => {
  agreedToTerms.value = !agreedToTerms.value
  checkButtonStatus()
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
      <div class="phone-input">
        <div class="country-code">
          {{ countryCode }} <van-icon name="arrow-down" />
        </div>
        <input 
          type="text" 
          v-model="phoneNumber" 
          placeholder="请输入手机号" 
          @input="checkButtonStatus"
        />
        <van-icon 
          v-if="phoneNumber" 
          name="cross" 
          class="clear-icon"
          @click="phoneNumber = ''"
        />
      </div>
      <div class="division"></div>
      <div class="password-input">
        <input 
          type="password" 
          v-model="password" 
          placeholder="请输入密码" 
          @input="checkButtonStatus"
        />
      </div>
      <div class="division"></div>
      
      <!-- 验证码输入框 -->
      <div v-if="userStore.needsCaptcha" class="captcha-input">
        <input 
          type="text" 
          v-model="captchaCode" 
          placeholder="请输入验证码" 
          maxlength="4"
        />
        <img 
          :src="userStore.captchaImage" 
          alt="验证码" 
          class="captcha-image"
          @click="refreshCaptcha"
        />
      </div>
      <div v-if="userStore.needsCaptcha" class="division"></div>
    </div>

    <div 
      class="login-button"
      :class="{ active: isButtonActive }"
      @click="handleLogin"
    >
      一键登录
    </div>

    <div class="clause">
      <div 
        class="custom-checkbox" 
        :class="{ checked: agreedToTerms }"
        @click="toggleAgreement"
      >
        <van-icon name="success" v-show="agreedToTerms" />
      </div>
      <span>未注册手机号登录后将自动生成账号，且代表您已阅读并同意</span>
      <span class="blue">《用户服务协议》</span>
      <span>《隐私政策》</span>
      <span>和</span>
      <span class="blue">《运营商协议》</span>
    </div>

    <div class="other-login">
      <div class="divider">
        <span>更多登录方式</span>
      </div>
      <div class="login-methods">
        <div class="method" @click="handleOtherLogin">
          <van-icon name="chat-o" />
        </div>
        <div class="method" @click="handleOtherLogin">
          <van-icon name="alipay" />
        </div>
        <div class="method" @click="handleOtherLogin">
          <van-icon name="wechat" />
        </div>
        <div class="method" @click="handleOtherLogin">
          <van-icon name="more-o" />
        </div>
      </div>
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
  padding: 0 20px;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
}

.back, .help {
  color: #666;
  font-size: 14px;
}

.logo {
  margin-top: 40px;
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
  margin-top: 40px;
}

.phone-input {
  font-size: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
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

.phone-input input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  padding: 0 12px;
  color: #333;
}

.clear-icon {
  color: #999;
  cursor: pointer;
  padding: 4px;
}

.password-input {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-top: 12px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
}

.password-input input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: #333;
}

input::placeholder {
  color: #999;
}

.division {
  margin-top: 8px;
  border: 1px solid #EDEDED;
  width: 100%;
}

.login-button {
  margin-top: 24px;
  border-radius: 25px;
  background-color: #EEEEEE;
  padding: 12px;
  width: 100%;
  text-align: center;
  color: #999999;
  cursor: pointer;
  font-size: 16px;
}

.login-button.active {
  background-color: #0095ff;
  color: white;
}

.clause {
  margin-top: 16px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  text-align: left;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-wrap: wrap;
}

.clause span {
  display: inline-block;
}

.clause .blue {
  color: #0095ff;
  margin: 0 2px;
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
}

.other-login {
  margin-top: auto;
  width: 100%;
  padding: 24px 0;
}

.divider {
  position: relative;
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-bottom: 24px;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background: #eee;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.login-methods {
  display: flex;
  justify-content: space-around;
}

.method {
  font-size: 24px;
  color: #666;
  cursor: pointer;
}

.method:hover {
  color: #0095ff;
}

.captcha-input {
  display: flex;
  align-items: center;
  padding: 12px 0;
}

.captcha-input input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  padding: 0 12px;
}

.captcha-image {
  width: 100px;
  height: 30px;
  margin-left: 12px;
  cursor: pointer;
}
</style> 