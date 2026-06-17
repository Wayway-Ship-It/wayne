<template>
  <div class="inbound-list">
    <el-card>
      <div slot="header" class="header">
        <span>入库管理</span>
        <el-button type="primary" @click="showAddDialog" icon="el-icon-plus">创建入库单</el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="单号/配件名称/编码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="orderNo" label="入库单号" width="180"></el-table-column>
        <el-table-column label="配件信息" width="200">
          <template slot-scope="scope">
            {{ scope.row.partCode }} {{ scope.row.partName }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="入库数量" width="100"></el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template slot-scope="scope">¥{{ scope.row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="type" label="入库方式" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type === 'CGRK' ? 'primary' : 'success'">
              {{ scope.row.type === 'CGRK' ? '采购入库' : '退货入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="120"></el-table-column>
        <el-table-column prop="operator" label="操作人" width="100"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" v-if="$store.state.user.role === 'ADMIN'">删除</el-button>
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

    <el-dialog title="创建入库单" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="入库方式" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="CGRK">采购入库</el-radio>
            <el-radio label="THRK">退货入库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择配件" prop="partId">
          <div class="select-with-button">
            <el-select v-model="form.partId" placeholder="请选择配件" style="width: 75%;" filterable @change="onPartChange">
              <el-option v-for="item in partList" :key="item.id" :label="`${item.partName} (${item.partCode})`" :value="item.id"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddPartDialog" style="width: 23%; margin-left: 2%;">新增配件</el-button>
          </div>
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId" v-if="form.type === 'CGRK'">
          <div class="select-with-button">
            <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 75%;" filterable>
              <el-option v-for="item in supplierList" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddSupplierDialog" style="width: 23%; margin-left: 2%;">新增供应商</el-button>
          </div>
        </el-form-item>
        <el-form-item label="配件编码">
          <el-input v-model="form.partCode" disabled></el-input>
        </el-form-item>
        <el-form-item label="配件名称">
          <el-input v-model="form.partName" disabled></el-input>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :precision="2" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="库位" prop="warehouseLocation">
          <el-input v-model="form.warehouseLocation"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="新增配件" :visible.sync="partDialogVisible" width="600px">
      <el-form :model="partForm" :rules="partRules" ref="partForm" label-width="100px">
        <el-form-item label="配件编码" prop="partCode">
          <el-input v-model="partForm.partCode"></el-input>
        </el-form-item>
        <el-form-item label="配件名称" prop="partName">
          <el-input v-model="partForm.partName"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="partForm.category" style="width: 100%;">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="partForm.brand"></el-input>
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="partForm.model"></el-input>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="partForm.specification"></el-input>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="partForm.unit"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="partForm.price" :precision="2" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="制造商" prop="manufacturer">
          <el-input v-model="partForm.manufacturer"></el-input>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="partForm.supplier"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="partForm.description"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="partForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="partDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddPart" :loading="partSubmitLoading">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="新增供应商" :visible.sync="supplierDialogVisible" width="600px">
      <el-form :model="supplierForm" :rules="supplierRules" ref="supplierForm" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierForm.name"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="supplierForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="supplierForm.contactPerson"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="supplierForm.email"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="supplierForm.description"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="supplierForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSupplier" :loading="supplierSubmitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'InboundList',
  data() {
    return {
      searchForm: {
        pageNum: 1,
        pageSize: 10,
        keyword: ''
      },
      tableData: [],
      total: 0,
      categories: [],
      dialogVisible: false,
      submitLoading: false,
      partList: [],
      supplierList: [],
      form: {
        type: 'CGRK',
        partId: null,
        partCode: '',
        partName: '',
        supplierId: null,
        quantity: 1,
        unitPrice: 0,
        warehouseLocation: '',
        remark: ''
      },
      rules: {
        partId: [{ required: true, message: '请选择配件', trigger: 'change' }],
        supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
      },
      partDialogVisible: false,
      partSubmitLoading: false,
      partForm: {
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
      partRules: {
        partCode: [{ required: true, message: '请输入配件编码', trigger: 'blur' }],
        partName: [{ required: true, message: '请输入配件名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
      },
      supplierDialogVisible: false,
      supplierSubmitLoading: false,
      supplierForm: {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      },
      supplierRules: {
        name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadCategories()
    this.loadData()
    this.loadPartList()
    this.loadSupplierList()
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
        const res = await this.$axios.get('/api/inbound/list', { params: this.searchForm })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },
    async loadPartList() {
      try {
        const res = await this.$axios.get('/api/part/list', { params: { pageNum: 1, pageSize: 1000 } })
        if (res.code === 200) {
          this.partList = res.data.records
        }
      } catch (error) {
        this.$message.error('加载配件列表失败')
      }
    },
    async loadSupplierList() {
      try {
        const res = await this.$axios.get('/api/supplier/list', { params: { page: 1, size: 1000 } })
        if (res.code === 200) {
          this.supplierList = res.data.records
        }
      } catch (error) {
        this.$message.error('加载供应商列表失败')
      }
    },
    resetSearch() {
      this.searchForm = {
        pageNum: 1,
        pageSize: 10,
        keyword: ''
      }
      this.loadData()
    },
    showAddDialog() {
      this.form = {
        type: 'CGRK',
        partId: null,
        partCode: '',
        partName: '',
        quantity: 1,
        unitPrice: 0,
        warehouseLocation: '',
        remark: ''
      }
      this.dialogVisible = true
    },
    showAddPartDialog() {
      this.partForm = {
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
      this.partDialogVisible = true
    },
    showAddSupplierDialog() {
      this.supplierForm = {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      }
      this.supplierDialogVisible = true
    },
    onPartChange(partId) {
      const part = this.partList.find(p => p.id === partId)
      if (part) {
        this.form.partCode = part.partCode
        this.form.partName = part.partName
        this.form.unitPrice = part.price || 0
      }
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          this.form.operator = this.$store.state.user.realName
          try {
            const res = await this.$axios.post('/api/inbound/create', this.form)
            if (res.code === 200) {
              this.$message.success('入库成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    handleSizeChange(val) {
      this.searchForm.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.searchForm.pageNum = val
      this.loadData()
    },
    handleDelete(row) {
      this.$confirm('确定要删除该入库单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/api/inbound/${row.id}`)
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
    async handleAddPart() {
      this.$refs.partForm.validate(async valid => {
        if (valid) {
          this.partSubmitLoading = true
          try {
            const res = await this.$axios.post('/api/part/add', this.partForm)
            if (res.code === 200) {
              this.$message.success('配件添加成功')
              this.partDialogVisible = false
              // 重新加载配件列表
              await this.loadPartList()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.partSubmitLoading = false
          }
        }
      })
    },
    async handleAddSupplier() {
      this.$refs.supplierForm.validate(async valid => {
        if (valid) {
          this.supplierSubmitLoading = true
          try {
            const res = await this.$axios.post('/api/supplier/save', this.supplierForm)
            if (res.code === 200) {
              this.$message.success('供应商添加成功')
              this.supplierDialogVisible = false
              // 重新加载供应商列表
              await this.loadSupplierList()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.supplierSubmitLoading = false
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

.search-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

.select-with-button {
  display: flex;
  align-items: center;
}

.select-with-button .el-button {
  border-radius: 4px;
  transition: all 0.3s ease;
}

.select-with-button .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.select-with-button .el-select {
  border-radius: 4px;
  transition: all 0.3s ease;
}

.select-with-button .el-select:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
