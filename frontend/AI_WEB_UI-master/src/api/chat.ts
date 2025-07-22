import { http } from "@/utils/http";

const sendChatUrl = "/dialogue/saveChat";
const queryChatUrl = "/chat/findChats";
const queryHistoryUrl = "/dialogue/findDialogues";
const modHistoryUrl = "/dialogue/updateDialogues";

const giveLikeUrl = '/chat/updateChatHistory'
const refreshUrl ='/chat/refresh'

const clearHistoryUrl ='/chat/clearChats'

const stopUrl ='/dialogue/stop'
// export const sendChatApi = (val: any) => {
//     return http<any>(
//         {
//             method: 'post',
//             url: sendChatUrl,
//             data: { ...val }
//         })
// }
export const sendChatApi = (val: any, showLoading: boolean = false) => {
  return http<any>({
    method: "post",
    url: sendChatUrl,
    data: val,
    headers: { "Content-Type": "multipart/form-data", showLoading },
  });
};
export const queryChatApi = (val: any) => {
  return http<any>({
    method: "get",
    url: `${queryChatUrl}?dialogueId=${val.dialogueId}&pageNum=${val.pageNum}&pageSize=${val.pageSize}`,
  });
};

export const queryHistoryApi = (val: any) => {
  return http<any>({
    method: "get",
    url: `${queryHistoryUrl}?search=${val.search}&pageNum=${val.pageNum}&pageSize=${val.pageSize}`,
  });
};

export const modHistoryApi = (val: any) => {
  return http<any>({
    method: "post",
    url: modHistoryUrl,
    data: { ...val },
  });
};
export const giveLikeApi =(val:any)=>{
  return http<any>({
    method:"post",
    url: giveLikeUrl,
    data:{...val}
  })
}

export const refreshApi =(val:any)=>{
  return http<any>({
    method:"post",
    url: refreshUrl,
    data:{...val}
  })
}

export const clearHistoryApi =(val:any)=>{
  return http<any>({
    method:"get",
    url: `${clearHistoryUrl}?dialogueId=${val}`,
  })
}

export const stopApi =(val:any)=>{
  return http<any>({
    method:"get",
    url: `${stopUrl}/${val}`,
  })
}
