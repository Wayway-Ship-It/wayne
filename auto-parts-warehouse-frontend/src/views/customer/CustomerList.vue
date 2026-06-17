<template>
  <div class="customer-list">
    <el-card shadow="hover">
      <div slot="header" class="clearfix">
        <span>客户管理</span>
        <el-button type="primary" @click="openAddDialog" style="float: right;">添加客户</el-button>
      </div>
      
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.name" placeholder="请输入客户名称"></el-input>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contactPerson" placeholder="请输入联系人"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" border>
        <el-table-column label="序号" width="80">
          <template slot-scope="scope">
            {{ (searchForm.page - 1) * searchForm.size + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="客户名称"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="contactPerson" label="联系人"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCustomer(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" style="margin-top: 20px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="searchForm.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="searchForm.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog title="客户信息" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCustomer">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tableData: [],
      total: 0,
      searchForm: {
        page: 1,
        size: 10,
        name: '',
        contactPerson: ''
      },
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      },
      rules: {
        name: [
          { required: true, message: '请输入客户名称', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.$axios.get('/api/customer/list', { params: this.searchForm })
        .then(res => {
          if (res.code === 200) {
            this.tableData = res.data.records
            this.total = res.data.total
          } else {
            this.$message.error(res.message || '加载失败')
          }
        })
        .catch(error => {
          this.$message.error('加载失败：' + error.message)
        })
    },
    resetForm() {
      this.searchForm = {
        page: 1,
        size: 10,
        name: '',
        contactPerson: ''
      }
      this.loadData()
    },
    handleSizeChange(size) {
      this.searchForm.size = size
      this.loadData()
    },
    handleCurrentChange(current) {
      this.searchForm.page = current
      this.loadData()
    },
    openAddDialog() {
      this.form = {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.form = { ...row }
      this.dialogVisible = true
    },
    saveCustomer() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.$axios.post('/api/customer/save', this.form)
            .then(res => {
              if (res.code === 200) {
                this.$message.success('保存成功')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(res.message || '保存失败')
              }
            })
            .catch(error => {
              this.$message.error('保存失败：' + error.message)
            })
        }
      })
    },
    deleteCustomer(id) {
      this.$confirm('确定要删除这个客户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/api/customer/delete/${id}`)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.loadData()
            } else {
              this.$message.error(res.message || '删除失败')
            }
          })
          .catch(error => {
            this.$message.error('删除失败：' + error.message)
          })
      }).catch(() => {
        // 取消删除
      })
    }
  }
}
</script>

<style scoped>
.customer-list {
  padding: 20px;
}
</style>