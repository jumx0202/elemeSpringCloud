<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* 重置默认样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

#app {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  background: #f5f5f5;
}
</style>
