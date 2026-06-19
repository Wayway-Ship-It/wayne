<template>
  <div id="app">
    <router-view />
    <!-- 登录页不显示 AI 助手 -->
    <AIAssistant v-if="$route.name !== 'Login'" />
  </div>
</template>

<script>
import AIAssistant from './components/AIAssistant.vue'

export default {
  name: 'App',
  components: {
    AIAssistant
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
}
</style>
