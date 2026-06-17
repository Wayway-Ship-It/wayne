<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content" style="cursor: pointer;" @click="goToParts">
            <div class="stat-icon" style="background-color: #409eff;">
              <i class="el-icon-goods"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.partCount }}</div>
              <div class="stat-label">配件管理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content" style="cursor: pointer;" @click="goToStock">
            <div class="stat-icon" style="background-color: #67c23a;">
              <i class="el-icon-box"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.stockCount }}</div>
              <div class="stat-label">库存总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content" style="cursor: pointer;" @click="goToInbound">
            <div class="stat-icon" style="background-color: #e6a23c;">
              <i class="el-icon-download"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inboundCount }}</div>
              <div class="stat-label">今日入库</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content" style="cursor: pointer;" @click="goToOutbound">
            <div class="stat-icon" style="background-color: #f56c6c;">
              <i class="el-icon-upload2"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.outboundCount }}</div>
              <div class="stat-label">今日出库</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>待审批出库单</span>
          </div>
          <el-table :data="pendingOutbounds" style="width: 100%" max-height="400">
            <el-table-column prop="orderNo" label="出库单号" width="150"></el-table-column>
            <el-table-column prop="partName" label="配件名称"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
            <el-table-column prop="applicant" label="申请人" width="100"></el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="160"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>库存预警</span>
          </div>
          <el-table :data="lowStockItems" style="width: 100%" max-height="400">
            <el-table-column prop="partCode" label="配件编码" width="120"></el-table-column>
            <el-table-column prop="partName" label="配件名称"></el-table-column>
            <el-table-column prop="quantity" label="当前库存" width="100"></el-table-column>
            <el-table-column prop="safeStock" label="安全库存" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag type="danger">库存不足</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        partCount: 0,
        stockCount: 0,
        inboundCount: 0,
        outboundCount: 0
      },
      pendingOutbounds: [],
      lowStockItems: []
    }
  },
  mounted() {
    this.loadStats()
    this.loadPendingOutbounds()
    this.loadLowStockItems()
  },
  methods: {
    goToParts() {
      this.$router.push('/part')
    },
    goToStock() {
      this.$router.push('/stock')
    },
    goToInbound() {
      this.$router.push('/inbound')
    },
    goToOutbound() {
      this.$router.push('/outbound')
    },
    async loadStats() {
      try {
        const [partRes, stockRes, inboundRes, outboundRes] = await Promise.all([
          this.$axios.get('/api/part/count'),
          this.$axios.get('/api/stock/list', { params: { pageNum: 1, pageSize: 1000 } }),
          this.$axios.get('/api/inbound/today'),
          this.$axios.get('/api/outbound/today')
        ])
        if (partRes.code === 200) {
          this.stats.partCount = partRes.data || 0
        }
        if (stockRes.code === 200) {
          this.stats.stockCount = stockRes.data.records.reduce((sum, item) => sum + item.quantity, 0)
        }
        if (inboundRes.code === 200) {
          this.stats.inboundCount = inboundRes.data || 0
        }
        if (outboundRes.code === 200) {
          this.stats.outboundCount = outboundRes.data || 0
        }
      } catch (error) {
        console.error('加载统计数据失败', error)
        this.stats.inboundCount = 0
        this.stats.outboundCount = 0
      }
    },
    async loadPendingOutbounds() {
      try {
        const res = await this.$axios.get('/api/outbound/list', { params: { pageNum: 1, pageSize: 10, status: 0 } })
        if (res.code === 200) {
          this.pendingOutbounds = res.data.records
        }
      } catch (error) {
        console.error('加载待审批出库单失败', error)
      }
    },
    async loadLowStockItems() {
      try {
        const res = await this.$axios.get('/api/stock/list', { params: { pageNum: 1, pageSize: 100 } })
        if (res.code === 200) {
          this.lowStockItems = res.data.records.filter(item => item.quantity < item.safeStock)
        }
      } catch (error) {
        console.error('加载库存预警失败', error)
      }
    }
  }
}
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon i {
  font-size: 30px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}
</style>
