<script lang="ts" setup>
import { v4 as uuidv4 } from 'uuid';
import { onMounted, ref, onUnmounted, reactive } from "vue";
import {
  Search,
  Promotion,
  FolderAdd,
  Eleme,
  Loading,
  Refresh,
  CopyDocument,
  ArrowDown,
  CirclePlus,
  Document,
  Delete,
  Position,
  SwitchButton,
  User,
  ArrowUp
} from "@element-plus/icons-vue";
// import MarkdownRenderer from '@/MarkdownRenderer.vue';
import MarkdownRenderer from "@/components/MarkDownIt.vue";
import ResultFeedback from "@/components/ResultFeedback.vue";
import EditDialog from "@/components/dialogs/EditDialog.vue";
import router from "@/router";
//获取用户信息
import { userInfoStore } from "@/stores";
import {
  clearHistoryApi,
  giveLikeApi,
  queryChatApi,
  queryHistoryApi,
  refreshApi,
  sendChatApi,
  stopApi,
} from "@/api/chat";
import { ElMessage } from "element-plus";
import UserCenter from "./UserCenter.vue";
import ChatHistory from "@/components/ChatHistory.vue";
import PreImage from "@/components/PreImage.vue";
import ClipboardJS from "clipboard";
import { base_url } from '@/utils/http'
import { sassTrue } from 'sass';
import { EventSourcePolyfill } from 'event-source-polyfill'; // 引入 EventSourcePolyfill


const userInfo = userInfoStore();
// 个人中心
const userCenter = ref<any>(null);

const sendData = ref<any>("");
const goSearch = (str: string) => {
  keyword.value = str;
  queryKey();
};

/*----------- 下拉加载---------------- */
// const load =ref<boolean>(false)
const count = ref<any>(60);

const load = () => {

  // if (history.value) return;
  if (historyLoading.value) return;
  //当还有未查询时
  if (!historyNomore.value) {
    queryHistory();
  }
};

/* -----------上拉加载    问答内容加载 */
//上拉加载-- 加载中的标志
// const markdownContent = ref<any>("# 占位回复\n\n当前服务尚未部署实际的语言模型。\n\n## 您的选择\n- **模型**: `default`\n- **是否上传图片**: 否\n\n## 示例 Markdown 样式\n- **加粗**: `**加粗文本**`\n- *斜体*: `*斜体文本*`\n- ~~删除线~~: `~~删除线文本~~`\n- [超链接](https://www.bing.com): `[超链接](URL)`\n- 图片:\n\n![示例图片](https://vuejs.org/images/logo.png)\n\n## 列表示例\n1. 第一项\n2. 第二项\n   - 子项 1\n   - 子项 2\n\n> 引用示例：这是一个引用块。\n\n```python\n# 代码块示例\ndef hello():\n    print('Hello, World!')\n```\n\n")
//     const markdownContent = ref<any>(`\`\`\`math
// \oint_C x^3\, dx + 4y^2\, dy
// \`\`\``)
 //const markdownContent = ref<any>("  我是\n\n深度思考的内容，  <br>我是深度思考的内容，深度思考的内容  <\think># 占位回复\n\n当前服务尚未部署实际的语言模型。\n\n## 您的选择\n- **模型**: `default`\n- **上传的图片数量**: 0\n\n## 示例 Markdown 样式\n- **加粗**: `**加粗文本**`\n- *斜体*: `*斜体文本*`\n- ~~删除线~~: `~~删除线文本~~`\n- [超链接](https://www.bing.com): `[超链接](URL)`\n- 图片:\n\n![示例图片](https://vuejs.org/images/logo.png)\n\n## 列表示例\n1. 第一项\n2. 第二项\n   - 子项 1\n   - 子项 2\n\n> 引用示例：这是一个引用块。\n\n```python\n# 代码块示例\ndef hello():\n    print('Hello, World!')\n```\n\n- 行内公式: `$E = mc^2$` 显示为 $E = mc^2$\n- 块级公式:\n\n$$\n\\int_a^b f(x) \\ dx = F(b) - F(a)\n$$\n\n上述公式显示为：\n\n$$\n\\int_a^b f(x) \\ dx = F(b) - F(a)\n$$\n` ");

// const markdownContent = ref<any>(`$$
// \\int_a^b f(x)\\,dx = F(b) - F(a)
// $$`)
// const markdownContent = ref<any>(`$E = mc^2$`)
// const markdownContent = ref<any>(`\[[C(z) = \sum_{\alpha=1}^{m} C_\alpha(z)\]]`)

// const markdownContent = ref<any>(`$ C(z) = \sum_{\alpha=1}^{m} C_{\alpha}(z) $`)
//$E = mc^2$

//     const markdownContent = ref<any>( ` ### 公式解析与相关文献解释

// #### 公式解析

// **公式**：
// \[ C(z) = \sum_{\alpha=1}^{m} C_\alpha(z) \]

// **符号含义**：
// - \( C(z) \): 一个函数，表示由 \( z \) 所代表的 \( n \) 位二进制字符串满足的约束条件数量。
// - \( m \): 总共有 \( m \) 个约束条件（或称作“clause”）。
// - \( C_\alpha(z) \): 对于特定约束条件 \( \alpha \)，当 \( z \) 满足该约束条件时，\( C_\alpha(z) = 1 \)；否则 \( C_\alpha(z) = 0 \)。

// **数学含义**：
// 公式 (1) 描述了一个组合优化问题中的目标函数 \( C(z) \)，其中 \( z \) 是一个长度为 \( n \) 的二进制字符串。每个 \( C_\alpha(z) \) 表示第 \( \alpha \) 个约束条件是否被满足。通过求和所有 \( C_\alpha(z) \)，我们得到 \( z \) 满足的约束条件总数。因此，\( C(z) \) 可以被视为一个衡量 \( z \) 在给定约束条件下的满意度的函数。

// #### 相关文献解释

// **文献背景**：
// 该公式出现在一篇关于量子算法设计的论文中，主要讨论了如何利用量子计算解决组合优化问题。文中提到，组合优化问题通常由 \( n \) 个比特和 \( m \) 个约束条件组成。每个约束条件是一个子集的约束，只有某些特定的比特配置满足这些约束条件，而其他配置则不满足。目标函数 \( C(z) \) 定义为满足约束条件的数量，即 \( C(z) \) 等于所有满足约束条件的 \( C_\alpha(z) \) 的总和。

// **公式在文献中的角色**：
// 公式 (1) 在文献中扮演着核心角色，它定义了目标函数 \( C(z) \)，用于量化一个二进制字符串 \( z \) 在给定约束条件下的满意度。在量子算法设计中，这个目标函数是优化问题的核心，因为量子算法的目标通常是找到一个 \( z \) 使得 \( C(z) \) 达到最大值。此外，文献还讨论了如何使用量子算法来近似解决这种优化问题，以及在特定情况下（如 MaxCut 问题）的性能表现。

// **应用场景**：
// 该公式在量子计算领域有广泛的应用，特别是在组合优化问题上。例如，在 MaxCut 问题中，目标是找到一个分割图的切割方案，使得切割边的数量最大化。通过将 MaxCut 问题转化为一个组合优化问题，可以利用公式 (1) 来定义目标函数，并设计相应的量子算法来寻找最优解。

// **潜在意义**：
// 公式 (1) 对于量子算法设计具有重要意义。通过量子算法，可以更高效地搜索满足约束条件的解空间，从而提高优化问题的求解效率。此外，公式 (1) 还可以用于评估量子算法的性能，通过比较不同量子算法下 \( C(z) \) 的期望值，可以判断算法的有效性和优越性。"`)

const chatLoading = ref<boolean>(false);

const items = ref<any>([]); // 数据列表

//设置一个标志  看看是否是用户鼠标向上滚动
const isUserScrolledUp = ref<boolean>(false)
const handleScroll = () => {
  //这样可以实现用户向上移了一下
  if (scrollContainer.value.clientHeight + scrollContainer.value.scrollTop < scrollContainer.value.scrollHeight - 10) {
    //用户向上滚动
    console.log('用户向上滚动了')
    isUserScrolledUp.value = true
  } else {
    //用户滚动到底部
    console.log('用户向下滚动了')
    isUserScrolledUp.value = false
  }


  if (
    scrollContainer.value.scrollTop == 0 &&
    !chatLoading.value &&
    !chatNomore.value
  ) {
    scrollTop.value = scrollContainer.value.scrollHeight;
    loadMore();
  }
  // console.log(e.scrollTop)
  // if (e.scrollTop === 0 && !chatLoading.value && !chatNomore.value) {
  //     scrollTop.value = scrollContainer.value.scrollHeight;
  //     console.log('11111111111')
  //     console.log(scrollTop.value)
  //     loadMore();
  // }
};
// 模拟加载数据
const scrollContainer = ref<any>(null);
const scrollTop = ref(0);
const loadMore = async () => {
  if (chatLoading.value) return;
  chatLoading.value = true;
  //应该传输一个值、、是判断到顶部还是到底部
  //传输true  则到底部
  await queryChat(false);
  // Vue.nextTick(() => {
  setTimeout(() => {
    const newScrollHeight = scrollContainer.value.scrollHeight;
    scrollContainer.value.scrollTop = newScrollHeight - scrollTop.value;
  }, 0);

  // })

  // setTimeout(() => {
  //     // const newItems = Array.from({ length: 10 }, (_, i) => `Item ${items.value.length + i + 1}`);
  //     // items.value.unshift(...newItems); // 将新数据添加到顶部

  //     chatLoading.value = false;
  // }, 1000);
};

const myDirective = {};

//保存对话的list
const listChat = ref<any>([]);

//退出登录
const exitFun = () => {
  userInfo.clearUserInfo();
  userInfo.clearActiveIndex()
  userInfo.saveDialogueId("");
  userInfo.clearPreChat()
  router.push({ path: "/" });
};
const isCopied = ref(false);
let clipboard: any = null;

onMounted(() => {
  // initHello()
  dialogueId.value = userInfo.dialogueId;
  if (dialogueId.value == "") {
    //如何进来的时候//调用 新建对话
    newDialogue()
    //此时判断缓存里是不是preChat为空
    if(userInfo.preChat!=''){
      //调用sendChat方法  --进行发送信息
      sendData.value=userInfo.preChat;
      sendChat()
      userInfo.clearPreChat()
    }

  } else {
    activeIndex.value=userInfo.activeIndex
    queryChat(true);
  }
  queryHistory();
  clipboard = new ClipboardJS(".copy-button");
  // 监听复制成功事件
  clipboard.on("success", (e: any) => {
    isCopied.value = true;
    // setTimeout(() => (isCopied.value = false), 2000); // 2 秒后隐藏提示
    e.clearSelection(); // 清除选中状态
    ElMessage({
      message: "复制成功",
      type: "success",
    });
  });
  // 监听复制失败事件
  clipboard.on("error", (err: any) => {
    ElMessage({
      message: "复制成功",
      type: "success",
    });
    // console.error('复制失败:', err);
  });
});

onUnmounted(() => {
  // 销毁 clipboard 实例
  closeConnection()
  if (clipboard) {
    clipboard.destroy();
  }
});

//聊天禁止发送的标志
const chatPageNum = ref<number>(1);
const chatPageSize = ref<number>(10);
const chatTotal = ref<number>();
const dialogueId = ref<string>("");
const chatNomore = ref<boolean>(false);
//是否是个新窗口
const isNew = ref<boolean>(true)

//查询聊天记录
const queryChat = async (val: any) => {
  chatLoading.value = true;
  let params = {
    pageNum: chatPageNum.value,
    pageSize: chatPageSize.value,
    dialogueId: dialogueId.value,
  };
  let res: any = await queryChatApi(params);
  if (res.code == 200) {

    for (let i = 0; i < res.data.content.length; i++) {
      // listChat.value.unshift(res.data[i])
      //---------
      let think = ''
      let content = ''

      if (res.data.content[i].content.includes('</think>')) {
        let thiPre = res.data.content[i].content.split('</think>') //切出的前一段
        //
        let thiBre = thiPre[0].split('<think>')
        think = thiBre[1]
        content = thiPre[1]
      } else {
        content = res.data.content[i].content
        think = ''
      }

      let reTmp = res.data.content[i];
      reTmp.content = content
      let obj = { ...reTmp, think: think, isSpread: true }
      // listChat.value[listChat.value.length - 1] = obj;
      //---------
      listChat.value.unshift(obj);
    }
    if (res.data.totalPages == chatPageNum.value||res.data.totalPages==0) {
      chatNomore.value = true;
      //没有更多了
      //push一个问题  你好，我是量子AI智能体
      initHello()

    } else {
      chatPageNum.value++;
      chatNomore.value = false;
    }
    chatLoading.value = false;
    if (val) {
      setTimeout(() => {
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
      }, 200);

      //则滚动到底部
      //否则不处理
    }
  } else {
    chatLoading.value = false;
    ElMessage({
      type: "warning",
      message: res.msg,
    });
  }
};
//发送聊天记录

const isOut = ref<boolean>(false);
const senfFlagLoading = ref<boolean>(false);
//等待发送信息
const sendChat = async () => {
  senfFlagLoading.value = true;
  let params = {
    phone: userInfo.userInfo.phone,
    dialogueId: dialogueId.value,
    content: sendData.value,
    isSpread: true,
    think: '',//当有think字段时----不为空
    image: previewImage.value,
    //
    isHello: false,
    createTime: "",
    id: "",
    role: "user",
    status: "",
    updateTime: "",
  };
  listChat.value.push(params);
  let pa = {
    phone: userInfo.userInfo.phone,
    dialogueId: dialogueId.value,
    content: sendData.value,
    image: previewImage.value,
  };
  //组合成参数
  const data = new FormData();
  //添加需要的字段

  if (dialogueId.value == '') {
    //如果当对话Id是空时，这时需要创建一个
    dialogueId.value = (uuidv4().replace(/-/g, ''))
  }
  data.append("phone", userInfo.userInfo.phone);
  data.append("dialogueId", dialogueId.value);
  data.append("content", sendData.value);
  // data.append()
  if (fileImage.value) {
    data.append("image", fileImage.value);
  } else {
    data.append("image", "");
  }
  previewImage.value = null;
  sendData.value = "";
  fileImage.value = null;
  fileInput.value.value = "";
  let tmpParams = {
    phone: userInfo.userInfo.phone,
    dialogueId: dialogueId.value,
    isSpread: true,//是否展开think的字段
    think: '', //当有think字段时----不为空
    content: "",
    image: "",
    isHello: false,
    createTime: "",
    id: "",
    role: "system",
    status: "",
    updateTime: "",
  };
  listChat.value.push(tmpParams);
  //设置一个临时的加载中的数据

  setTimeout(() => {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;

  }, 100);
  setTimeout(() => {
    isUserScrolledUp.value = false;
  }, 1000)
  userInfo.saveDialogueId(dialogueId.value);
  restartConnection(dialogueId.value)
  const res: any = await sendChatApi(data);

  //发出去后就清空值
  senfFlagLoading.value = false;
  if (res.code == 200) {

    //开始切割字符串
    //<think>
    //</think>


    let think = ''
    let content = ''

    if (res.data.content.includes('</think>')) {
      let thiPre = res.data.content.split('</think>') //切出的前一段
      //
      let thiBre = thiPre[0].split('<think>')
      think = thiBre[1]
      content = thiPre[1]
    } else {
      content = res.data.content
      think = ''
    }

    let reTmp = res.data;
    reTmp.content = content
    let obj = { ...reTmp, think: think, isSpread: true, isHello: false }
    listChat.value[listChat.value.length - 1] = obj;
    // listChat.value[listChat.value.length - 1] = res.data;
    // let a = res.data.content;
    // listChat.value[listChat.value.length - 1].content = "";
    // dialogueId.value = res.data.dialogueId;

    // userInfo.saveDialogueId(res.data.dialogueId);
    // let i = 0;
    // typingInterval.value = setInterval(() => {
    //   if (i < a.length) {
    //     isOut.value = true;
    //     listChat.value[listChat.value.length - 1].content += a.charAt(i);
    //     scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    //     i++;
    //   } else {
    //     isOut.value = false;
    //     clearInterval(typingInterval.value);
    //   }
    // }, 20);

    // setTimeout(() => {
    //   scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    // }, 0);
  } else {
    //这时大模型报错了
    //需要设置的时候isHello
    listChat.value[listChat.value.length - 1].content =
      "大模型繁忙，请稍后再试";
        listChat.value[listChat.value.length - 1].isHello =true
    setTimeout(() => {
      scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    }, 0);
    ElMessage({
      message: res.msg,
      type: "error",
    });
  }
  //查询完毕调用左侧的菜单接口
  if (isNew.value) {
    historyList.value = [];
    historyPageNum.value = 1;
    historyPageSize.value = 20;
    historyNomore.value = false;
    isNew.value = false;
    // setTimeout(()=>{
    queryHistory();
    // },3000)
  }
};
const activeIndex = ref<number>(-1);
//查询左侧历史记录列表
const historyList = ref<any>([]);
const historyPageNum = ref<number>(1);
const historyPageSize = ref<number>(20);
//host
const historyLoading = ref<boolean>(false);
const historyNomore = ref<boolean>(false);
const historyTotal = ref<number>();
const keyword = ref<string>("");

// 重命名历史记录
const renameObj = ref<any>({});
const editDialog = ref<any>(null);
const handleRename = (data: any) => {
  //当是新对话时则直接返回
  if (isNew.value) {
    return
  }
  renameObj.value = data;
  editDialog.value.showEditDialog();
};

// 删除历史记录
const handleDelete = (data: any) => {
  renameObj.value = data;
  editDialog.value.handleDelete();
};

const queryHistory = async () => {
  historyLoading.value = true;
  let params = {
    pageNum: historyPageNum.value,
    pageSize: historyPageSize.value,
    search: keyword.value,
  };
  const res: any = await queryHistoryApi(params);

  historyLoading.value = false;
  if (res.code == 200) {
    if (
      res.data.totalPages == historyPageNum.value ||
      res.data.content.length == 0
    ) {
      historyNomore.value = true;
    } else {
      historyNomore.value = false;
      historyPageNum.value++;
    }

    for (let i = 0; i < res.data.content.length; i++) {
      historyList.value.push(res.data.content[i]);
    }

    //格式化当前数据时间格式的问题
    let listTmp = JSON.parse(JSON.stringify(historyList.value));

    for (let i = 0; i < listTmp.length; i++) {
      listTmp[i].timeFlag = formatDateRange(listTmp[i].createTime);
      listTmp[i].showOperation = false;
      listTmp[i].showOperationMenu = false;
    }

    // let arr = [];
    // for (let i = 0; i < listTmp.length; i++) {
    //   if (arr.indexOf(listTmp[i].timeFlag) == -1) {
    //     arr.push(listTmp[i].timeFlag);
    //   } else {
    //     listTmp[i].timeFlag = "";
    //   }
    // }

    historyList.value = listTmp;
  } else {
    ElMessage({
      type: "error",
      message: res.msg,
    });
  }
};

const updataHistory = (data: any) => {
  const updataList = historyList.value.map((item: any) => {
    return {
      ...item,
      dialogueName: item.id == data.id ? data.dialogueName : item.dialogueName,
    };
  });
  historyList.value = updataList;
};

const deleteHistory = (data: any) => {
  const updataList = historyList.value.filter(
    (item: any) => item.id != data.id
  );
  historyList.value = updataList;
};

const formatDateRange = (dateString: any) => {
  const date: any = new Date(dateString);
  const today: any = new Date();
  const timeDiff = today - date;
  const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));
  if (daysDiff === 0 || daysDiff < 0) {
    return "今天";
  } else if (daysDiff == 1) {
    return "昨天";
  } else if (daysDiff <= 7) {
    return "一周";
  } else if (daysDiff <= 30) {
    return "30天前";
  } else if (daysDiff <= 60) {
    return "60天";
  } else {
    return "更早";
  }
};

const queryKey = () => {
  historyList.value = [];
  historyPageNum.value = 1;
  historyPageSize.value = 20;
  queryHistory();
};

//点击查询一段对话
const queryDetail = (val: any) => {
  closeConnection()
  senfFlagLoading.value=false;
  userInfo.saveDialogue(val);
  isNew.value = false
  //关闭所有弹窗
  historyDialog.value.showHistory = false;
  userCenter.value.showUserCenter = false;

  //点击查询详情
  dialogueId.value = val.id;
  userInfo.dialogueId = val.id;

  //查询测试记录
  chatPageNum.value = 1;
  chatPageSize.value = 10;
  listChat.value = [];
  queryChat(true);
  clearFun();
};
const clearFun = () => {
  clearInterval(typingInterval.value);
  isOut.value = false;
};
//新建对话

const newDialogue = () => {
  closeConnection()
  senfFlagLoading.value=false;
  // 关闭所有弹窗
  historyDialog.value.showHistory = false;
  userCenter.value.showUserCenter = false;
  //这时设置  activeIndex 未-1
  //设置 dialogueId 为空
  //设置列表为空
  activeIndex.value = -1;
  userInfo.saveDialogueId("");
  dialogueId.value = "";
  listChat.value = [];
  isNew.value = true;
  clearFun();

  initHello()
};

const helloString =ref<any>('你好，我是量子AI智能体')

// 初始化语句
const initHello = () => {
  let params = {
    phone: userInfo.userInfo.phone,
    dialogueId: dialogueId.value,
    content: helloString,
    isSpread: false,
    think: '',//当有think字段时----不为空
    image: previewImage.value,
    createTime: "",
    isHello: true,
    id: "",
    role: "system",
    status: "",
    updateTime: "",
  };
  listChat.value.unshift(params);
}


const visible = ref<boolean>(false);

//图片参数事件

//预览图片
const previewImage = ref<any>(null);
//上传图片的图片
const fileImage = ref<any>(null);

const fileInput = ref<any>(null);
//选择图片改变事件

const handleFileChange = (event: any) => {
  // previewImage.value = event.target.files[0];
  fileImage.value = event.target.files[0];
  const files = event.target.files;

  if (files && files.length > 0) {
    for (const file of files) {
      if (file.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          // 将图片文件读取为 Base64 字符串并添加到数组中
          previewImage.value = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    }
  }
};
//移除单个图片
// 移除单个图片
const removeImage = (index: any) => {
  previewImage.value = null; // 从数组中移除指定索引的图片
};

//模拟点击输入框的事件

const triggerFileInput = () => {
  fileInput.value.click(); // 模拟点击文件输入框
};

//点击enter 进行发送
const handleSend = (event: any) => {
  if (event.key === "Enter") {
    // 如果同时按下了 Shift 键，允许换行
    if (event.shiftKey) {
      return;
    }

    // 否则，阻止默认行为并触发发送逻辑
    event.preventDefault();
  }
  // event.preventDefault();
  if (sendData.value.trim().length == 0) {
    ElMessage({
      message: "请输入内容",
      type: "warning",
    });
    return;
  } else {
    sendChat();
  }
};

// 查看用户中心
const goUserCenter = () => {
  userCenter.value.showUserCenter = true;
};

//点击复制文本
const copyText = () => { };
//点击刷新 按钮，//将聊天记录里边的这个对话以下的删除掉，从这句话开始生成
const refreshFun = () => {
  //开始刷新
  //调用接口
};

const preImage = ref<any>(null);

const imageFun = (url: string) => {
  preImage.value.open(url);
};
//点赞触发的方法
const praiseFun = (id: string, endorse: string) => {
  //
  let params = {};
  for (let i = 0; i < listChat.value.length; i++) {
    if (id == listChat.value[i].id) {
      listChat.value[i].endorse = endorse;
      params = JSON.parse(JSON.stringify(listChat.value[i]));
    }
  }
  giveLikeApi(params);
  //调用接口//开始改变状态
};

const resultOppose = ref<any>(null);
//点击踩的按钮
const oppose = (id: string) => {
  resultOppose.value.open(id);
};

//父组件的方法
const parentMethod = (val: any) => {
  //调用父组件的方法
  let id = val.id;
  let textarea = val.textarea;
  let params = {};
  for (let i = 0; i < listChat.value.length; i++) {
    if (id == listChat.value[i].id) {
      listChat.value[i].endorse = "3";
      listChat.value[i].feedback = textarea;
      params = JSON.parse(JSON.stringify(listChat.value[i]));
    }
  }
  giveLikeApi(params);
};

// 查看对话历史记录
const historyDialog = ref<any>(null);
const typingInterval = ref<any>(null);
const showHistoryDialog = () => {
  historyDialog.value.showHistory = true;
};

//点击刷线在线记录
const refreshRecord = async (val: any) => {
  senfFlagLoading.value = true;
  //在线刷新记录
  //拿到id
  //改ID之前的所有数据
  //改ID之后的数据//包括此ID的数据全部删除
  let params = {};
  for (let i = 0; i < listChat.value.length; i++) {
    if (val.id == listChat.value[i].id) {
      params = JSON.parse(JSON.stringify(listChat.value[i]));
    }
  }
  let list = [];
  for (let i = 0; i < listChat.value.length; i++) {
    if (val.id == listChat.value[i].id) {
      break;
    }
    list.push(listChat.value[i]);
  }
  listChat.value = list;
  let tmpParams = {
    phone: userInfo.userInfo.phone,
    dialogueId: dialogueId.value,
    think: '',
    content: "",
    isSpread: true,
    image: "",
    isHello: false,
    createTime: "",
    id: "",
    role: "system",
    status: "",
    updateTime: "",
  };
  listChat.value.push(tmpParams);
  dialogueId.value = val.dialogueId;
  userInfo.saveDialogueId(val.dialogueId);
  restartConnection(dialogueId.value)
  let res: any = await refreshApi(params);
  senfFlagLoading.value = false;
  if (res.code == 200) {
    let think = ''
    let content = ''

    if (res.data.content.includes('</think>')) {
      let thiPre = res.data.content.split('</think>') //切出的前一段
      //
      let thiBre = thiPre[0].split('<think>')
      think = thiBre[1]
      content = thiPre[1]
    } else {
      content = res.data.content
      think = ''
    }

    let reTmp = res.data;
    reTmp.content = content
    let obj = { ...reTmp, think: think, isSpread: true, isHello: false }
    listChat.value[listChat.value.length - 1] = obj;


  } else {
    //这时大模型报错了
    //需要设置的时候
    listChat.value[listChat.value.length - 1].content =
      "大模型繁忙，请稍后再试";
    setTimeout(() => {
      scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    }, 0);
    ElMessage({
      message: res.msg,
      type: "error",
    });
  }
  //结果返回后
};

//初始化监听的对象
const eventSource = ref<any>()
//初始化监听的方法
const initEventSource = (id: string) => {

  //需要传入访问的路径
  //获取用户信息
  // base_url
  let url = `${base_url}/sse/foundSse?dialogueId=${id}`

  //这是新插件的写法--------开始
  let token = userInfo.token
  //新的插件-----进行创建的
  eventSource.value = new EventSourcePolyfill(url, {
    headers: {
      Authorization: `Bearer ${token}`, // 自定义请求头
    },
    withCredentials: true, // 允许跨域请求携带凭据
  });
  //这是新插件的写法--------结束


  // eventSource.value = new EventSource(url);
  eventSource.value.onmessage = (event: any) => {

    //打印传输过来的数据
    let jsonData = JSON.parse(event.data)
    if (jsonData.type == 'think') {
      if (jsonData.dialogueId == dialogueId.value) {
        listChat.value[listChat.value.length - 1].think += jsonData.content

        //当不是再最低部时 则不强制向下刷新
        if (!isUserScrolledUp.value) {
          scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
        }

      }

    } else {
      if (jsonData.dialogueId == dialogueId.value) {
        listChat.value[listChat.value.length - 1].content += jsonData.content
        if (!isUserScrolledUp.value) {
          scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
        }
      }

    }
  }
  eventSource.value.onerror = (error: any) => {

    console.error('EventSource error:', error);
    closeConnection()
  }

}

//关闭sse通道
const closeConnection = () => {
  if (eventSource.value) {
    eventSource.value.close();
    eventSource.value = null;
    console.log('EventSource closed.')
  }
}

//重启先关闭--再初始化开启
const restartConnection = (dialogueId: string) => {
  closeConnection();
  initEventSource(dialogueId);
}





//清空历史
const clearHistory = () => {
  //如果是新的测试
  if (isNew.value) {
    return
  }

  //查询当前的列表
  listChat.value = []
  // initHello()
  //调用清空历史的接口
  clearHistoryApi(dialogueId.value)
  // cle(dialogueId.value)
  queryChat(true);
}
//点击展开需要的参数
const spreadFun = (item: any) => {
  for (let i = 0; i < listChat.value.length; i++) {
    if (item.id == listChat.value[i].id) {
      listChat.value[i].isSpread = !listChat.value[i].isSpread
    }
  }
}

//点击激活的菜单
const activeIndexFun=(val:number)=>{
  userInfo.saveActiveIndex(val)
  activeIndex.value=val
  // (activeIndex = index)
}

//点击停止

const stop_fun=(item:any)=>{
  //点击停止触发方法
  stopApi(item.dialogueId)
}


</script>
<template>
  <div class="ai_main">
    <div class="main_left">
      <div class="ai_main_title">
        <img class="img_cla" src="../assets/logo.png" alt="" />
        <div class="title">量子AI智能体</div>
        <div class="build_yuyan" @click="newDialogue">
          <span>新建对话</span>
        </div>
      </div>
      <div class="search_input">
        <el-input v-model="keyword" @clear="queryKey" placeholder="搜索历史记录" clearable class="input_com">
          <template #prepend><el-button @click="queryKey" :icon="Search" /></template>
          <template #suffix>
            <el-tooltip class="box-item" effect="dark" content="查看全部历史会话" placement="right">
              <svg t="1741168639269" class="icon_history" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="5410" width="18" height="18" @click="showHistoryDialog">
                <path
                  d="M771.569371 709.280914h-55.178971v-81.276343c0-11.088457-7.314286-18.490514-18.373486-18.490514-11.029943 0-18.402743 7.402057-18.402743 18.490514v118.198858h95.612343c7.3728 0 14.716343-7.3728 14.716343-18.461258s-7.343543-18.461257-18.373486-18.461257zM664.429714 146.285714c57.344 0 62.8736 17.554286 62.8736 65.565257v218.901943c0 11.088457-10.912914 18.549029-19.104914 18.549029s-19.573029-7.460571-19.573029-18.549029V216.502857c0-20.099657-2.223543-30.134857-24.195657-30.134857H214.688914c-19.894857 0-28.203886 9.069714-28.203885 30.134857v591.286857c0.058514 29.842286 1.2288 29.842286 28.203885 29.842286h302.986972c11.029943 0 19.543771 9.742629 19.543771 20.362971a19.6608 19.6608 0 0 1-19.543771 19.719315H214.688914C168.608914 877.714286 146.285714 865.133714 146.285714 820.984686V211.850971C146.285714 163.84 162.2016 146.285714 210.007771 146.285714z m38.414629 384.643657c96.724114 0 174.869943 77.472914 174.869943 173.407086C877.714286 800.241371 799.597714 877.714286 702.844343 877.714286c-96.724114 0-174.869943-77.472914-174.869943-173.377829 0-95.963429 78.116571-173.407086 174.869943-173.407086z m0 36.893258c-74.400914 0-137.654857 62.698057-137.654857 136.513828 0 77.443657 63.253943 136.484571 137.654857 136.484572a136.777143 136.777143 0 0 0 137.654857-136.484572c0-77.502171-63.253943-136.513829-137.654857-136.513828zM390.290286 658.285714c11.146971 0 18.607543 8.777143 18.607543 21.942857s-7.460571 21.942857-18.607543 21.942858H252.664686C241.488457 702.171429 234.057143 693.394286 234.057143 680.228571s7.431314-21.942857 18.607543-21.942857z m63.253943-121.914514c11.146971 0 18.607543 8.777143 18.607542 21.942857s-7.460571 21.942857-18.607542 21.942857H252.664686c-11.176229 0-18.607543-8.777143-18.607543-21.942857s7.431314-21.942857 18.607543-21.942857z m148.801828-121.885257c11.176229 0 18.607543 8.777143 18.607543 21.942857s-7.431314 21.942857-18.607543 21.942857H252.664686c-11.176229 0-18.607543-8.777143-18.607543-21.942857s7.431314-21.942857 18.607543-21.942857z m0-121.914514c11.176229 0 18.607543 8.777143 18.607543 21.942857s-7.431314 21.942857-18.607543 21.942857H252.664686c-11.176229 0-18.607543-8.777143-18.607543-21.942857s7.431314-21.942857 18.607543-21.942857z"
                  fill="#8a8a8a" opacity=".8" p-id="5411"></path>
              </svg>
            </el-tooltip>
          </template>
        </el-input>
      </div>

      <!-- <MarkdownRenderer :markdown="markdownContent" class="think_cla" /> -->
      <div class="history">
        <!-- 下拉加载控件 -->
        <div class="history_list">
          <div v-infinite-scroll="load" class="infinite-list">
            <div v-for="(item, index) in historyList" :key="item" class="infinite-list-item">
              <div v-show="item.timeFlag != historyList[index - 1]?.timeFlag" class="history_list_cycle">
                {{ item.timeFlag }}
              </div>
              <div :class="{ active: index === activeIndex }" class="history_list_title"
                @click="activeIndexFun(index), queryDetail(item)">
                <span>{{ item.dialogueName }}</span>
                <el-popover :width="80" trigger="hover" :show-arrow="true" placement="right">
                  <template #reference>
                    <div class="more_btn">...</div>
                    <!-- <span>{{ item.dialogueName }}</span> -->
                  </template>

                  <ul class="more_opration">
                    <li @click.stop="handleRename(item)">
                      <svg width="20" height="20" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"
                        focusable="false" class="chakra-icon css-147zza3" id="SvgIc-24">
                        <g clip-path="url(#SvgIc-24-edit_svg__a)">
                          <path fill-rule="evenodd" clip-rule="evenodd"
                            d="M9.264 2.02a1.333 1.333 0 0 1 1.8-.078l.085.078 2.829 2.829a1.334 1.334 0 0 1 .078 1.8l-.078.085-6.876 6.876a1.334 1.334 0 0 1-.826.385L6.16 14H2.67a.674.674 0 0 1-.669-.6l-.004-.073V9.838c0-.313.11-.616.311-.856l.08-.087L9.264 2.02Zm-1.15 3.036L3.33 9.839v2.828h2.83l4.782-4.783-2.828-2.829v.001Zm2.092-2.093-1.15 1.15 2.828 2.829 1.151-1.15-2.829-2.83Z"
                            fill="currentColor"></path>
                        </g>
                        <defs>
                          <clipPath id="SvgIc-24-edit_svg__a">
                            <path fill="#fff" d="M0 0h16v16H0z"></path>
                          </clipPath>
                        </defs>
                      </svg>
                      <span>重命名</span>
                    </li>
                    <li @click.stop="handleDelete(item)">
                      <svg width="20" height="20" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"
                        focusable="false" class="chakra-icon css-147zza3" id="SvgIc-2j">
                        <g clip-path="url(#SvgIc-2j-trash_svg__a)">
                          <path
                            d="M9.52 1.333a1.333 1.333 0 0 1 1.265.912l.362 1.088h2.186a.667.667 0 1 1 0 1.333l-.002.048-.578 8.095a2 2 0 0 1-1.994 1.857H5.24a2 2 0 0 1-1.994-1.857l-.578-8.096a.667.667 0 0 1-.002-.047.667.667 0 0 1 0-1.333h2.186l.362-1.088a1.333 1.333 0 0 1 1.266-.912H9.52Zm2.478 3.333H4.002l.575 8.048a.667.667 0 0 0 .664.619h5.518a.667.667 0 0 0 .664-.62l.575-8.047Zm-5.331 2a.667.667 0 0 1 .662.589l.004.078v3.333a.667.667 0 0 1-1.328.078L6 10.666V7.333a.667.667 0 0 1 .667-.667Zm2.666 0a.667.667 0 0 1 .667.667v3.333a.666.666 0 1 1-1.333 0V7.333a.667.667 0 0 1 .666-.667Zm.187-4H6.48l-.222.667h3.484l-.222-.667Z"
                            fill="currentColor"></path>
                        </g>
                        <defs>
                          <clipPath id="SvgIc-2j-trash_svg__a">
                            <path fill="#fff" d="M0 0h16v16H0z"></path>
                          </clipPath>
                        </defs>
                      </svg>
                      <span>删除</span>
                    </li>
                  </ul>

                </el-popover>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="left_user">
        <div class="driver"></div>
        <div class="user_info">
          <img class="user_img" :src="userInfo.getAvatar()" alt="" />
          <el-popover placement="top" :width="160" trigger="hover" :show-arrow="false">
            <!-- <p>Are you sure to delete this?</p> -->
            <!-- <div style="text-align: right; margin: 0">
                            <el-button size="small" text @click="visible = false">cancel</el-button>
                            <el-button size="small" type="primary" @click="visible = false">
                                confirm
                            </el-button>
                        </div> -->
            <div class="tip_p">
              <div class="userIn" @click="goUserCenter">
                <el-icon>
                  <User />
                </el-icon>
                <div>个人中心</div>
              </div>
              <div class="exit" @click="exitFun">
                <el-icon>
                  <SwitchButton />
                </el-icon>
                <div>退出登录</div>
              </div>
            </div>
            <template #reference>
              <!-- <el-button @click="visible = true">Delete</el-button> -->
              <span class="userName_cla">{{ userInfo.userInfo.userName }}</span>
            </template>
          </el-popover>
        </div>
      </div>
    </div>
    <div class="main_right">
      <!-- 个人中心 -->
      <UserCenter ref="userCenter"></UserCenter>
      <!-- 聊天记录 -->
      <ChatHistory ref="historyDialog" @update:historyList="updataHistory" @delete:historyList="deleteHistory"
        @goDetail="queryDetail"></ChatHistory>
      <div class="main_fix">
        <div class="main_lz_top">
          <div class="main_lz_gr">
            <img style="width: 20px;margin-right: 6px;" src="../assets/logo.png" alt="" />
            <div class="top_name">量子AI智能体</div>
            <el-icon style="margin-left: 6px; font-weight: 700">
              <ArrowDown />
            </el-icon>
            <div class="main_lz_sh">
              <div class="group_select" @click="newDialogue">
                <el-icon class="icon_cl">
                  <CirclePlus />
                </el-icon>
                <div class="new_font">新建对话</div>
              </div>
              <div :class="{ group_select: isNew ? false : true, group_select_dis: isNew ? true : false }"
                @click="handleRename(userInfo.dialogue)">
                <el-icon class="icon_cl">
                  <Document />
                </el-icon>
                <div class="new_font">重命名</div>
              </div>
              <div :class="{ group_select: isNew ? false : true, group_select_dis: isNew ? true : false }">
                <el-icon class="icon_cl">
                  <Delete />
                </el-icon>
                <div class="new_font" @click="clearHistory()">清空历史</div>
              </div>
              <!-- <div class="group_select">
                <el-icon class="icon_cl">
                  <Position />
                </el-icon>
                <div class="new_font">分享会话</div>
              </div> -->
              <!-- <div class="group_select"><el-icon><Document /></el-icon><div>重命名</div></div>
              <div class="group_select">清空历史</div>
              <div class="group_select">分享会话</div> -->
            </div>
          </div>
        </div>

        <div class="main_top">
          <el-icon style="
              position: absolute;
              top: 40px;
              left: calc(50% - 8px);
              font-size: 16px;
            " class="is-loading" v-show="chatLoading">
            <Loading />
          </el-icon>
          <!-- 上拉加载控件 -->
          <div @scroll="handleScroll" ref="scrollContainer" class="scrollbar-hidden">
            <div v-for="(item, index) in listChat" :key="item">
              <div class="main_left_i" v-show="item.role == 'system'">
                <img style="width: 40px; height: 40px; border-radius: 50%" src="../assets/logo.png" alt="" />
                <div class="main_left_hua">
                  <div v-show="senfFlagLoading && index == listChat.length - 1" style="
                      /* position: absolute; */
                      top: 10px;
                      left: 35px;
                      font-size: 16px;
                      display: flex;
                      align-items: center;
                      margin-bottom: 10px;
                    ">
                    <el-icon style="
                      
                     
                    " class="is-loading">
                      <Loading />

                    </el-icon>
                    <div class="creat_cla">生成中...</div>
                  </div>
                  <div class="think_icon" @click="spreadFun(item)" v-show="!item.isHello">think<el-icon
                      v-show="item.isSpread">
                      <ArrowUp />
                    </el-icon><el-icon v-show="!item.isSpread">
                      <ArrowDown />
                    </el-icon></div>
                  <!-- {{item.content}} -->
                  <div class="think_cla" v-if="item.think.length > 0 && item.isSpread">
                    <MarkdownRenderer :markdown="item.think" class="think_cla" />
                  </div>
                  <MarkdownRenderer :markdown="item.content" class="zw_cla" />
                  <div class="button_group" v-show="index == listChat.length - 1 && !isOut && !senfFlagLoading && !item.isHello
                    ">
                    <div class="g_copy">
                      <div>
                        <!-- <button :data-clipboard-text="item.content" class="copy-button">复制</button> -->
                        <el-tooltip class="box-item" effect="dark" content="复制" placement="top-start">
                          <img :data-clipboard-text="item.content" class="copy-button" style="
                              width: 20px;
                              cursor: pointer;
                              margin-right: 10px;
                            " src="../assets/copy.png" alt="" />
                        </el-tooltip>
                      </div>
                      <div>
                        <el-tooltip class="box-item" effect="dark" content="重新生成" placement="top-start">
                          <img style="
                              width: 20px;
                              margin-right: 10px;
                              cursor: pointer;
                            " @click="refreshRecord(item)" src="../assets/refresh.png" alt="" />
                        </el-tooltip>
                      </div>
                    </div>

                    <div class="g_zan">
                      <div v-show="item.endorse == '1'">
                        <img style="
                            width: 20px;
                            margin-right: 10px;
                            cursor: pointer;
                          " @click="praiseFun(item.id, '2')" src="../assets/zan.png" alt="" />
                      </div>
                      <div v-show="item.endorse == '2'">
                        <img style="width: 20px; cursor: pointer" :style="{
                          marginRight: item.endorse == '2' ? '0px' : '10px',
                        }" @click="praiseFun(item.id, '1')" src="../assets/isZan.png" alt="" />
                      </div>
                      <div v-show="item.endorse == '1'">
                        <img style="width: 20px; cursor: pointer" @click="oppose(item.id)" src="../assets/cai.png"
                          alt="" />
                      </div>
                      <div v-show="item.endorse == '3'">
                        <img style="width: 20px; cursor: pointer" @click="praiseFun(item.id, '1')"
                          src="../assets/isCai.png" alt="" />
                      </div>
                    </div>
                  </div>
                  <div class="button_group1" v-show="index != listChat.length - 1 && !item.isHello">
                    <div style="
                        display: flex;
                        background-color: #ffffff;
                        align-items: center;
                        border-radius: 40px;
                        justify-content: space-between;
                        padding: 10px;
                        box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
                      ">
                      <div>
                        <el-tooltip class="box-item" effect="dark" content="复制" placement="top-start">
                          <img :data-clipboard-text="item.content" class="copy-button" style="
                              width: 20px;
                              cursor: pointer;
                              margin-right: 10px;
                            " src="../assets/copy.png" alt="" />
                        </el-tooltip>
                      </div>
                      <div>
                        <el-tooltip class="box-item" effect="dark" content="重新生成" placement="top-start">
                          <img style="width: 20px; cursor: pointer" @click="refreshRecord(item)"
                            src="../assets/refresh.png" alt="" />
                        </el-tooltip>
                      </div>
                    </div>
                    <div class="dh_ll" :style="{
                      justifyContent:
                        item.endorse == '2' || item.endorse == '3'
                          ? 'center'
                          : 'space-between',
                    }">
                      <div v-show="item.endorse == '1'">
                        <img style="
                            width: 20px;
                            margin-right: 10px;
                            cursor: pointer;
                          " @click="praiseFun(item.id, '2')" src="../assets/zan.png" alt="" />
                      </div>
                      <div v-show="item.endorse == '2'">
                        <img style="width: 20px; cursor: pointer" :style="{
                          marginRight: item.endorse == '2' ? '0px' : '10px',
                        }" @click="praiseFun(item.id, '1')" src="../assets/isZan.png" alt="" />
                      </div>
                      <div v-show="item.endorse == '1'">
                        <img style="width: 20px; cursor: pointer" @click="oppose(item.id)" src="../assets/cai.png"
                          alt="" />
                      </div>
                      <div v-show="item.endorse == '3'">
                        <img style="width: 20px; cursor: pointer" @click="praiseFun(item.id, '1')"
                          src="../assets/isCai.png" alt="" />
                      </div>
                    </div>
                  </div>
                  <!--  -->
                  <div  v-show="senfFlagLoading && index == listChat.length - 1" style="
                      /* position: absolute; */
                      top: 10px;
                      left: 35px;
                      font-size: 16px;
                      display: flex;
                      align-items: center;
                      margin-bottom: 10px;
                    ">
                    <div class="stop_cla" @click="stop_fun(item)">停止生成</div>
                  </div>
                </div>
              </div>
              <div class="main_right_i" v-show="item.role == 'user'">
                <div class="main_left_hua">
                  {{ item.content }}
                </div>
                <img style="width: 40px; height: 40px; border-radius: 50%" :src="userInfo.getAvatar()" alt="" />
              </div>
              <div class="img_pre" v-show="item.role == 'user' && !(item.image == null || item.image == '')">
                <img class="img_chat" :src="item.image" @click="imageFun(item.image)" alt="" />
              </div>
            </div>
          </div>
        </div>
        <div class="main_bottom_in">
          <div class="main_text">
            <div v-if="previewImage" class="preview-container">
              <img :src="previewImage" alt="Preview" />
              <!-- 关闭按钮 -->
              <button class="close-button" @click="removeImage">×</button>
            </div>
            <el-input @keydown.enter.native="handleSend" v-model="sendData" type="textarea"
              :autosize="{ minRows: 1, maxRows: 6 }" placeholder="Enter发送  Shift+Enter换行" resize="none">
            </el-input>
            <div class="bottom_send">
              <div>
                <el-button type="primary" :icon="FolderAdd" circle @click="triggerFileInput" />
              </div>
              <div>
                <el-button :disabled="chatLoading || sendData.length == 0 || isOut" @click="sendChat()" type="primary"
                  style="margin-left: 1rem" :icon="Promotion" circle />
              </div>
            </div>
          </div>
          <input type="file" ref="fileInput" class="hidden-input" @change="handleFileChange" accept="image/*"
            multiple />
          <div class="ai_tip">内容由AI生成,请核查重要信息</div>
        </div>
      </div>
    </div>
    <!-- 重命名弹窗 -->
    <EditDialog ref="editDialog" :currentRecord="renameObj" @update:historyList="updataHistory"
      @delete:historyList="deleteHistory"></EditDialog>
  </div>
  <PreImage ref="preImage" />
  <ResultFeedback ref="resultOppose" @call-parent-method="parentMethod" />
</template>

<style lang="scss" scoped>
.userName_cla{
    width: 80%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.stop_cla{
  background-color: #ececec;
  color: #706f6f;
  padding:5px 10px;
  border-radius: 5px;
  font-size: 12px;
  position: absolute;
  bottom: -32px;
  left: 0px;
  cursor: pointer;
  
}


.custom-loading .circular .path {
  animation: loading-dash 1.5s ease-in-out infinite;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 2;
  stroke: var(--el-button-text-color);
  stroke-linecap: round;
}

.img_pre {
  text-align: right;

  .img_chat {
    max-width: 60px;
    max-height: 60px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 5px;
    margin-right: 50px;
    right: 10px;
  }
}

.preview-container {
  position: relative;
  display: inline-block;

  img {
    max-width: 60px;
    max-height: 60px;
    margin-top: 20px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 5px;
  }

  .close-button {
    position: absolute;
    display: flex;
    top: 10px;
    right: -4px;
    background-color: rgba(117, 117, 117, 0.7);
    color: white;
    border: none;
    border-radius: 14px;
    width: 14px;
    height: 14px;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 12px;
    //   line-height: 16px;
    padding: 10px;
  }
}

.hidden-input {
  display: none;
  /* 隐藏文件输入框 */
}

.active {
  background-color: rgba(0, 0, 0, 0.02);
  color: #333333;
  font-weight: bold;
}

.tip_p {
  width: 100%;
  // background-color: #333333;
  display: flex;
  flex-direction: column;
  align-items: center;

  .exit {
    // line-height: 30px;
    width: 100%;
    box-sizing: border-box;
    cursor: pointer;
    padding: 1rem 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .exit:hover {
    // line-height: 30px;
    // background-color: #000000;
    background-color: rgba(0, 0, 0, 0.17);
    border-radius: 0.4rem;
  }

  .userIn {
    width: 100%;
    padding: 1rem 1.5rem;
    box-sizing: border-box;
    // line-height: 30px;

    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .userIn:hover {
    background-color: rgba(0, 0, 0, 0.17);
    border-radius: 0.4rem;
  }
}

.infinite-list {
  // background-color: aqua;
  // height: 100px;
  height: calc(100% - 20px);
  // height:calc(40vh  - 10px) ;
  // height: 100px;
  // flex: 1;
  // overflow: auto;
  overflow-y: auto;

  // flex: 1;
  .history_list_cycle {
    color: rgba(0, 0, 0, 0.5);
    font-size: 0.9rem;
    font-weight: 700;
  }

  .history_list_title {
    display: flex;
    align-items: center;
    color: #333333;
    font-size: 0.8rem;
    cursor: pointer;
    box-sizing: border-box;
    padding: 0 10px;
    line-height: 40px;
    border-radius: 5px;

    span {
      display: inline-block;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .more_btn {
      // display: none;
      visibility: hidden;
      width: 28px;
      height: 28px;
      line-height: 22px;
      text-align: center;
      background: rgb(244, 246, 248);
      border-radius: 4px;

      &:hover {
        background: rgb(223, 223, 223);
      }
    }

    &:hover {
      .more_btn {
        // display: block;
        visibility: visible;
      }
    }
  }

  .history_list_title:hover {
    color: #333333;
    font-size: 0.8rem;
    cursor: pointer;
    background-color: rgba(233, 143, 218, 0.1);
    color: #333333;
    font-weight: bold;
  }

  // overflow-y: auto;
  .infinite-list-item {
    font-size: 0.8rem;
    line-height: 40px;
    padding-left: 10px;
    border-radius: 4px;
    color: #333333;
    cursor: pointer;
  }

  .infinite-list-item:hover {
    // line-height: 40px;
    // background-color: rgba(233, 143, 218, 0.1);
    // color: #333333;
    // font-weight: bold;
  }
}

/* 尝试隐藏滚动条 */
.scrollbar-hidden {
  height: 100%;
  // height: 100vh;
  overflow-y: auto;
  overflow-anchor: none;
  scrollbar-gutter: stable;
  will-change: transform;
  /* Firefox */
  // -ms-overflow-style: none;
  /* Internet Explorer 10+ */
}

/* 针对 WebKit 浏览器隐藏滚动条 */
// .scrollbar-hidden::-webkit-scrollbar {
//     display: none;
// }

.ai_main {
  width: 100vw;
  height: 100vh;
  // background: linear-gradient(to bottom, rgba(199, 253, 191, 0.4), rgb(226, 225, 225));
  display: flex;

  .main_left {
    width: 240px;
    height: 100vh;
    background-color: rgb(255, 255, 255);
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .ai_main_title {
      width: 100%;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;

      .img_cla {
        margin-top: 40px;
        margin-bottom: 10px;
        width: 60px;
      }

      .title {
        font-weight: 700;
      }

      .build_yuyan {
        width: 80%;
        cursor: pointer;
        margin-top: 1rem;
        background-color: aqua;
        // padding: 0.5rem 2rem;
        padding: 0.5rem 0rem;
        border-radius: 2rem;
        color: #ffffff;
        font-weight: 700;
        background: linear-gradient(90deg, rgb(119, 76, 255) 0%, rgb(172, 81, 255) 100%);
        font-size: 0.9rem;
      }
    }

    .search_input {
      margin-top: 2rem;
      width: 100%;
      text-align: center;

      :deep(.el-input-group__prepend) {
        box-shadow: none;
        background-color: rgb(245, 241, 241);
      }

      :deep(.el-input__wrapper) {
        box-shadow: none;
        background-color: rgb(245, 241, 241);
      }

      :deep(.el-input__inner) {
        font-size: small;
      }

      .input_com {
        width: 80%;
      }
    }

    .history {
      padding-top: 20px;
      flex: 1;
      width: 100%;
      display: flex;
      // justify-content: center;
      align-items: center;
      flex-direction: column;
      overflow: hidden;

      .history_list {
        flex: 1;
        width: 80%;
        background-color: rgb(255, 255, 255);

        display: flex;
        height: 100%;
        flex-direction: column;
      }
    }

    .left_user {
      width: 100%;
      display: flex;
      align-items: center;
      flex-direction: column;

      .driver {
        width: 100%;
        height: 1px;
        background-color: rgba(0, 0, 0, 0.3);
      }

      .user_info {
        width: 80%;
        // background-color: aqua;
        height: 3rem;
        display: flex;
        flex-direction: row;
        align-items: center;

        .user_img {
          border-radius: 50%;
          cursor: pointer;
          height: 30px;
          width: 30px;
          margin-right: 4px;
        }
      }
    }
  }

  .main_right {
    flex: 1;
    height: 100vh;
    // background: linear-gradient(to bottom, rgba(34, 255, 0, 0.3) 20vh, rgb(226, 225, 225) 80vh);
    background: url("../assets/char_bg.png") no-repeat;
    background-size: cover;
    display: flex;
    justify-content: center;

    .main_fix {
      height: 100vh;
      width: 768px;
      // background-color: #ffffff;
      display: flex;
      overflow: hidden;
      flex-direction: column;

      .main_lz_top {
        display: inline-flex;
        justify-content: center;
        align-items: center;

        // line-height: 60px;
        height: 60px;
        // background-color: #333333;
        position: relative;

        .main_lz_gr {
          // background-color: #007bff;
          display: flex;
          align-items: center;
          position: relative;

          .main_lz_sh {
            position: absolute;
            // background-color: #007bff;
            // padding:20px 40px;
            // box-sizing: border-box;
            visibility: hidden;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            // top: 100px;
          }
        }

        .main_lz_gr:hover {
          // background-color: #007bff;
          display: flex;
          align-items: center;
          position: relative;

          .main_lz_sh {
            position: absolute;
            background-color: #ffffff;
            // padding:20px 40px;
            // box-sizing: border-box;
            visibility: visible;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            z-index: 1000;
            // width: 80%;
            // width: 400px;
            // width: auto;
            // top: 100px;
            padding: 10px 10px;
            display: flex;
            flex-direction: column;
            border-radius: 10px;

            .group_select_dis {
              box-sizing: border-box;
              display: flex;
              // width: 100%;
              font-size: 14px;
              padding: 4px 10px;
              display: flex;
              align-items: center;
              white-space: nowrap;
              padding: 10px 10px;

              .icon_cl {
                margin-right: 10px;
                color: #d6d4d4;
              }

              .new_font {
                color: #d6d4d4;
              }
            }

            .group_select {
              box-sizing: border-box;
              display: flex;
              // width: 100%;
              font-size: 14px;
              padding: 4px 10px;
              display: flex;
              align-items: center;
              white-space: nowrap;
              padding: 10px 10px;

              // justify-content: space-between;
              .icon_cl {
                margin-right: 10px;
              }

              .new_font {
                color: #333333;
              }
            }

            .group_select:hover {
              cursor: pointer;
              box-sizing: border-box;
              background-color: #ebe8e8;
              border-radius: 6px;
              display: flex;
              // width: 100%;
              font-size: 14px;
              padding: 4px 10px;
              display: flex;
              align-items: center;
              white-space: nowrap;
              padding: 10px 10px;

              // justify-content: space-between;
              .icon_cl {
                margin-right: 10px;
              }

              .new_font {
                color: #333333;
              }
            }
          }
        }

        .top_name {
          font-weight: 700;
        }
      }

      .main_top {
        position: relative;
        padding-top: 4rem;
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        :deep(::-webkit-scrollbar) {
          width: 0px !important;
        }

        .main_left_i {
          display: flex;
          width: 100%;
          padding: 2rem 0rem;

          .main_left_hua {
            font-size: 0.9rem;
            position: relative;
            will-change: transform;
            max-width: calc(100% - 180px);
            min-width: calc(60px);
            // width: calc(100% - 180px);
            padding: 1rem;
            overflow-wrap: break-word;
            margin-left: 1rem;
            box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
            background-color: #ffffff;
            // border-top-left-radius: 1rem;
            border-top-right-radius: 1rem;
            border-bottom-right-radius: 1rem;
            border-bottom-left-radius: 1rem;
            color: #333333;
            margin-bottom: 40px;

            // max-height: 100vh;
            // overflow-y: auto;
            // height: 10000px;
            // overflow-y: auto;
            .think_cla {
              background-color: #7c7b7b;
              color: #ebebeb;
              border-radius: 4px;
              padding: 2px 4px;
              line-height: 26px;
              letter-spacing: 1x;
              // margin-top: 20px;
              word-wrap: break-word;
              overflow-wrap: break-word;
              white-space: pre-wrap;
              word-break: break-all;
             
              hyphens: auto;
              overflow-x: auto;

            }

            .zw_cla {
              line-height: 26px;
              letter-spacing: 2x;
              // width: 100px;
              word-wrap: break-word;
              overflow-wrap: break-word;
              white-space: pre-wrap;
              word-break: break-all;
             
              hyphens: auto;
              overflow-x: auto;
            }

            .creat_cla {
              font-size: 14px;
              color: #7e7c7c;
             

            }

            .think_icon {
              display: inline-flex;
              align-items: center;
              // background-color: #333333;
              background-color: #7c7b7b;
              border-radius: 4px;
              // color: #b8b7b7;
              color: #ebebeb;
              padding: 4px 10px;
              cursor: pointer;
              margin-bottom: 10px;
            }

            .button_group {
              display: flex;
              flex-direction: row;
              justify-content: space-between;

              .g_copy {
                display: flex;
              }

              .g_zan {
                display: flex;
              }
            }

            .button_group1 {
              position: absolute;
              bottom: -20px;
              margin-left: -1rem;
              display: flex;
              width: 100%;
              justify-content: space-between;
              visibility: hidden;
            }
          }

          .main_left_hua:hover {
            font-size: 0.9rem;
            position: relative;
            will-change: transform;
            max-width: calc(100% - 180px);
            min-width: calc(60px);
            // width: calc(100% - 180px);
            padding: 1rem;
            overflow-wrap: break-word;
            margin-left: 1rem;
            box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
            background-color: #ffffff;
            // border-top-left-radius: 1rem;
            border-top-right-radius: 1rem;
            border-bottom-right-radius: 1rem;
            border-bottom-left-radius: 1rem;
            color: #333333;

            // max-height: 100vh;
            // overflow-y: auto;
            // height: 10000px;
            // overflow-y: auto;
            .button_group1 {
              // box-sizing: border-box;
              // position: absolute;
              // bottom: -20px;
              // width: 100%;
              // // padding: 1rem;
              // box-sizing: border-box;
              // display: flex;
              margin-left: -1rem;
              // visibility: visible;
              // background-color: aqua;
              position: absolute;
              bottom: -20px;
              visibility: visible;
              display: flex;
              width: 100%;
              justify-content: space-between;
            }
          }
        }

        .main_right_i {
          margin-top: 10px;
          display: flex;
          justify-content: flex-end;
          padding: 2rem 0rem;

          .main_left_hua {
            font-size: 0.9rem;
            box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
            max-width: calc(100% - 180px);
            padding: 1rem;
            overflow-wrap: break-word;
            margin-right: 1rem;
            background-color: #ffffff;
            border-top-left-radius: 1rem;
            // border-top-right-radius: 1rem;
            border-bottom-right-radius: 1rem;
            border-bottom-left-radius: 1rem;
            color: #333333;
          }
        }
      }

      .main_bottom_in {
        padding-bottom: 1rem;

        :deep(.el-textarea__inner) {
          // height: 60px;
          // line-height: 60px;
          border: none !important;
          box-shadow: none !important;
          padding: 5px 0 !important;
          resize: none !important;
        }

        :deep(.el-textarea__inner) {
          // border-radius: 1rem;
        }

        .ai_tip {
          margin-top: 1rem;
          // line-height: 1rem;
          text-align: center;
          color: rgba(0, 0, 0, 0.4);
          font-size: 0.8rem;
        }

        .main_text {
          position: relative;
          width: 100%;
          // height: 100%;
          background-color: rgb(255, 255, 255);
          box-sizing: border-box;
          padding: 1rem 2rem;
          border-radius: 1rem;

          .bottom_send {
            display: flex;
            position: absolute;
            right: 2rem;
            bottom: 1rem;
          }
        }
      }
    }
  }
}

.icon_history {
  cursor: pointer;
  margin-top: 7px;

  &:hover {
    path {
      fill: #7d4dff;
    }
  }
}

.dh_ll {
  display: flex;
  background-color: #ffffff;
  align-items: center;
  border-radius: 40px;
  // justify-content: space-between;
  justify-content: center;
  padding: 10px;
  box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
}

/*历史记录更多操作 */
.more_opration {
  padding: 0;
  margin: 0;

  li {
    padding: 12px 14px;
    margin: 0;
    line-height: 30px;
    list-style: none;
    display: flex;
    align-items: center;
    text-align: center;
    border-radius: 4px;
    transition: all 0.3s ease-in-out;
    cursor: pointer;

    &:hover {
      background-color: rgba(17, 24, 36, 0.05);
      color: #663ed3;
    }

    span {
      line-height: 20px;
    }
  }

  .chakra-icon {
    width: 20px;
    height: 20px;
    margin-right: 6px;
  }
}
</style>