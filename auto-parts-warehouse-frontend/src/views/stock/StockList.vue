<template>
  <div class="stock-list">
    <el-card>
      <div slot="header" class="header">
        <span>库存管理</span>
      </div>

      <!-- 库存监控区域 -->
      <div class="stock-monitor" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 8px;">
        <h4 style="margin-bottom: 15px; color: #303133;">库存监控</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="monitor-card" style="background-color: #ecf5ff; padding: 15px; border-radius: 8px; cursor: pointer;" @click="filterLowStock">
              <div style="font-size: 14px; color: #409eff; margin-bottom: 5px;">库存不足</div>
              <div style="font-size: 24px; font-weight: bold; color: #f56c6c;">{{ lowStockCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-card" style="background-color: #f0f9eb; padding: 15px; border-radius: 8px;">
              <div style="font-size: 14px; color: #67c23a; margin-bottom: 5px;">库存正常</div>
              <div style="font-size: 24px; font-weight: bold; color: #67c23a;">{{ normalStockCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-card" style="background-color: #fdf6ec; padding: 15px; border-radius: 8px;">
              <div style="font-size: 14px; color: #e6a23c; margin-bottom: 5px;">库存总量</div>
              <div style="font-size: 24px; font-weight: bold; color: #e6a23c;">{{ totalStockQuantity }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-card" style="background-color: #f5f0ff; padding: 15px; border-radius: 8px;">
              <div style="font-size: 14px; color: #909399; margin-bottom: 5px;">配件种类</div>
              <div style="font-size: 24px; font-weight: bold; color: #909399;">{{ totalPartCount }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="配件名称/编码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
          <el-button type="warning" @click="sendStockAlert" icon="el-icon-message" :loading="alertLoading">发送预警邮件</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="partCode" label="配件编码" width="120"></el-table-column>
        <el-table-column prop="partName" label="配件名称" width="150"></el-table-column>
        <el-table-column prop="quantity" label="当前库存" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.quantity < scope.row.safeStock ? '#f56c6c' : '#67c23a' }">
              {{ scope.row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safeStock" label="安全库存" width="100"></el-table-column>
        <el-table-column prop="warehouseLocation" label="库位" width="120"></el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="120"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.quantity < scope.row.safeStock" type="danger">库存不足</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" @click="handleEdit(scope.row)">调整</el-button>
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

    <el-dialog title="调整库存" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="配件编码">
          <el-input v-model="form.partCode" disabled></el-input>
        </el-form-item>
        <el-form-item label="配件名称">
          <el-input v-model="form.partName" disabled></el-input>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input v-model="form.quantity" disabled></el-input>
        </el-form-item>
        <el-form-item label="安全库存" prop="safeStock">
          <el-input-number v-model="form.safeStock" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="库位" prop="warehouseLocation">
          <el-input v-model="form.warehouseLocation"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog title="确认删除" :visible.sync="deleteDialogVisible" width="400px">
      <p>确定要删除该库存记录吗？</p>
      <div slot="footer" class="dialog-footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StockList',
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
      deleteDialogVisible: false,
      submitLoading: false,
      alertLoading: false,
      form: {
        id: null,
        partCode: '',
        partName: '',
        quantity: 0,
        safeStock: 0,
        warehouseLocation: ''
      },
      deleteId: null,
      rules: {
        safeStock: [{ required: true, message: '请输入安全库存', trigger: 'blur' }]
      },
      // 库存监控数据
      lowStockCount: 0,         // 库存不足数量
      normalStockCount: 0,       // 库存正常数量
      totalStockQuantity: 0,     // 库存总量
      totalPartCount: 0,         // 配件种类数量
      // 筛选条件
      isFilterLowStock: false    // 是否筛选库存不足
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        // 加载当前页数据
        const params = { ...this.searchForm }
        if (this.isFilterLowStock) {
          params.lowStock = true
        }
        const res = await this.$axios.get('/api/stock/list', { params })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
        
        // 加载所有库存数据用于监控
        this.loadAllStockData()
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },
    // 筛选库存不足的配件
    filterLowStock() {
      this.isFilterLowStock = !this.isFilterLowStock
      if (this.isFilterLowStock) {
        this.$message.info('已筛选出库存不足的配件')
      } else {
        this.$message.info('已取消筛选')
      }
      this.searchForm.pageNum = 1
      this.loadData()
    },
    // 发送库存预警邮件
    async sendStockAlert() {
      this.alertLoading = true
      try {
        const res = await this.$axios.get('/api/stock-alert/check')
        if (res.code === 200) {
          this.$message.success('预警邮件发送成功')
        } else {
          this.$message.error(res.message || '发送失败')
        }
      } catch (error) {
        this.$message.error('发送失败')
      } finally {
        this.alertLoading = false
      }
    },
    // 加载所有库存数据用于监控
    async loadAllStockData() {
      try {
        const res = await this.$axios.get('/api/stock/list', { params: { pageNum: 1, pageSize: 1000 } })
        if (res.code === 200) {
          this.calculateStockMonitorData(res.data.records)
        }
      } catch (error) {
        console.error('加载库存监控数据失败', error)
      }
    },
    // 计算库存监控数据
    calculateStockMonitorData(stockData) {
      let lowStock = 0
      let normalStock = 0
      let totalQuantity = 0
      
      stockData.forEach(item => {
        if (item.quantity < item.safeStock) {
          lowStock++
        } else {
          normalStock++
        }
        totalQuantity += item.quantity
      })
      
      this.lowStockCount = lowStock
      this.normalStockCount = normalStock
      this.totalStockQuantity = totalQuantity
      this.totalPartCount = stockData.length
    },
    resetSearch() {
      this.searchForm = {
        pageNum: 1,
        pageSize: 10,
        keyword: ''
      }
      this.loadData()
    },
    handleEdit(row) {
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            const res = await this.$axios.put('/api/stock/update', this.form)
            if (res.code === 200) {
              this.$message.success('操作成功')
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
      this.deleteId = row.id
      this.deleteDialogVisible = true
    },
    async confirmDelete() {
      this.submitLoading = true
      try {
        const res = await this.$axios.delete(`/api/stock/delete/${this.deleteId}`)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.deleteDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.message)
        }
      } catch (error) {
        this.$message.error('删除失败')
      } finally {
        this.submitLoading = false
      }
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
