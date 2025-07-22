<template>
  <div v-if="showHistory" class="chat-history">
    <!-- 关闭按钮 -->
    <div class="close_btn" @click="closeHandle">×</div>
    <div class="history_main">
      <div class="main_title">历史对话</div>
      <el-input
        v-model="keywords"
        size="large"
        placeholder="搜索历史会话记录，请输入会话标题、应用名称"
        @keypress.enter="searchHandle"
        clearable
        @change="changeHandle"
      >
        <template #append>
          <el-button :icon="Search" @click="searchHandle" />
        </template>
      </el-input>
      <ul
        v-infinite-scroll="load"
        class="infinite-list"
        infinite-scroll-distance="10"
        style="overflow-y: auto"
      >
        <li
          v-for="(item, index) in historyList"
          :key="item.id"
          @click="goDetail(item)"
        >
          <div
            class="time_flag"
            v-if="item.timeFlag != historyList[index - 1]?.timeFlag"
          >
            {{ item.timeFlag }}
          </div>
          <div
            class="history_item"
            @mouseover="item.showOperation = true"
            @mouseleave="item.showOperation = false"
          >
            <div class="item_title">
              <img src="../assets/1.png" alt="" /><span>量子AI智能体</span>
            </div>
            <div class="item_content">{{ item.dialogueName }}</div>
            <div class="item_time" v-if="!item.showOperation">
              {{ item.updateTime.split(" ")[0] }}
            </div>
            <div class="item_opration" v-else>
              <div class="edit_btn" @click.stop="openEditDialog(item)">
                <svg
                  width="1024"
                  height="1024"
                  viewBox="0 0 22 22"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  focusable="false"
                  class="chakra-icon css-1pkriwl"
                  id="SvgIc-u"
                  style="width: 18px; height: 18px"
                >
                  <g clip-path="url(#SvgIc-u-edit_svg__a)">
                    <path
                      fill-rule="evenodd"
                      clip-rule="evenodd"
                      d="M15.33 2.777a1.833 1.833 0 0 0-2.592 0l-9.455 9.455c-.344.344-.537.81-.537 1.296v4.797c0 .51.415.926.926.926l14.661-.001a.917.917 0 0 0 0-1.833h-7.27l8.156-8.158a1.833 1.833 0 0 0 0-2.592l-3.889-3.89Zm-6.86 14.64 9.453-9.454-3.89-3.89-9.454 9.455v3.889h3.89Z"
                      fill="#606266"
                    ></path>
                  </g>
                  <defs>
                    <clipPath id="SvgIc-u-edit_svg__a">
                      <path fill="#fff" d="M0 0h22v22H0z"></path>
                    </clipPath>
                  </defs>
                </svg>
              </div>
              <div class="edit_btn" @click.stop="handleDelete(item)">
                <svg
                  width="1024"
                  height="1024"
                  viewBox="0 0 22 22"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  focusable="false"
                  class="chakra-icon css-1er84ct"
                  id="SvgIc-v"
                  style="width: 24px; height: 24px"
                >
                  <g clip-path="url(#SvgIc-v-delete_svg__a)">
                    <path
                      d="M13.09 1.833a1.833 1.833 0 0 1 1.739 1.254l.498 1.496h3.006a.917.917 0 1 1 0 1.834l-.002.065-.795 11.131a2.75 2.75 0 0 1-2.743 2.554H7.207a2.75 2.75 0 0 1-2.743-2.554L3.67 6.481a.922.922 0 0 1-.002-.064.917.917 0 1 1 0-1.833h3.006l.498-1.496a1.833 1.833 0 0 1 1.74-1.255h4.179Zm3.407 4.584H5.503l.79 11.065a.917.917 0 0 0 .914.852h7.586a.916.916 0 0 0 .914-.852l.79-11.065Zm-7.33 2.75a.917.917 0 0 1 .91.81l.006.107v4.583a.916.916 0 0 1-1.827.107l-.006-.107v-4.583a.917.917 0 0 1 .917-.917Zm3.666 0a.917.917 0 0 1 .917.917v4.583a.917.917 0 0 1-1.833 0v-4.583a.916.916 0 0 1 .916-.917Zm.257-5.5H8.91l-.305.917h4.79l-.305-.917Z"
                      fill="#606266"
                    ></path>
                  </g>
                  <defs>
                    <clipPath id="SvgIc-v-delete_svg__a">
                      <path fill="#fff" d="M0 0h22v22H0z"></path>
                    </clipPath>
                  </defs>
                </svg>
              </div>
            </div>
          </div>
        </li>
      </ul>
    </div>
    <!-- 编辑弹窗 -->
    <!-- <EditDialog
      ref="editDialog"
      :currentRecord="currentRecord"
      @update:historyList="saveEdit"
      @delete:historyList="handleDelete"
    ></EditDialog> -->
    <EditDialog
      ref="editDialog"
      :currentRecord="currentRecord"
      @update:historyList="saveEdit"
      @delete:historyList="deleteHistory"
    ></EditDialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import { queryHistoryApi } from "../api/chat";
import { formatDateRange } from "../utils/utils";
import EditDialog from "./dialogs/EditDialog.vue";

export default defineComponent({
  name: "ChatHistory",
  components: {
    Search,
    EditDialog,
  },
  setup(props, { emit }) {
    const showHistory = ref(false);
    // 查询记录
    const pageNum = ref<number>(1);
    const pageSize = ref<number>(10);
    const total = ref<number>(0);
    const keywords = ref<string>("");
    const historyList = ref<any[]>([]);

    // 显示操作功能
    const showOperation = ref(false);
    // 编辑历史记录
    const editDialogVisible = ref(false);
    const editContent = ref("");
    const currentRecord = ref<any | null>(null);
    const editDialog = ref<any>(null);

    // 查询历史记录
    const queryHistory = async () => {
      let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        search: keywords.value,
      };
      const res: any = await queryHistoryApi(params);
      if (res.code == 200) {
        let tempArr = res.data?.content?.map((item: any) => {
          return {
            ...item,
            timeFlag: formatDateRange(item.createTime),
            showOperation: false,
          };
        });
        historyList.value.push(...tempArr);
        total.value = res.data.totalPages;
      }
    };

    // 关闭历史记录
    const closeHandle = () => {
      showHistory.value = false;
    };

    // 搜索方法
    const searchHandle = () => {
      historyList.value = [];
      pageNum.value = 1;
      queryHistory();
    };

    // 打开编辑弹窗
    const openEditDialog = (item: any) => {
      currentRecord.value = item;
      editDialog.value.showEditDialog();
    };

    // 使用示例：更新条目
    const updateItem = (id: string) => {
      const updatedList = historyList.value.map((item) =>
        item.id === id ? { ...item, dialogueName: editContent.value } : item
      );
      return updatedList;
    };
    const changeHisData=(data:any)=>{
      for(let i=0;i<historyList.value.length;i++){
        if(data.id==historyList.value[i].id){
          historyList.value[i]=data
        }
      }
    }
    // 保存编辑
    const saveEdit = async (data: any) => {
      changeHisData(data)
     
      //将
      emit("update:historyList", data);
    };

    const handleDelete = async (data: any) => {
     
      // console.log( currentRecord.value)
      currentRecord.value = data;
      editDialog.value.handleDelete();
    };

    const deleteHistory = (data: any) => {
      const updataList = historyList.value.filter(
        (item: any) => item.id != data.id
      );
      historyList.value = updataList;
      emit("delete:historyList", data);
    };

    const changeHandle = () => {
      if (keywords.value == "") {
        searchHandle();
      }
    };

    const load = () => {
      if (pageNum.value < total.value) {
        pageNum.value++;
        queryHistory();
      }
    };

    // 查看详情
    const goDetail = (item: any) => {
      emit("goDetail", item);
    };

    onMounted(() => {
      watch(showHistory, () => {
        if (showHistory) {
          pageNum.value = 1;
          queryHistory();
        }
      });
    });

    onUnmounted(() => {
      pageNum.value = 1;
      historyList.value = [];
    });

    return {
      pageNum,
      pageSize,
      keywords,
      historyList,
      searchHandle,
      changeHandle,
      showHistory,
      showOperation,
      openEditDialog,
      editDialogVisible,
      editContent,
      saveEdit,
      handleDelete,
      closeHandle,
      currentRecord,
      load,
      goDetail,
      editDialog,
      deleteHistory,
      Search
    };
  },
});
</script>

<style lang="less" scoped>
ul {
  padding: 0;
  height: 36.458vw;
}

li {
  list-style: none;
}

.chat-history {
  position: fixed;
  right: 0px;
  bottom: 0px;
  width: calc(100% - 240px);
  height: 100vh;
  overflow: hidden;
  z-index: 900;
  background: #f6f9fa;
  backdrop-filter: blur(10px) hue-rotate(90deg);
}

.close_btn {
  width: 1.563vw;
  height: 1.563vw;
  background: rgb(204, 82, 95);
  border-radius: 0.417vw;
  position: absolute;
  top: 0.625vw;
  right: 1.25vw;
  font-size: 0.729vw;
  line-height: 1.563vw;
  text-align: center;
  color: #ffffff;
  cursor: pointer;
  &:hover {
    background: rgb(196, 74, 87);
    transform: scale(1.05);
  }
}

.history_main {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 40vw;
  overflow: visible;
  margin: 12px auto 0;
  .main_title {
    font-size: 24px;
    color: rgb(3, 7, 18);
    font-weight: 500;
    text-align: center;
    line-height: 2;
    margin-bottom: 16px;
  }
}

.time_flag {
  // margin-top: 21px;
  // margin-bottom: 14px;
  // font-size: 16px;
  line-height: 18.125px;

  color: rgba(0, 0, 0, 0.5);
    margin-top: 21px;
  margin-bottom: 14px;
    font-size: 0.9rem;
    font-weight: 700;
}

.history_item {
  box-sizing: border-box;
  width: 100%;
  line-height: 36px;
  background-color: rgb(255, 255, 255);
  cursor: pointer;
  font-size: 14px;
  border-radius: 7px;
  padding-inline: 21px;
  padding-top: 10.5px;
  padding-bottom: 10.5px;
  margin-top: 10.5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  &:hover {
    box-shadow: rgba(0, 0, 0, 0.07) 0px 0px 8px 0px,
      rgba(0, 0, 0, 0.03) 0px 2px 4px 0px;
  }

  .item_title {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #303133;
    font-weight: 600;
    img {
      width: 26px;
      height: auto;
      margin-right: 10px;
    }
  }
}

.item_content {
  flex: 1;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  padding-left: 12px;
}

.item_opration {
  display: flex;
  align-items: center;
  .edit_btn {
    display: flex;
    align-items: center;
    margin: 0 5px;
    &:hover {
      transform: scale(1.05);
    }
  }
}

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