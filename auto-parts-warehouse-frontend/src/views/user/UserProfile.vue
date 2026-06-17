<template>
  <div class="user-profile">
    <el-card>
      <div slot="header" class="header">
        <span>个人信息</span>
      </div>

      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="form.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ form.role === 'ADMIN' ? '管理员' : '普通操作员' }}
          </el-tag>
        </el-form-item>
        
        <el-divider content-position="center">密码修改</el-divider>
        
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入旧密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（至少6位）"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认新密码"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      form: {
        id: null,
        username: '',
        realName: '',
        phone: '',
        email: '',
        role: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      submitLoading: false,
      rules: {
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
        oldPassword: [{ required: false, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: false, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '新密码长度至少为6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: false, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.form.newPassword) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const userId = this.$store.state.user.userId
        const response = await this.$axios.get(`/api/user/${userId}`)
        if (response.code === 200) {
          this.form = response.data
        }
      } catch (error) {
        this.$message.error('加载用户信息失败')
      }
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            // 先准备基本信息更新数据（排除密码字段）
            const userData = { 
              id: this.form.id,
              username: this.form.username,
              realName: this.form.realName,
              phone: this.form.phone,
              email: this.form.email,
              role: this.form.role,
              status: this.form.status,
              createTime: this.form.createTime,
              updateTime: this.form.updateTime
            }
            
            // 检查是否需要修改密码
            if (this.form.oldPassword && this.form.newPassword && this.form.confirmPassword) {
              // 调用密码修改API
              const passwordResponse = await this.$axios.put('/api/user/change-password', {
                id: this.form.id,
                oldPassword: this.form.oldPassword,
                newPassword: this.form.newPassword
              })
              console.log('密码修改响应:', passwordResponse)
              if (passwordResponse.code !== 200) {
                this.$message.error(passwordResponse.message || '密码修改失败')
                return
              }
            }
            
            // 调用基本信息更新API
            const response = await this.$axios.put('/api/user/update', userData)
            if (response.code === 200) {
              this.$message.success('修改成功')
              // 更新store中的用户信息
              this.$store.commit('UPDATE_USER_INFO', response.data)
              // 清空密码字段
              this.form.oldPassword = ''
              this.form.newPassword = ''
              this.form.confirmPassword = ''
            } else {
              this.$message.error(response.message)
            }
          } catch (error) {
            this.$message.error('修改失败')
          } finally {
            this.submitLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-form {
  margin-top: 20px;
}
</style>