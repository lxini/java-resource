<template>
  <div class="app-container">
    <el-card shadow="never" class="operate-container">
      <el-button size="mini" class="btn-add" @click="handleAdd()">添加</el-button>
    </el-card>
    <div class="table-container">
      <el-table ref="resourceCategoryTable"
                :data="list"
                style="width: 100%;"
                v-loading="listLoading" border>
        <el-table-column label="编号" width="100" align="center">
          <template slot-scope="scope">{{scope.row.id}}</template>
        </el-table-column>
        <el-table-column label="名称" align="center">
          <template slot-scope="scope">{{scope.row.name}}</template>
        </el-table-column>
        <el-table-column label="创建时间" align="center">
          <template slot-scope="scope">{{scope.row.createdTime | formatDateTime}}</template>
        </el-table-column>
        <el-table-column label="排序" align="center">
          <template slot-scope="scope">{{scope.row.sort}}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button size="mini"
                       type="text"
                       @click="handleUpdate(scope.$index, scope.row)">编辑
            </el-button>
            <el-button size="mini"
                       type="text"
                       @click="handleDelete(scope.$index, scope.row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      title="添加分类"
      :visible.sync="dialogVisible"
      width="40%">
      <el-form :model="resourceCategory"
               ref="resourceCategoryForm"
               label-width="150px" size="small">
        <el-form-item label="名称：">
          <el-input v-model="resourceCategory.name" style="width: 250px"></el-input>
        </el-form-item>
        <el-form-item label="排序：">
          <el-input v-model="resourceCategory.sort" style="width: 250px"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="small">取 消</el-button>
        <el-button type="primary" @click="handleDialogConfirm()" size="small">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { listAllCate, saveOrderUpdate, deleteResourceCategory } from '@/services/resourceCategory'
import { formatDate } from '@/utils/date'
const defaultResourceCategory = {
  name: null,
  sort: 0
}
export default {
  name: 'resourceCategoryList',
  title: '资源分类',
  data () {
    return {
      list: null,
      listLoading: false,
      dialogVisible: false,
      isEdit: false,
      resourceCategory: Object.assign({}, defaultResourceCategory)
    }
  },
  created () {
    this.getList()
  },
  filters: {
    formatDateTime (time) {
      if (time == null || time === '') {
        return 'N/A'
      }
      const date = new Date(time)
      return formatDate(date, 'yyyy-MM-dd hh:mm:ss')
    }
  },
  methods: {
    handleAdd () {
      this.dialogVisible = true
      this.isEdit = false
      this.resourceCategory = Object.assign({}, defaultResourceCategory)
    },
    handleUpdate (index, row) {
      this.dialogVisible = true
      this.isEdit = true
      this.resourceCategory = Object.assign({}, row)
    },
    handleDelete (index, row) {
      this.$confirm('是否要删除该分类?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteResourceCategory(row.id).then(response => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getList()
        })
      })
    },
    handleDialogConfirm () {
      if (this.isEdit) {
        saveOrderUpdate(this.resourceCategory).then(response => {
          this.$message({
            message: '修改成功！',
            type: 'success'
          })
          this.dialogVisible = false
          this.getList()
        })
      } else {
        saveOrderUpdate(this.resourceCategory).then(response => {
          this.$message({
            message: '添加成功！',
            type: 'success'
          })
          this.dialogVisible = false
          this.getList()
        })
      }
    },
    getList () {
      this.listLoading = true
      listAllCate({}).then(response => {
        this.listLoading = false
        this.list = response.data
      })
    }
  }
}
</script>
<style>
</style>
