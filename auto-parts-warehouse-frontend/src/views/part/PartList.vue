<template>
  <div class="part-list">
    <el-card>
      <div slot="header" class="header">
        <span>配件信息管理</span>
        <el-button type="primary" @click="showAddDialog" icon="el-icon-plus">新增配件</el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="配件名称/编码/品牌"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="partCode" label="配件编码" width="120"></el-table-column>
        <el-table-column prop="partName" label="配件名称" width="150"></el-table-column>
        <el-table-column prop="category" label="分类" width="100"></el-table-column>
        <el-table-column prop="brand" label="品牌" width="100"></el-table-column>
        <el-table-column prop="model" label="型号" width="120"></el-table-column>
        <el-table-column prop="specification" label="规格" width="120"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="searchForm.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="searchForm.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="配件编码" prop="partCode">
          <el-input v-model="form.partCode" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="配件名称" prop="partName">
          <el-input v-model="form.partName"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width: 100%;">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="form.brand"></el-input>
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model"></el-input>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification"></el-input>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="制造商" prop="manufacturer">
          <el-input v-model="form.manufacturer"></el-input>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="form.supplier"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
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
  name: 'PartList',
  data() {
    return {
      searchForm: {
        pageNum: 1,
        pageSize: 10,
        keyword: '',
        category: ''
      },
      tableData: [],
      total: 0,
      categories: [],
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      submitLoading: false,
      form: {
        id: null,
        partCode: '',
        partName: '',
        category: '',
        brand: '',
        model: '',
        specification: '',
        unit: '',
        price: 0,
        manufacturer: '',
        supplier: '',
        description: '',
        status: 1
      },
      rules: {
        partCode: [{ required: true, message: '请输入配件编码', trigger: 'blur' }],
        partName: [{ required: true, message: '请输入配件名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    async loadCategories() {
      try {
        const response = await this.$axios.get('/api/category/list')
        if (response.code === 200) {
          this.categories = response.data
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    async loadData() {
      try {
        const res = await this.$axios.get('/api/part/list', { params: this.searchForm })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },
    resetSearch() {
      this.searchForm = {
        pageNum: 1,
        pageSize: 10,
        keyword: '',
        category: ''
      }
      this.loadData()
    },
    showAddDialog() {
      this.dialogTitle = '新增配件'
      this.isEdit = false
      this.form = {
        id: null,
        partCode: '',
        partName: '',
        category: '',
        brand: '',
        model: '',
        specification: '',
        unit: '',
        price: 0,
        manufacturer: '',
        supplier: '',
        description: '',
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑配件'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            if (this.isEdit) {
              const res = await this.$axios.put('/api/part/update', this.form)
              if (res.code === 200) {
                this.$message.success('操作成功')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(res.message)
              }
            } else {
              const res = await this.$axios.post('/api/part/add', this.form)
              if (res.code === 200) {
                this.$message.success('操作成功')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(res.message)
              }
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该配件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/api/part/${row.id}`)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.message)
          }
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    handleSizeChange(val) {
      this.searchForm.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.searchForm.pageNum = val
      this.loadData()
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

.search-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
