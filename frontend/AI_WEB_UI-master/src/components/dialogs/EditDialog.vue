<template>
  <div>
    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑对话"
      width="24%"
      align-center
    >
      <template #header="{ close, titleId, titleClass }">
        <div class="my-header">
          <img src="../../assets/ai.svg" alt="" />
          <div :id="titleId" :class="titleClass">编辑对话</div>
        </div>
      </template>
      <div class="my-body">
        <p class="my-label"><i>*</i>标题</p>
        <el-input v-model="editContent" type="text" placeholder="请输入标题" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button class="cancel-btn" @click="editDialogVisible = false"
          >取消</el-button
        >
        <el-button type="primary" color="#7d4dff" @click="saveEdit"
          >保存</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, watchEffect } from "vue";
import { modHistoryApi } from "../../api/chat";
import { ElMessage,ElMessageBox } from "element-plus";
export default defineComponent({
  name: "EditDialog",
  props: {
    currentRecord: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props, { emit }) {
    const editDialogVisible = ref(false);
    const editContent = ref("");
    const showEditDialog = () => {
      editDialogVisible.value = true;
    };
    const closeEditDialog = () => {
      editDialogVisible.value = false;
    };

    // 保存编辑
    const saveEdit = async () => {
      if (!editContent.value.trim()) {
        ElMessage.warning("内容不能为空");
        return;
      }

      try {
        // 调用更新API
        const res = await modHistoryApi({
          ...props.currentRecord,
          dialogueName: editContent.value,
        });

        if (res.code == 200) {
          // 更新本地数据
          const currentRecord = {
            ...props.currentRecord,
            dialogueName: editContent.value,
          };
          emit("update:historyList", currentRecord);
          ElMessage.success("修改成功");
          editDialogVisible.value = false;
        }
      } catch (error) {
        ElMessage.error("修改失败");
      }
    };

    // 删除记录
    const handleDelete = () => {
      ElMessageBox.confirm("确定要删除该历史记录吗？", "删除确认", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            const currentRecord = {
              ...props.currentRecord,
              status: 0,
            };
            const res = await modHistoryApi(currentRecord);
            if (res.code == 200) {
              emit("delete:historyList", currentRecord);
              ElMessage.success("删除成功");
            }
          } catch (error) {
            ElMessage.error("删除失败");
          }
        })
        .catch(() => {
          // 取消删除
        });
    };

    watchEffect(() => {
      if (props.currentRecord) {
        editContent.value = props.currentRecord.dialogueName;
      }
    });

    return {
      editDialogVisible,
      editContent,
      saveEdit,
      showEditDialog,
      closeEditDialog,
      saveEdit,
      handleDelete,
    };
  },
});
</script>

<style lang="less" scoped>
// 弹窗样式
.my-header {
  display: flex;
  align-items: center;
  padding: 10px 10px;
  font-weight: 500;
  background: rgb(251, 251, 252);
  border-radius: 8px 8px 0 0;
  div {
    line-height: 32px;
  }
  img {
    width: 20px;
    margin-right: 5px;
  }
}

.my-body {
  padding: 0 24px 24px;
  .el-input {
    --el-input-inner-height: 40px;
    --el-input-border-radius: 8px;
  }
  .my-label {
    font-size: 14px;
    color: #333333;
    font-weight: 500;
    margin: 0 0 10px;
    i {
      color: #f04438;
      margin-right: 5px;
      font-style: normal;
    }
  }

  .my_input {
    margin-top: 10px;
    margin-bottom: 10px;
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px 16px;
}

:deep(.el-dialog) {
  padding: 0;
  border-radius: 8px;
  .el-dialog__headerbtn {
    background: rgb(251, 251, 252);
    border-radius: 0 8px 0 0;
  }
}

.cancel-btn.el-button {
  border-color: #f2edff;
}

.cancel-btn.el-button:hover {
  color: #7d4dff;
  background: #f2edff;
}
</style>