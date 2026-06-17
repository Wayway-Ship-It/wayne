<template>
  <div class="outbound-list">
    <el-card>
      <div slot="header" class="header">
        <span>出库管理</span>
        <el-button type="primary" @click="showAddDialog" icon="el-icon-plus">申请出库</el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="单号/配件名称/编码"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" :value="0"></el-option>
            <el-option label="已审批" :value="1"></el-option>
            <el-option label="已完成" :value="2"></el-option>
            <el-option label="已拒绝" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="orderNo" label="出库单号" width="180"></el-table-column>
        <el-table-column label="配件信息" width="200">
          <template slot-scope="scope">
            {{ scope.row.partCode }} {{ scope.row.partName }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="出库数量" width="100"></el-table-column>
        <el-table-column prop="type" label="出库方式" width="120">
          <template slot-scope="scope">
            <el-tag :type="getTypeColor(scope.row.type)">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiver" label="收货人" width="100"></el-table-column>
        <el-table-column prop="receiverPhone" label="收货人电话" width="130"></el-table-column>
        <el-table-column prop="applicant" label="申请人" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0 && isAdmin" size="small" type="success" @click="handleApprove(scope.row)">审批</el-button>
            <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" v-if="isAdmin">删除</el-button>
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

    <el-dialog title="申请出库" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="出库方式" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="XSCK">销售出库</el-radio>
            <el-radio label="DBCK">调拨出库</el-radio>
            <el-radio label="BFCK">报废出库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择客户" prop="customerId" v-if="form.type === 'XSCK' || form.type === 'DBCK'">
          <div class="select-with-button">
            <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 75%;" filterable @change="onCustomerChange">
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddCustomerDialog" style="width: 23%; margin-left: 2%;">新增客户</el-button>
          </div>
        </el-form-item>
        <el-form-item label="选择配件" prop="partId">
          <el-select v-model="form.partId" placeholder="请选择配件" style="width: 100%;" filterable @change="onPartChange">
            <el-option v-for="item in partList" :key="item.id" :label="`${item.partName} (${item.partCode})`" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="配件编码">
          <el-input v-model="form.partCode" disabled></el-input>
        </el-form-item>
        <el-form-item label="配件名称">
          <el-input v-model="form.partName" disabled></el-input>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input v-model="currentStock" disabled></el-input>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;"
            :tooltip="`当前库存：${currentStock}`">
            <template slot="append">
              <span style="color: #909399; font-size: 12px; margin-right: 10px;">
                库存: {{ currentStock }}
              </span>
            </template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver"></el-input>
        </el-form-item>
        <el-form-item label="收货人电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone"></el-input>
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input type="textarea" v-model="form.receiverAddress"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交申请</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审批出库单" :visible.sync="approveDialogVisible" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="出库单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="配件名称">{{ currentRow.partName }}</el-descriptions-item>
        <el-descriptions-item label="出库数量">{{ currentRow.quantity }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.applicant }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentRow.receiver }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="approveForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.approvalRemark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject" :loading="approveLoading">拒绝</el-button>
        <el-button type="success" @click="handleAgree" :loading="approveLoading">同意</el-button>
      </div>
    </el-dialog>

    <el-dialog title="新增客户" :visible.sync="customerDialogVisible" width="600px">
      <el-form :model="customerForm" :rules="customerRules" ref="customerForm" label-width="100px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="customerForm.name"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="customerForm.address"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="customerForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="customerForm.contactPerson"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="customerForm.email"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="customerForm.description"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="customerForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="customerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddCustomer" :loading="customerSubmitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'OutboundList',
  computed: {
    ...mapGetters(['isAdmin'])
  },
  data() {
    return {
      searchForm: {
        pageNum: 1,
        pageSize: 10,
        keyword: '',
        status: null
      },
      tableData: [],
      total: 0,
      dialogVisible: false,
      approveDialogVisible: false,
      submitLoading: false,
      approveLoading: false,
      partList: [],
      customerList: [],
      currentStock: 0,
      currentRow: {},
      form: {
        type: 'XSCK',
        partId: null,
        customerId: null,
        partCode: '',
        partName: '',
        quantity: 1,
        receiver: '',
        receiverPhone: '',
        receiverAddress: '',
        remark: ''
      },
      approveForm: {
        approvalRemark: ''
      },
      rules: {
        partId: [{ required: true, message: '请选择配件', trigger: 'change' }],
        customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }]
      },
      customerDialogVisible: false,
      customerSubmitLoading: false,
      customerForm: {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      },
      customerRules: {
        name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadPartList()
    this.loadCustomerList()
  },
  methods: {
    getStatusType(status) {
      const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = { 0: '待审批', 1: '已审批', 2: '已完成', 3: '已拒绝' }
      return texts[status] || '未知'
    },
    getTypeColor(type) {
      const colors = { 'XSCK': 'primary', 'DBCK': 'warning', 'BFCK': 'danger' }
      return colors[type] || 'info'
    },
    getTypeText(type) {
      const texts = { 'XSCK': '销售出库', 'DBCK': '调拨出库', 'BFCK': '报废出库' }
      return texts[type] || '未知'
    },
    handleDelete(row) {
      this.$confirm('确定要删除该出库单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/api/outbound/${row.id}`)
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
    async loadData() {
      try {
        const res = await this.$axios.get('/api/outbound/list', { params: this.searchForm })
        console.log('Response:', res)
        if (res.code === 200) {
          console.log('Records:', res.data.records)
          console.log('Total:', res.data.total)
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$message.error(res.message)
        }
      } catch (error) {
        console.error('Error:', error)
        this.$message.error('加载数据失败')
      }
    },
    async loadPartList() {
      try {
        const res = await this.$axios.get('/api/part/list', { params: { pageNum: 1, pageSize: 1000 } })
        this.partList = res.data.records
      } catch (error) {
        this.$message.error('加载配件列表失败')
      }
    },
    async loadCustomerList() {
      try {
        const res = await this.$axios.get('/api/customer/list', { params: { page: 1, size: 1000 } })
        if (res.code === 200) {
          this.customerList = res.data.records
        }
      } catch (error) {
        this.$message.error('加载客户列表失败')
      }
    },
    resetSearch() {
      this.searchForm = {
        pageNum: 1,
        pageSize: 10,
        keyword: '',
        status: null
      }
      this.loadData()
    },
    showAddDialog() {
      this.form = {
        type: 'XSCK',
        partId: null,
        customerId: null,
        partCode: '',
        partName: '',
        quantity: 1,
        receiver: '',
        receiverPhone: '',
        receiverAddress: '',
        remark: ''
      }
      this.currentStock = 0
      this.dialogVisible = true
    },
    showAddCustomerDialog() {
      this.customerForm = {
        id: null,
        name: '',
        address: '',
        phone: '',
        contactPerson: '',
        email: '',
        description: '',
        status: 1
      }
      this.customerDialogVisible = true
    },
    async onPartChange(partId) {
      const part = this.partList.find(p => p.id === partId)
      if (part) {
        this.form.partCode = part.partCode
        this.form.partName = part.partName
        try {
          const res = await this.$axios.get(`/api/stock/part/${partId}`)
          if (res.code === 200 && res.data) {
            this.currentStock = res.data.quantity
          } else {
            this.currentStock = 0
          }
        } catch (error) {
          this.currentStock = 0
        }
      }
    },
    onCustomerChange(customerId) {
      const customer = this.customerList.find(c => c.id === customerId)
      if (customer) {
        this.form.receiver = customer.contactPerson
        this.form.receiverPhone = customer.phone
        this.form.receiverAddress = customer.address
      }
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          // 检查库存
          if (this.form.quantity > this.currentStock) {
            this.$message.error('库存不足，无法提交出库申请')
            return
          }
          this.submitLoading = true
          this.form.applicant = this.$store.state.user.realName
          try {
            const res = await this.$axios.post('/api/outbound/create', this.form)
            if (res.code === 200) {
              this.$message.success('申请成功，等待审批')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error(error.response?.data?.message || '操作失败')
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    handleApprove(row) {
      this.currentRow = row
      this.approveForm.approvalRemark = ''
      this.approveDialogVisible = true
    },
    async doApprove(approveStatus) {
      this.approveLoading = true
      try {
        const res = await this.$axios.post('/api/outbound/approve', {
          id: this.currentRow.id,
          approveStatus: approveStatus,
          approvalRemark: this.approveForm.approvalRemark,
          approver: this.$store.state.user.realName
        })
        if (res.code === 200) {
          this.$message.success('审批成功')
          this.approveDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.message)
        }
      } catch (error) {
        this.$message.error(error.response?.data?.message || '审批失败')
      } finally {
        this.approveLoading = false
      }
    },
    handleAgree() {
      this.doApprove(1)
    },
    handleReject() {
      this.doApprove(3)
    },
    handleDetail(row) {
      this.currentRow = row
      this.$alert(`
        <div style="text-align: left;">
          <p><strong>出库单号：</strong>${row.orderNo}</p>
          <p><strong>配件名称：</strong>${row.partName}</p>
          <p><strong>出库数量：</strong>${row.quantity}</p>
          <p><strong>收货人：</strong>${row.receiver}</p>
          <p><strong>收货人电话：</strong>${row.receiverPhone || '-'}</p>
          <p><strong>收货人地址：</strong>${row.receiverAddress || '-'}</p>
          <p><strong>申请人：</strong>${row.applicant}</p>
          <p><strong>审批人：</strong>${row.approver || '-'}</p>
          <p><strong>审批时间：</strong>${row.approvalTime || '-'}</p>
          <p><strong>审批备注：</strong>${row.approvalRemark || '-'}</p>
          <p><strong>备注：</strong>${row.remark || '-'}</p>
        </div>
      `, '出库单详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      })
    },
    async handleAddCustomer() {
      this.$refs.customerForm.validate(async valid => {
        if (valid) {
          this.customerSubmitLoading = true
          try {
            const res = await this.$axios.post('/api/customer/save', this.customerForm)
            if (res.code === 200) {
              this.$message.success('客户添加成功')
              this.customerDialogVisible = false
              // 重新加载客户列表
              await this.loadCustomerList()
            } else {
              this.$message.error(res.message)
            }
          } catch (error) {
            this.$message.error('操作失败')
          } finally {
            this.customerSubmitLoading = false
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
    }
  },
  created() {
    this.loadData()
    this.loadPartList()
    this.loadCustomerList()
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
</style>
