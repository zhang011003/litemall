<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.goodsId" clearable class="filter-item" style="width: 160px;" placeholder="请输入商品ID" />
      <el-input v-model="listQuery.name" clearable class="filter-item" style="width: 160px;" placeholder="请输入商品名称" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button>
    </div>

    <el-tabs v-model="listQuery.type" @tab-click="handleFilter">
      <el-tab-pane label="派货" name="1" />
      <el-tab-pane label="收货" name="2" />
    </el-tabs>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" element-loading-text="正在查询中。。。" border fit highlight-current-row @expand-change="expandChange">

      <el-table-column align="center" label="商品ID" prop="goods.id" />

      <el-table-column align="center" min-width="100" label="名称" prop="goods.name" />

      <el-table-column align="center" property="iconUrl" label="图片">
        <template slot-scope="scope">
          <img :src="scope.row.goods.picUrl" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" prop="specifications" label="货品规格">
        <template slot-scope="scope">
          <el-tag v-for="tag in scope.row.product.specifications" :key="tag">
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="派货价格" prop="history.dispatchPrice" />

      <el-table-column align="center" label="派货量" prop="history.dispatchNumber" />

      <el-table-column align="center" label="时间" prop="history.addTime" />

      <el-table-column align="center" prop="nickName" :label="listQuery.type == '1' ? '收货人' : '派货人'" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <el-tooltip placement="top" content="返回顶部">
      <back-to-top :visibility-height="100" />
    </el-tooltip>
  </div>
</template>

<style>
  .table-expand {
    font-size: 0;
  }
  .table-expand label {
    width: 100px;
    color: #99a9bf;
  }
  .table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
  }
  .gallery {
    width: 80px;
    margin-right: 10px;
  }
  .goods-detail-box img {
    width: 100%;
  }
</style>

<script>
import { dispatchHistory } from '@/api/goodsProductAgent'
import BackToTop from '@/components/BackToTop'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination

export default {
  name: 'GoodsDispatchHistoryList',
  components: { BackToTop, Pagination },
  data() {
    var checkDispatchPrice = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('派货价不能为空'))
      } else if (value > this.productForm.price) {
        return callback(new Error('派货价不能大于货品售价'))
      } else if (this.productForm.basePrice && value < this.productForm.basePrice) {
        return callback(new Error('派货价不能小于基础价'))
      } else {
        return callback()
      }
    }
    var checkDispatchNumber = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('派货量不能为空'))
      } else if (value > this.productForm.number) {
        return callback(new Error('派货量不能大于货品数量'))
      } else if (value <= 0) {
        return callback(new Error('派货量不能为0'))
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
        type: '1',
        goodsId: undefined,
        name: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      goodsDetail: '',
      detailDialogVisible: false,
      downloadLoading: false,
      dispatchDialogVisible: false,
      productLoading: true,
      productsInTable: [],
      editDispatchDialogVisible: false,
      productForm: {
        price: 0,
        basePrice: 0,
        dispatchPrice: 0,
        number: 0,
        dispatchNumber: 0,
        agentId: 0,
        agentName: null
      },
      rules: {
        dispatchPrice: [
          { required: true, message: '派货价不能为空', trigger: 'blur' },
          { validator: checkDispatchPrice, trigger: 'blur' }
        ],
        dispatchNumber: [
          { required: true, message: '派货价不能为空', trigger: 'blur' },
          { validator: checkDispatchNumber, trigger: 'blur' }
        ]
      },
      formIsValid: true,
      rowIndex: 0,
      agents: [],
      adminListQuery: {
        page: 1,
        limit: 20,
        type: 1,
        sort: 'add_time',
        order: 'desc'
      },
      selectedAgent: {}
    }
  },
  created() {
    this.getList()
    this.getAgents()
  },
  methods: {
    getList() {
      this.listLoading = true
      dispatchHistory(this.listQuery).then(response => {
        this.list = response.data.data.list
        for (const item of this.list) {
          item.productsLoading = true
        }
        this.total = response.data.data.total
        this.listLoading = false
      }).catch(() => {
        this.list = []
        this.total = 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['商品ID', '商品编号', '名称', '专柜价格', '当前价格', '是否新品', '是否热品', '是否在售', '首页主图', '宣传图片列表', '商品介绍', '详细介绍', '商品图片', '商品单位', '关键字', '类目ID', '品牌商ID']
        const filterVal = ['id', 'goodsSn', 'name', 'counterPrice', 'retailPrice', 'isNew', 'isHot', 'isOnSale', 'listPicUrl', 'gallery', 'brief', 'detail', 'picUrl', 'goodsUnit', 'keywords', 'categoryId', 'brandId']
        excel.export_json_to_excel2(tHeader, this.list, filterVal, '商品信息')
        this.downloadLoading = false
      })
    },
    handleDispatchGoods(row) {
      this.initProducts(row)
      this.dispatchDialogVisible = true
    }
  }
}
</script>
