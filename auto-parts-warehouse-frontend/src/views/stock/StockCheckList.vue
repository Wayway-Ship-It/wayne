<template>
  <div class="stock-check-list">
    <el-card>
      <div slot="header" class="header">
        <span>库存盘点</span>
        <el-button type="primary" @click="showAddDialog" icon="el-icon-plus">创建盘点单</el-button>
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
        <el-table-column prop="checkNo" label="盘点单号" width="180"></el-table-column>
        <el-table-column prop="partCode" label="配件编码" width="120"></el-table-column>
        <el-table-column prop="partName" label="配件名称" width="150"></el-table-column>
        <el-table-column prop="systemQuantity" label="系统库存" width="100"></el-table-column>
        <el-table-column prop="actualQuantity" label="实际库存" width="100"></el-table-column>
        <el-table-column prop="difference" label="差异" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.difference > 0 ? '#67c23a' : scope.row.difference < 0 ? '#f56c6c' : '#909399' }">
              {{ scope.row.difference }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="checkLocation" label="盘点位置" width="120"></el-table-column>
        <el-table-column prop="checker" label="盘点人" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已完成' : '盘点中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="确认状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.confirmStatus === 1 ? 'success' : scope.row.confirmStatus === 0 ? 'warning' : 'info'">
              {{ scope.row.confirmStatus === 1 ? '已确认' : scope.row.confirmStatus === 0 ? '待确认' : '无需确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handleComplete(scope.row)">完成盘点</el-button>
            <el-button v-if="scope.row.status === 0 && scope.row.confirmStatus === 0 && $store.state.user.role === 'ADMIN'" size="small" type="success" @click="handleConfirm(scope.row)">确认差异</el-button>
            <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-if="$store.state.user.role === 'ADMIN'" size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog title="创建盘点单" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="盘点类型" prop="checkType">
          <el-radio-group v-model="form.checkType" @change="onCheckTypeChange">
            <el-radio label="regular">定期盘点</el-radio>
            <el-radio label="cycle">循环盘点</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 定期盘点选项 -->
        <el-form-item v-if="form.checkType === 'regular'" label="盘点周期">
          <el-select v-model="form.period" placeholder="请选择盘点周期" style="width: 100%;">
            <el-option label="每月" value="monthly"></el-option>
            <el-option label="每季度" value="quarterly"></el-option>
            <el-option label="年末" value="yearly"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 定期盘点提示 -->
        <el-alert v-if="form.checkType === 'regular'" type="info" :closable="false" style="margin-bottom: 20px;">
          定期盘点将一次性为所有配件创建盘点单，盘点单将按配件分类显示
        </el-alert>
        
        <!-- 循环盘点选项 -->
        <template v-if="form.checkType === 'cycle'">
          <el-form-item label="配件分类">
            <el-select v-model="form.category" placeholder="请选择配件分类" style="width: 100%;" @change="filterPartsByCategory">
              <el-option v-for="category in categoryList" :key="category.name" :label="category.name" :value="category.name"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="选择配件" prop="partId">
            <el-select v-model="form.partId" placeholder="请选择配件" style="width: 100%;" filterable @change="onPartChange">
              <el-option v-for="item in filteredPartList" :key="item.id" :label="`${item.partName} (${item.partCode})`" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="配件编码">
            <el-input v-model="form.partCode" disabled></el-input>
          </el-form-item>
          <el-form-item label="配件名称">
            <el-input v-model="form.partName" disabled></el-input>
          </el-form-item>
          <el-form-item label="系统库存">
            <el-input v-model="form.systemQuantity" disabled></el-input>
          </el-form-item>
          <el-form-item label="盘点位置" prop="checkLocation">
            <el-input v-model="form.checkLocation"></el-input>
          </el-form-item>
        </template>
        
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注信息（定期盘点时将应用于所有盘点单）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ form.checkType === 'regular' ? '创建所有盘点单' : '创建' }}
        </el-button>
      </div>
    </el-dialog>

    <el-dialog title="完成盘点" :visible.sync="completeDialogVisible" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="盘点单号">{{ currentRow.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="配件名称">{{ currentRow.partName }}</el-descriptions-item>
        <el-descriptions-item label="系统库存">{{ currentRow.systemQuantity }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 配件汇总信息 -->
      <div v-if="currentRow.partsSummary" style="margin-top: 20px;">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>配件分类汇总</span>
          </div>
          <el-scrollbar style="height: 300px;">
            <pre style="white-space: pre-wrap; font-family: 'Courier New', monospace; font-size: 14px; line-height: 1.5;">{{ currentRow.partsSummary }}</pre>
          </el-scrollbar>
        </el-card>
      </div>
      
      <el-form :model="completeForm" :rules="completeRules" ref="completeForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="实际库存" prop="actualQuantity">
          <el-input-number v-model="completeForm.actualQuantity" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="completeForm.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doComplete" :loading="completeLoading">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StockCheckList',
  data() {
    return {
      searchForm: {
        pageNum: 1,
        pageSize: 10,
        keyword: ''
      },
      tableData: [],
      total: 0,
      dialogVisible: false,
      completeDialogVisible: false,
      submitLoading: false,
      completeLoading: false,
      partList: [],
      currentRow: {},
      form: {
        partId: null,
        partCode: '',
        partName: '',
        systemQuantity: 0,
        checkLocation: '',
        remark: '',
        checkType: 'regular',  // 盘点类型：regular-定期盘点，cycle-循环盘点
        period: '',            // 盘点周期：monthly-每月，quarterly-每季度，yearly-年末
        category: ''           // 配件分类（循环盘点时使用）
      },
      categoryList: [],        // 配件分类列表
      filteredPartList: [],    // 过滤后的配件列表
      completeForm: {
        actualQuantity: 0,
        remark: ''
      },
      rules: {
        partId: [{ required: true, message: '请选择配件', trigger: 'change' }]
      },
      completeRules: {
        actualQuantity: [{ required: true, message: '请输入实际库存', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadPartList()
    this.loadCategoryList()
  },
  methods: {
    async loadData() {
      try {
        console.log('Loading stock check data with params:', this.searchForm)
        const res = await this.$axios.get('/api/stock-check/list', { params: this.searchForm })
        console.log('API response:', res)
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$message.error(`加载失败：${res.message || '未知错误'}`)
        }
      } catch (error) {
        console.error('Error loading data:', error)
        this.$message.error(`加载数据失败：${error.message || '网络错误'}`)
      }
    },
    async loadPartList() {
      try {
        console.log('Loading part list')
        const res = await this.$axios.get('/api/part/list', { params: { pageNum: 1, pageSize: 1000 } })
        console.log('Part list response:', res)
        if (res.code === 200) {
          this.partList = res.data.records
          this.filteredPartList = res.data.records
        } else {
          this.$message.error(`加载配件列表失败：${res.message || '未知错误'}`)
        }
      } catch (error) {
        console.error('Error loading part list:', error)
        this.$message.error('加载配件列表失败')
      }
    },
    // 加载配件分类列表
    async loadCategoryList() {
      try {
        console.log('Loading category list')
        const res = await this.$axios.get('/api/category/list')
        console.log('Category list response:', res)
        if (res.code === 200) {
          this.categoryList = res.data
        } else {
          this.$message.error(`加载配件分类失败：${res.message || '未知错误'}`)
        }
      } catch (error) {
        console.error('Error loading category list:', error)
        this.$message.error('加载配件分类失败')
      }
    },
    // 盘点类型改变时的处理
    onCheckTypeChange() {
      if (this.form.checkType === 'cycle') {
        // 循环盘点时，根据分类过滤配件
        this.filterPartsByCategory()
      } else {
        // 定期盘点时，显示所有配件
        this.filteredPartList = this.partList
      }
    },
    // 根据分类过滤配件
    filterPartsByCategory() {
      if (this.form.category) {
        this.filteredPartList = this.partList.filter(part => part.category === this.form.category)
      } else {
        this.filteredPartList = this.partList
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
        partId: null,
        partCode: '',
        partName: '',
        systemQuantity: 0,
        checkLocation: '',
        remark: '',
        checkType: 'regular',
        period: 'monthly',  // 默认每月盘点
        category: ''
      }
      this.filteredPartList = this.partList
      this.dialogVisible = true
    },
    async onPartChange(partId) {
      const part = this.partList.find(p => p.id === partId)
      if (part) {
        this.form.partCode = part.partCode
        this.form.partName = part.partName
        this.form.category = part.category || ''
        try {
          const res = await this.$axios.get(`/api/stock/part/${partId}`)
          if (res.code === 200 && res.data) {
            this.form.systemQuantity = res.data.quantity
            this.form.checkLocation = res.data.warehouseLocation || ''
          }
        } catch (error) {
          this.form.systemQuantity = 0
        }
      }
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          this.form.checker = this.$store.state.user.realName
          try {
            let res
            if (this.form.checkType === 'regular') {
              // 定期盘点：创建一个汇总的盘点单
              res = await this.$axios.post('/api/stock-check/batch-create', {
                checker: this.form.checker,
                remark: this.form.remark,
                period: this.form.period
              })
              if (res.code === 200) {
                this.$message.success('创建成功，已生成汇总盘点单')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(res.message)
              }
            } else {
              // 循环盘点：单个创建
              res = await this.$axios.post('/api/stock-check/create', this.form)
              if (res.code === 200) {
                this.$message.success('创建成功')
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
    handleComplete(row) {
      this.currentRow = row
      this.completeForm.actualQuantity = row.systemQuantity
      this.completeForm.remark = ''
      this.completeDialogVisible = true
    },
    async doComplete() {
      this.$refs.completeForm.validate(async valid => {
        if (valid) {
          this.completeLoading = true
          try {
            const res = await this.$axios.post('/api/stock-check/complete', {
              id: this.currentRow.id,
              actualQuantity: this.completeForm.actualQuantity,
              remark: this.completeForm.remark
            })
            if (res.code === 200) {
              this.$message.success('盘点完成')
              this.completeDialogVisible = false
              this.loadData()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.completeLoading = false
          }
        }
      })
    },
    async handleConfirm(row) {
      this.$confirm('确定要确认此盘点差异并调整库存吗？', '确认差异', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.post('/api/stock-check/confirm', {
            id: row.id,
            confirmUser: this.$store.state.user.realName
          })
          if (res.code === 200) {
            this.$message.success('确认成功')
            this.loadData()
          } else {
            this.$message.error(res.message)
          }
        } catch (error) {
          this.$message.error('操作失败')
        }
      }).catch(() => {
        // 取消确认
      })
    },
    handleDetail(row) {
      this.$alert(`
        <div style="text-align: left;">
          <p><strong>盘点单号：</strong>${row.checkNo}</p>
          <p><strong>配件名称：</strong>${row.partName}</p>
          <p><strong>系统库存：</strong>${row.systemQuantity}</p>
          <p><strong>实际库存：</strong>${row.actualQuantity !== null ? row.actualQuantity : '-'}</p>
          <p><strong>差异：</strong>${row.difference !== null ? row.difference : '-'}</p>
          <p><strong>盘点位置：</strong>${row.checkLocation || '-'}</p>
          <p><strong>盘点人：</strong>${row.checker}</p>
          <p><strong>确认状态：</strong>${row.confirmStatus === 1 ? '已确认' : row.confirmStatus === 0 ? '待确认' : '无需确认'}</p>
          <p><strong>确认人：</strong>${row.confirmUser || '-'}</p>
          <p><strong>确认时间：</strong>${row.confirmTime || '-'}</p>
          <p><strong>备注：</strong>${row.remark || '-'}</p>
        </div>
      `, '盘点单详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      })
    },
    // 删除盘点单
    handleDelete(row) {
      this.$confirm('确定要删除此盘点单吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/api/stock-check/delete/${row.id}`)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.message)
          }
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
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

/* 操作按钮样式 */
.el-table__column--fixed-right .el-button {
  margin-right: 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-size: 12px;
  padding: 4px 12px;
}

.el-table__column--fixed-right .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 按钮间距 */
.el-table__column--fixed-right .el-button + .el-button {
  margin-left: 8px;
  margin-right: 0;
}

/* 确保按钮在表格中垂直居中 */
.el-table__row .el-button {
  vertical-align: middle;
}
</style>
