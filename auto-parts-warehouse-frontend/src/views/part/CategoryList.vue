<template>
  <div class="category-list">
    <el-card>
      <div slot="header" class="header">
        <span>配件分类管理</span>
        <el-button type="primary" @click="showAddDialog" icon="el-icon-plus" v-if="$store.state.user.role === 'ADMIN'">新增分类</el-button>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form" v-if="$store.state.user.role === 'ADMIN'">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="分类名称/描述" style="width: 300px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCategories" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="categories" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="80" :index="indexMethod"></el-table-column>
        <el-table-column prop="name" label="分类名称" width="180"></el-table-column>
        <el-table-column prop="description" label="分类描述"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" @click="handleEdit(scope.row)" v-if="$store.state.user.role === 'ADMIN'">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" v-if="$store.state.user.role === 'ADMIN'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CategoryList',
  data() {
    return {
      categories: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      submitLoading: false,
      searchForm: {
        keyword: ''
      },
      form: {
        id: null,
        name: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
        description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadCategories()
  },
  methods: {
    async loadCategories() {
      this.loading = true
      try {
        const response = await this.$axios.get('/api/category/list', { params: this.searchForm })
        if (response.code === 200) {
          this.categories = response.data
        }
      } catch (error) {
        this.$message.error('加载分类失败')
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = {
        keyword: ''
      }
      this.loadCategories()
    },
    showAddDialog() {
      this.dialogTitle = '新增分类'
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        description: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑分类'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            let response
            if (this.isEdit) {
              response = await this.$axios.put('/api/category/update', this.form)
            } else {
              response = await this.$axios.post('/api/category/add', this.form)
            }
            
            if (response.code === 200) {
              this.$message.success('操作成功')
              this.dialogVisible = false
              this.loadCategories()
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    async handleDelete(row) {
      this.$confirm('确定要删除该分类吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/api/category/${row.id}`)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadCategories()
          }
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    indexMethod(index) {
      return index + 1
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

.el-table {
  margin-top: 20px;
}
</style>