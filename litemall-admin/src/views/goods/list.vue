<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.goodsId" clearable class="filter-item" style="width: 160px;" placeholder="请输入商品ID" />
      <el-input v-model="listQuery.goodsSn" clearable class="filter-item" style="width: 160px;" placeholder="请输入商品编号" />
      <el-input v-model="listQuery.name" clearable class="filter-item" style="width: 160px;" placeholder="请输入商品名称" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button class="filter-item" type="primary" icon="el-icon-edit" @click="handleCreate">添加</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button>
    </div>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" element-loading-text="正在查询中。。。" border fit highlight-current-row @expand-change="expandChange">

      <el-table-column type="expand">
        <template slot-scope="props">
          <el-form v-loading="productLoading" label-position="left" class="table-expand">
            <el-form-item label="商品编号">
              <span>{{ props.row.goodsSn }}</span>
            </el-form-item>
            <el-form-item label="宣传画廊">
              <img v-for="pic in props.row.gallery" :key="pic" :src="pic" class="gallery">
            </el-form-item>
            <el-form-item label="商品介绍">
              <span>{{ props.row.brief }}</span>
            </el-form-item>
            <el-form-item label="商品单位">
              <span>{{ props.row.unit }}</span>
            </el-form-item>
            <el-form-item label="商品库存">
              <!--<span>{{ props.row.products }}</span>-->
              <el-table :data="props.row.products" border style="width: 80%">
                <el-table-column prop="specifications" label="货品规格">
                  <template slot-scope="scope">
                    <el-tag v-for="tag in scope.row.specifications" :key="tag">
                      {{ tag }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="price" label="货品售价" />
                <!--TODO: 如何隐藏一列-->
                <el-table-column prop="basePrice" label="基础价格" />
                <el-table-column prop="number" label="货品数量" />
              </el-table>
            </el-form-item>
            <el-form-item label="关键字">
              <span>{{ props.row.keywords }}</span>
            </el-form-item>
            <el-form-item label="类目ID">
              <span>{{ props.row.categoryId }}</span>
            </el-form-item>
            <el-form-item label="品牌商ID">
              <span>{{ props.row.brandId }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>

      <el-table-column align="center" label="商品ID" prop="id" />

      <el-table-column align="center" min-width="100" label="名称" prop="name" />

      <el-table-column align="center" property="iconUrl" label="图片">
        <template slot-scope="scope">
          <img :src="scope.row.picUrl" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" property="iconUrl" label="分享图">
        <template slot-scope="scope">
          <img :src="scope.row.shareUrl" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" label="详情" prop="detail">
        <template slot-scope="scope">
          <el-dialog :visible.sync="detailDialogVisible" title="商品详情">
            <div class="goods-detail-box" v-html="goodsDetail" />
          </el-dialog>
          <el-button type="primary" size="mini" @click="showDetail(scope.row.detail)">查看</el-button>
        </template>
      </el-table-column>

      <el-table-column align="center" label="市场售价" prop="counterPrice" />

      <el-table-column align="center" label="当前价格" prop="retailPrice" />

      <el-table-column align="center" label="是否新品" prop="isNew">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isNew ? 'success' : 'error' ">{{ scope.row.isNew ? '新品' : '非新品' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="是否热品" prop="isHot">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isHot ? 'success' : 'error' ">{{ scope.row.isHot ? '热品' : '非热品' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="是否在售" prop="isOnSale">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isOnSale ? 'success' : 'error' ">{{ scope.row.isOnSale ? '在售' : '未售' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="220" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['POST /admin/goods/update']" type="primary" size="mini" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button v-permission="['POST /admin/goods/delete']" type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          <el-button v-permission="['POST /admin/agent/dispatch']" type="primary" size="mini" @click="handleDispatchGoods(scope.row)">派货</el-button>
        </template>

      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <el-tooltip placement="top" content="返回顶部">
      <back-to-top :visibility-height="100" />
    </el-tooltip>

    <el-dialog :visible.sync="dispatchDialogVisible" title="派货" width="800">
      <el-table :data="productsInTable" border fit highlight-current-row>
        <el-table-column property="value" label="货品规格">
          <template slot-scope="scope">
            <el-tag v-for="tag in scope.row.specifications" :key="tag">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column property="price" label="货品售价" />
        <el-table-column property="basePrice" label="基础价" />
        <el-table-column property="dispatchPrice" label="派货价" />
        <el-table-column property="number" label="货品数量" />
        <el-table-column property="dispatchNumber" label="派货量" />
        <el-table-column property="agentName" label="运营商" />
        <el-table-column align="center" label="操作" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="showEditProductDialog(scope.row, scope.$index)">设置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="dispatchGoods">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="editDispatchDialogVisible" title="编辑">
      <el-form ref="productForm" :model="productForm" :rules="rules" style="width: 400px; margin-left:50px;" status-icon label-position="left" label-width="100px" @validate="formValidate">
        <!--<el-form-item label="货品规格列" prop="specifications">
          <el-tag v-for="tag in productForm.specifications" :key="tag">
            {{ tag }}
          </el-tag>
        </el-form-item>-->
        <el-form-item label="货品售价" prop="price">
          <span>{{ productForm.price }}</span>
        </el-form-item>
        <el-form-item label="基础价" prop="basePrice">
          <span>{{ productForm.basePrice }}</span>
        </el-form-item>
        <el-form-item label="派货价" prop="dispatchPrice">
          <el-input v-model="productForm.dispatchPrice" type="number" :max="productForm.price" min="0" />
        </el-form-item>
        <el-form-item label="货品数量" prop="number">
          <span>{{ productForm.number }}</span>
        </el-form-item>
        <el-form-item label="派货量" prop="dispatchNumber">
          <el-input v-model="productForm.dispatchNumber" type="number" :max="productForm.number" min="0" />
        </el-form-item>
        <!--<el-form-item label="货品图片" prop="url">
          <img v-if="productForm.url" :src="productForm.url" class="avatar" width="100px">
          <i v-else class="el-icon-plus avatar-uploader-icon" />
        </el-form-item>-->
        <el-form-item label="运营商" prop="agentName">
          <template>
            <el-select v-model="selectedAgent" value-key="id">
              <el-option
                v-for="item in agents"
                :key="item.id"
                :label="item.username"
                :value="item"
              />
            </el-select>
          </template>

        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelProductEdit()">取消</el-button>
        <el-button type="primary" @click="handleProductEdit()">确定</el-button>
      </div>
    </el-dialog>

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
import { detailGoods, listGoods, deleteGoods } from '@/api/goods'
import { dispatchGoods } from '@/api/goodsProductAgent'
import { listAdminOfMine } from '@/api/admin'
import BackToTop from '@/components/BackToTop'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination
import { MessageBox } from 'element-ui'

export default {
  name: 'GoodsList',
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
        goodsSn: undefined,
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
    expandChange: function(row, expandedRows) {
      this.initProducts(row)
    },
    getList() {
      this.listLoading = true
      listGoods(this.listQuery).then(response => {
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
    handleCreate() {
      this.$router.push({ path: '/goods/create' })
    },
    handleUpdate(row) {
      this.$router.push({ path: '/goods/edit', query: { id: row.id }})
    },
    showDetail(detail) {
      this.goodsDetail = detail
      this.detailDialogVisible = true
    },
    handleDelete(row) {
      deleteGoods(row).then(response => {
        this.$notify.success({
          title: '成功',
          message: '删除成功'
        })
        const index = this.list.indexOf(row)
        this.list.splice(index, 1)
      }).catch(response => {
        this.$notify.error({
          title: '失败',
          message: response.data.errmsg
        })
      })
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
    },
    initProducts(row) {
      if (!row.products) {
        this.productLoading = true
        detailGoods(row.id).then(response => {
          row.products = response.data.data.products
          this.productsInTable = row.products
          this.productLoading = false
        })
      } else {
        this.productsInTable = row.products
      }
    },
    dispatchGoods() {
      const goodsToDispatch = []
      for (const item of this.productsInTable) {
        if (item.agentId) {
          item.goodsProductId = item.id
          goodsToDispatch.push(item)
        }
      }
      if (goodsToDispatch.length > 0) {
        dispatchGoods(goodsToDispatch).then(res => {
          this.dispatchDialogVisible = false
          this.$notify.success({
            title: '成功',
            message: '派货成功'
          })
          this.$router.replace({ path: '/goods/list' })
        })
          .catch(response => {
            MessageBox.alert('业务错误：' + response.data.errmsg, '警告', {
              confirmButtonText: '确定',
              type: 'error'
            })
          })
      } else {
        MessageBox.alert('请先设置派货信息', '提示', {
          confirmButtonText: '确定',
          type: 'error'
        })
      }
    },
    showEditProductDialog(row, index) {
      this.editDispatchDialogVisible = true
      this.productForm = JSON.parse(JSON.stringify(this.productsInTable[index]))

      this.rowIndex = index
      this.formIsValid = true
    },
    handleProductEdit() {
      this.$refs['productForm'].validate((valid) => {
        if (valid) {
          this.editDispatchDialogVisible = false
          this.productForm.agentId = this.selectedAgent.id
          this.productForm.agentName = this.selectedAgent.username
          this.$set(this.productsInTable, this.rowIndex, this.productForm)
        } else {
          return
        }
      })
    },
    cancelProductEdit() {
      if (!this.formIsValid) {
        this.$refs['productForm'].resetFields()
      }
      this.editDispatchDialogVisible = false
    },
    formValidate(prop, isValid, errorMsg) {
      if (!isValid) {
        this.formIsValid = false
      }
    }

  }
}
</script>
