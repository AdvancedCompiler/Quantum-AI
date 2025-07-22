import { defineStore } from "pinia";
import { ref } from "vue";

const userInfoStore = defineStore(
  "userInfo",
  () => {
    //定义token
    const token = ref<string>("");

    const dialogueId = ref<string>("");
    const saveDialogueId = (val: string) => {
      dialogueId.value = val;
    };
    //保存token信息
    const saveToken = (val: string) => {
      //保存token
      token.value = val;
    };
    //清除当前token 
    const clearToken =()=>{
      token.value=''
    }
    //当前激活的菜单选项
    const activeIndex = ref<any>(-1)
    //保存当前激活的导航栏信息
    const saveActiveIndex = (val: any) => {
      activeIndex.value = val

    }
    //清除当前的索引
    const clearActiveIndex=()=>{
      activeIndex.value = -1
    }

    //当前在主页显示的需要进行对话的字段
    const preChat =ref<string>('')
    //保存预输入的对话
    const savePreChat=(val:string)=>{
      preChat.value=val
    }
    const clearPreChat =()=>{
      preChat.value=''
    }

    //定义用户信息
    const userInfo = ref<any>({});

    // 当前对话信息
    const dialogue = ref<any>();

    // 更新对话信息
    const saveDialogue = (val: any) => {
      dialogue.value = val;
    };

    //保存用户信息
    const saveUserInfo = (val: any) => {
      userInfo.value = val;
    };
    //清除用户信息
    const clearUserInfo = () => {
      userInfo.value = {};
    };
    
    // 获取用户头像
    // const getAvatar = () => {
    //   let defaultAvatar = "../../assets/default_avatar.png";
    //   return userInfo.value.avatar
    //     ? `http://49.232.26.44:8062/user/show?filePath=${userInfo.value.avatar
    //     }`
    //     : new URL(defaultAvatar, import.meta.url).href;
    // };
    const getAvatar = () => {
      let defaultAvatar = "../../assets/default_avatar.png";
      return userInfo.value.avatar
        ? `${import.meta.env.VITE_API_BASE_URL}/user/show?filePath=${userInfo.value.avatar
        }`
        : new URL(defaultAvatar, import.meta.url).href;
    };
    return {
      token,
      saveToken,
      clearToken,
      userInfo,
      saveUserInfo,
      clearUserInfo,
      dialogueId,
      saveDialogueId,
      getAvatar,
      dialogue,
      saveDialogue,
      activeIndex,
      saveActiveIndex,
      clearActiveIndex,
      preChat,
      savePreChat,
      clearPreChat
    };
  },
  { persist: true }
);

export { userInfoStore };
