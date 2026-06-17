<template>
  <el-container class="layout-container">
    <el-aside width="240px">
      <div class="logo">
        <h3>汽车配件仓库管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b">
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-submenu index="/part">
          <template slot="title">
            <i class="el-icon-goods"></i>
            <span>配件管理</span>
          </template>
          <el-menu-item index="/part">
            <span>配件信息</span>
          </el-menu-item>
          <el-menu-item index="/category">
            <span>分类管理</span>
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/inbound">
          <i class="el-icon-download"></i>
          <span slot="title">入库管理</span>
        </el-menu-item>
        <el-menu-item index="/outbound">
          <i class="el-icon-upload2"></i>
          <span slot="title">出库管理</span>
        </el-menu-item>
        <el-menu-item index="/stock">
          <i class="el-icon-box"></i>
          <span slot="title">库存管理</span>
        </el-menu-item>
        <el-menu-item index="/stock-check">
          <i class="el-icon-document-checked"></i>
          <span slot="title">库存盘点</span>
        </el-menu-item>
        <el-menu-item index="/supplier">
          <i class="el-icon-s-custom"></i>
          <span slot="title">供应商管理</span>
        </el-menu-item>
        <el-menu-item index="/customer">
          <i class="el-icon-s-custom"></i>
          <span slot="title">客户管理</span>
        </el-menu-item>
        <el-menu-item index="/user" v-if="isAdmin">
          <i class="el-icon-user"></i>
          <span slot="title">用户管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-user-solid"></i>
          <span slot="title">个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <span>{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <i class="el-icon-user-solid"></i>
              {{ user.realName }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters(['isAdmin']),
    user() {
      return this.$store.state.user
    },
    activeMenu() {
      return this.$route.path
    },
    currentPageTitle() {
      return this.$route.meta.title || '首页'
    }
  },
  methods: {
    ...mapActions(['logout']),
    handleCommand(command) {
      if (command === 'logout') {
        this.logout()
        this.$router.push('/login')
        this.$message.success('已退出登录')
      } else if (command === 'profile') {
        // 检查当前是否已经在个人信息页面，避免重复导航
        if (this.$route.path !== '/profile') {
          this.$router.push('/profile')
        }
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #545c64;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #434a50;
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left span {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
