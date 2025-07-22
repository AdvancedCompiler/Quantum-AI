<template>
  <div class="main" v-if="showUserCenter">
    <div class="main_content">
      <div class="main_top">
        <el-tooltip
          class="box-item"
          effect="dark"
          content="点击更换头像"
          placement="bottom"
        >
          <div
            class="head"
            @mouseover="showCover = true"
            @mouseout="showCover = false"
          >
            <input
              type="file"
              ref="fileInput"
              accept="image/*"
              @change="handleAvatarUpload"
              style="display: none"
            />
            <img :src="avatarUrl" alt="用户头像" />
            <div
              @click="$refs.fileInput.click()"
              v-show="showCover"
              class="img_cover"
            ></div>
          </div>
        </el-tooltip>

        <div class="nickName">{{ userInfo.userInfo.userName }}</div>
        <div class="userNumber">账号：{{ userInfo.userInfo.phone }}</div>
      </div>
      <div class="main_bottom">
        <div class="bottom_left">
          <ul>
            <li
              v-for="item in menuList"
              :key="item.id"
              @click="currentIndex = item.id"
              :class="{ active: currentIndex == item.id }"
            >
              <img :src="getIcon(item)" alt="" /><span>{{ item.name }}</span>
            </li>
          </ul>
        </div>
        <div class="bottom_right">
          <div class="user_info" v-if="currentIndex === 0">
            <p>姓名</p>
            <input v-model="newUsername" type="text" />
            <el-button
              style="float: right; margin-top: 14px"
              type="primary"
              color="#7d4dff"
              @click="saveUsername"
              >保存</el-button
            >
          </div>
          <div class="account_manage" v-else>
            <div style="padding-bottom: 20px">
              <p>密码管理</p>
              <div class="set_psd">
                <span>已设置</span
                ><a href="javascript:;" @click="psdVisible = true">更换</a>
              </div>
            </div>
            <!-- <p>绑定手机号</p>
            <div class="set_psd">
              <span>175****8806</span><a href="#">更换</a>
            </div> -->
          </div>
        </div>
      </div>
    </div>
    <div class="close_btn" @click="closeHandle">×</div>
    <!-- 修改密码弹窗 -->
    <el-dialog
      title="修改密码"
      v-model="psdVisible"
      width="26%"
      :before-close="handleClose"
      align-center
    >
      <template #header="{ close, titleId, titleClass }">
        <div class="my-header">
          <div :id="titleId" :class="titleClass">更换密码</div>
        </div>
      </template>
      <div class="my-body">
        <el-form :model="passwordForm" label-position="left">
          <el-form-item label="原密码" label-width="70px">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item label="新密码" label-width="70px">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item label="确认密码" label-width="70px">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="psdVisible = false">取 消</el-button>
        <el-button color="#7d4dff" type="primary" @click="changePassword"
          >完 成</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, defineExpose, onMounted } from "vue";
import userApi from "../api/userApi";
import { ElMessage } from "element-plus";
import { userInfoStore } from "@/stores/modules/userInfo";
import { md5 } from "js-md5";
import { base_url } from "@/utils/http";

// 用户信息
const userInfo = userInfoStore();
const avatarUrl = ref(""); // 默认头像
const username = ref("石舒雅");
const showCover = ref(false);
const showUserCenter = ref(false);

// 用户名修改
const editingUsername = ref(false);
const newUsername = ref("");

// 密码修改表单
const psdVisible = ref(false);
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const menuList = [
  {
    id: 0,
    name: "个人信息",
    iconPath: "../assets/icon_user.png",
    iconActive: "../assets/icon_user_active.png",
  },
  {
    id: 1,
    name: "账号管理",
    iconPath: "../assets/icon_setting.png",
    iconActive: "../assets/icon_setting_active.png",
  },
];
const currentIndex = ref(0);

const getIcon = (item) => {
  let url = "";
  if (currentIndex.value === item.id) {
    url = item.iconActive;
  } else {
    url = item.iconPath;
  }
  return new URL(url, import.meta.url).href;
};

// 获取用户头像
const getAvatar = (url) => {
  let defaultAvatar = "../assets/default_avatar.png";
  return url
    ? `${import.meta.env.VITE_API_BASE_URL}/user/show?filePath=${url}`
    : new URL(defaultAvatar, import.meta.url).href;
};

const closeHandle = () => {
  showUserCenter.value = false;
};

// 头像上传
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件");
    return;
  }

  // 预览图片
  const reader = new FileReader();
  reader.onload = (e) => {
    avatarUrl.value = e.target.result;
  };
  reader.readAsDataURL(file);

  // 实际开发中这里调用上传接口
  try {
    const formData = new FormData();
    formData.append("avatar", file);
    const res = await userApi.updateAvatar({
      id: userInfo.userInfo.id,
      formData,
    });
    if (res.code == 200) {
      userInfo.saveUserInfo({
        ...userInfo.userInfo,
        avatar: res.data,
      });
      ElMessage.success("头像更新成功");
    } else {
      ElMessage.error(res.msg || "头像上传失败");
    }
  } catch (error) {
    ElMessage.error(error.message || "头像上传失败");
  }
};

// 保存用户名
const saveUsername = async () => {
  if (!newUsername.value.trim()) {
    ElMessage.error("用户名不能为空");
    return;
  }

  try {
    const res = await userApi.updateUserName({
      id: userInfo.userInfo.id,
      userName: newUsername.value,
    });
    if (res.code === "200") {
      userInfo.saveUserInfo({
        ...userInfo.userInfo,
        userName: newUsername.value,
      });
      ElMessage.success("用户名修改成功");
      // 清空表单
      Object.assign(passwordForm, {
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } else {
      ElMessage.error(res.msg || "用户名修改失败");
    }
  } catch (error) {
    console.log(error);
    ElMessage.error("用户名修改失败");
  }
};

// 取消编辑
const cancelEdit = () => {
  editingUsername.value = false;
  newUsername.value = username.value;
};

// 修改密码
const changePassword = async () => {
  if (!passwordForm.newPassword.trim()) {
    ElMessage.error("新密码不能为空");
    return;
  }

  if (!passwordForm.oldPassword.trim()) {
    ElMessage.error("旧密码不能为空");
    return;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error("两次输入的新密码不一致");
    return;
  }

  try {
    const res = await userApi.updatePassword({
      id: userInfo.userInfo.id,
      oldPassword: md5(passwordForm.oldPassword),
      password: md5(passwordForm.newPassword),
    });
    if (res.code === "200") {
      ElMessage.success("密码修改成功");
      // 清空表单
      Object.assign(passwordForm, {
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      psdVisible.value = false;
    } else {
      ElMessage.error(res.msg || "密码修改失败");
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "密码修改失败");
  }
};

onMounted(() => {
  // 获取用户信息
  newUsername.value = userInfo.userInfo.userName;
  avatarUrl.value = getAvatar(userInfo.userInfo.avatar);
  console
});

defineExpose({
  showUserCenter,
  closeHandle,
});
</script>
<style lang="less" scoped>
.main {
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

.main_content {
  width: 41.667vw;
  height: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.head {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
  }
}

.img_cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6) url(../assets/icon_cover.png) no-repeat center;
  border-radius: 50%;
  z-index: 1;
}

.main_top {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .nickName {
    color: rgb(48, 49, 51);
    font-size: 18px;
    font-weight: 600;
    padding:44px 0 16px;
  }

  .userNumber {
    color: rgb(96, 98, 102);
    font-size: 16px;
    padding-bottom: 16px;
  }
}

.main_bottom {
  display: flex;
  justify-content: center;
}

.bottom_left {
  display: flex;
  width: 10.417vw;
  min-height: 17.708vw;
  flex-direction: column;
  background: rgb(255, 255, 255);
  padding: 20px 24px;
  border-radius: 20px;
  margin-right: 1.042vw;
  ul {
    padding: 0;
    margin: 0;
    li {
      display: flex;
      align-items: center;
      list-style: none;
      width: 8.333vw;
      padding: 10px;
      margin-bottom: 8px;
      border-radius: 8px;
      font-size: 15px;
      color: #4e5969;
      font-weight: 600;
      cursor: pointer;
      &.active {
        background: #f2edff;
        color: #7d4dff;
      }
      img {
        width: 16px;
        height: auto;
        margin-right: 8px;
      }
    }
  }
}

.bottom_right {
  display: flex;
  flex-direction: column;
  background: rgb(255, 255, 255);
  width: 26.042vw;
  padding: 32px;
  border-radius: 20px;
  line-height: 30px;
  p {
    font-size: 14px;
    color: #4e5969;
    padding-bottom: 4px;
    margin: 0;
    line-height: 1.2;
  }

  input {
    box-sizing: border-box;
    width: 100%;
    height: 40px;
    padding: 6px 12px;
    border: 0;
    outline: transparent solid 2px;
    outline-offset: 2px;
    background: rgb(242, 243, 245);
    border-radius: 8px;
    border: 1px solid #e2e8f0;
  }
}

.set_psd {
  box-sizing: border-box;
  display: flex;
  width: 100%;
  background: #f2f3f5;
  border-radius: 8px;
  padding: 7px 14px 7px 12px;
  justify-content: space-between;
  font-size: 14px;
  a {
    color: #7d4dff;
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }
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

.my-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 10px;
  font-weight: 500;
  background: rgb(251, 251, 252);
  border-radius: 8px 8px 0 0;
  div {
    line-height: 32px;
  }
}

.my-body {
  padding: 10px 24px;
  .el-input {
    --el-input-inner-height: 40px;
    --el-input-border-radius: 8px;
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
</style>