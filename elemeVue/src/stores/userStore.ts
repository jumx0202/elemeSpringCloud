import { defineStore } from 'pinia'

interface UserState {
  token: string | null
  loginAttempts: number
  userInfo: any | null
  showCaptcha: boolean
  captchaId: string
  captchaImage: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => {
    // 安全地解析 localStorage 中的数据
    const userInfoStr = localStorage.getItem('userInfo')
    let userInfo = null
    
    try {
      // 只有当 userInfoStr 存在且不是 'undefined' 字符串时才解析
      if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
        userInfo = JSON.parse(userInfoStr)
      }
    } catch (error) {
      console.error('Error parsing userInfo from localStorage:', error)
      // 如果解析失败，移除无效的数据
      localStorage.removeItem('userInfo')
    }

    const token = localStorage.getItem('token')
    return {
      token: token && token !== 'undefined' && token !== 'null' ? token : null,
      loginAttempts: 0,
      userInfo,
      showCaptcha: false,
      captchaId: '',
      captchaImage: ''
    }
  },

  getters: {
    isLoggedIn: (state): boolean => {
      return !!(state.token && state.userInfo && state.userInfo.phoneNumber)
    },
    needsCaptcha: (state): boolean => state.showCaptcha
  },

  actions: {
    incrementLoginAttempts() {
      this.loginAttempts++
      if (this.loginAttempts >= 3) {
        this.showCaptcha = true
      }
    },

    resetLoginAttempts() {
      this.loginAttempts = 0
      this.showCaptcha = false
    },

    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUserInfo(userInfo: any) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    setCaptcha(id: string, image: string) {
      this.captchaId = id
      this.captchaImage = image
    },

    logout() {
      this.token = null
      this.userInfo = null
      this.loginAttempts = 0
      this.showCaptcha = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
}) 