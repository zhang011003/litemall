<template>
  <div class="app-container">
    <el-form
      ref="dataForm"
      :rules="rules"
      :model="dataForm"
      status-icon
      label-width="300px"
    >
      <el-tabs tab-position="left">
        <el-tab-pane label="首页配置">
          <el-form-item label="新品首发栏目商品显示数量" prop="litemall_wx_index_new">
            <el-input v-model="dataForm.litemall_wx_index_new" />
          </el-form-item>
          <el-form-item label="人气推荐栏目商品显示数量" prop="litemall_wx_index_hot">
            <el-input v-model="dataForm.litemall_wx_index_hot" />
          </el-form-item>
          <el-form-item label="品牌制造商直供栏目品牌商显示数量" prop="litemall_wx_index_brand">
            <el-input v-model="dataForm.litemall_wx_index_brand" />
          </el-form-item>
          <el-form-item label="专题精选栏目显示数量" prop="litemall_wx_index_topic">
            <el-input v-model="dataForm.litemall_wx_index_topic" />
          </el-form-item>
          <el-form-item label="分类栏目显示数量" prop="litemall_wx_catlog_list">
            <el-input v-model="dataForm.litemall_wx_catlog_list" />
          </el-form-item>
          <el-form-item label="分类栏目商品显示数量" prop="litemall_wx_catlog_goods">
            <el-input v-model="dataForm.litemall_wx_catlog_goods" />
          </el-form-item>
        </el-tab-pane>
        <el-tab-pane label="支付配置">
          <el-form-item label="支付宝支付" prop="litemall_wx_pay_zfb">
            <el-switch v-model="dataForm.litemall_wx_pay_zfb" />
          </el-form-item>
          <el-form-item label="微信支付" prop="litemall_wx_pay_wx">
            <el-switch v-model="dataForm.litemall_wx_pay_wx" />
          </el-form-item>
        </el-tab-pane>
        <el-tab-pane label="其他配置">
          <el-form-item label="仅使用微信登录" prop="litemall_wx_login_only">
            <el-switch v-model="dataForm.litemall_wx_login_only" />
          </el-form-item>
          <el-form-item label="商品分享功能" prop="litemall_wx_share">
            <el-switch v-model="dataForm.litemall_wx_share" />
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
      <el-form-item>
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="update">确定</el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script>
import { listWx, updateWx } from '@/api/config'

export default {
  name: 'ConfigWx',
  data() {
    return {
      dataForm: {
        litemall_wx_index_new: 0,
        litemall_wx_index_hot: 0,
        litemall_wx_index_brand: 0,
        litemall_wx_index_topic: 0,
        litemall_wx_catlog_list: 0,
        litemall_wx_catlog_goods: 0,
        litemall_wx_share: false,
        litemall_wx_login_only: true,
        litemall_wx_pay_wx: true,
        litemall_wx_pay_zfb: false
      },
      rules: {
        litemall_wx_index_new: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        litemall_wx_index_hot: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        litemall_wx_index_brand: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        litemall_wx_index_topic: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        litemall_wx_catlog_list: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ],
        litemall_wx_catlog_goods: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.init()
  },
  methods: {
    init: function() {
      listWx().then(response => {
        this.dataForm = response.data.data
        this.dataForm.litemall_wx_share = JSON.parse(this.dataForm.litemall_wx_share)
        this.dataForm.litemall_wx_login_only = JSON.parse(this.dataForm.litemall_wx_login_only)
        this.dataForm.litemall_wx_pay_wx = JSON.parse(this.dataForm.litemall_wx_pay_wx)
        this.dataForm.litemall_wx_pay_zfb = JSON.parse(this.dataForm.litemall_wx_pay_zfb)
      })
    },
    cancel() {
      this.init()
    },
    update() {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) {
          return false
        }
        this.doUpdate()
      })
    },
    doUpdate() {
      updateWx(this.dataForm)
        .then(response => {
          this.$notify.success({
            title: '成功',
            message: '小程序配置成功'
          })
        })
        .catch(response => {
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
        })
    }
  }
}
</script>
