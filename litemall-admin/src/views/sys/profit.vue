<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.detail" clearable class="filter-item" style="width: 200px;" placeholder="请输入内容关键字" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button>
    </div>

    <div class="operator-container">
      当前账户余额：<span>&yen;{{ balance }}</span>
    </div>
    <div class="operator-container">
      <el-button class="filter-item" type="danger" @click="transferVisible = true">提现</el-button>
    </div>

    <el-tabs v-model="listQuery.type" @tab-click="handleFilter">
      <el-tab-pane label="全部" />
      <el-tab-pane label="收入" name="1" />
      <el-tab-pane label="提现" name="2" />
    </el-tabs>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" element-loading-text="正在查询中。。。" border fit highlight-current-row>
      <el-table-column align="center" label="利润类型" prop="type">
        <template slot-scope="scope">
          <el-tag :type="success">{{ scope.row.type == 1 ? '收入' : '提现' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="金额" prop="money" />
      <el-table-column align="center" label="账户余额" prop="balance" />
      <el-table-column align="center" label="详情" prop="detail" />
      <el-table-column align="center" label="状态" prop="accoutStatus">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.accountStatus" :type="success">{{ scope.row.accountStatus === 1 ? '提现中' : '提现完成' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作时间" prop="updateTime" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <el-dialog :visible.sync="transferVisible" title="提现">
      <el-form ref="transferForm" :rules="rules" :model="transferForm" status-icon label-position="left" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="金额" prop="money">
          <el-input v-model="transferForm.money" type="number" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCashOut">确定</el-button>
      </div>
    </el-dialog>

    <el-tooltip placement="top" content="返回顶部">
      <back-to-top :visibility-height="100" />
    </el-tooltip>

  </div>
</template>

<script>
import { profitHistoryList, profitList, cashOut } from '@/api/account'
import BackToTop from '@/components/BackToTop'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination
import { getToken } from '@/utils/auth'
import { listAdminOfMine } from '@/api/admin'
import { MessageBox } from 'element-ui'

export default {
  name: 'Profit',
  components: { BackToTop, Pagination },
  data() {
    const checkMoney = (rule, value, callback) => {
      if (value > this.balance) {
        return callback(new Error('金额不能大于账户余额'))
      } else if (value <= 0) {
        return callback(new Error('金额不能小于等于0'))
      } else {
        return callback()
      }
    }
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        type: undefined,
        detail: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      downloadLoading: false,
      balance: 0,
      agents: [],
      transferVisible: false,
      rules: {
        money: [
          { required: true, message: '金额不能为空', trigger: 'blur' },
          { validator: checkMoney, trigger: 'blur' }
        ]
      },
      transferForm: {
        money: 0
      }
    }
  },
  computed: {
    headers() {
      return {
        'X-Litemall-Admin-Token': getToken()
      }
    }
  },
  created() {
    this.getList()
    this.getAmount()
    // this.getAgents()
  },
  methods: {
    getList() {
      this.listLoading = true
      profitHistoryList(this.listQuery)
        .then(response => {
          this.list = response.data.data.list
          this.total = response.data.data.total
          this.listLoading = false
        })
        .catch(() => {
          this.list = []
          this.total = 0
          this.listLoading = false
        })
    },
    getAmount() {
      profitList().then(resp => {
        this.balance = resp.data.data.balance
      })
    },
    getAgents() {
      if (!this.agents || this.agents.length === 0) {
        listAdminOfMine(this.adminListQuery)
          .then(response => {
            this.agents = response.data.data.list
          })
          .catch(() => {
            this.agents = []
          })
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleCashOut() {
      this.$refs['transferForm'].validate((valid) => {
        if (!valid) {
          return
        }
        cashOut(this.transferForm)
          .then(response => {
            this.transferVisible = false
            this.getList()
            this.getAmount()
            this.$notify.success({
              title: '成功',
              message: '提交提现请求成功'
            })
          })
          .catch(response => {
            MessageBox.alert('业务错误：' + response.data.errmsg, '警告', {
              confirmButtonText: '确定',
              type: 'error'
            })
          })
      })
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = [
          '通知ID',
          '通知标题',
          '管理员ID',
          '添加时间',
          '更新时间'
        ]
        const filterVal = [
          'id',
          'title',
          'content',
          'adminId',
          'addTime',
          'updateTime'
        ]
        excel.export_json_to_excel2(tHeader, this.list, filterVal, '通知')
        this.downloadLoading = false
      })
    }
  }
}
</script>
