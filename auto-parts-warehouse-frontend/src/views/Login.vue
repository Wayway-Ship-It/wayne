<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="login-header">
        <h2>汽车配件仓库管理系统</h2>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" @keyup.enter.native="handleLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%;" :loading="loading">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="showRegister = true" style="width: 100%;">注册新用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="用户注册" :visible.sync="showRegister" width="500px">
      <el-form :model="registerForm" :rules="registerRules" ref="registerForm" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registerLoading">注册</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'Login',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        phone: '',
        email: ''
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ]
      },
      showRegister: false,
      loading: false,
      registerLoading: false
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const res = await this.$axios.post('/api/auth/login', this.loginForm)
            if (res.code === 200) {
              this.login(res.data)
              this.$message.success('登录成功')
              this.$router.push('/')
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('登录失败，请重试')
          } finally {
            this.loading = false
          }
        }
      })
    },
    async handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (valid) {
          this.registerLoading = true
          try {
            const res = await this.$axios.post('/api/auth/register', {
              username: this.registerForm.username,
              password: this.registerForm.password,
              realName: this.registerForm.realName,
              phone: this.registerForm.phone,
              email: this.registerForm.email
            })
            if (res.code === 200) {
              this.$message.success('注册成功，请登录')
              this.showRegister = false
              this.registerForm = {
                username: '',
                password: '',
                confirmPassword: '',
                realName: '',
                phone: '',
                email: ''
              }
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('注册失败，请重试')
          } finally {
            this.registerLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-image: url('@/assets/background.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  width: 400px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 0;
  color: #333;
}
</style>
