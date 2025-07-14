import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import Vant from 'vant'
import 'element-plus/dist/index.css'
import 'vant/lib/index.css'
import 'tailwindcss/tailwind.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(Vant)

app.mount('#app')
