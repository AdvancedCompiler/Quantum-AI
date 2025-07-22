<script setup lang='ts'>
import { ref } from 'vue'
// import {Close} from 'element-plus'
import {
    Close
} from "@element-plus/icons-vue";
const resultFlag = ref<boolean>(false)
const textarea = ref<string>('')

//id传过来
const id = ref<string>('')
const open = (val: string) => {
    resultFlag.value = true;
    id.value = val
}

const closeFun = () => {
    //点击关闭  窗口
    textarea.value = ''
    id.value = ''
    resultFlag.value = false;
}
//点击提交按钮
const emit = defineEmits()

const subMit = () => {
    //调用父组件方法//
    //将整个对象再传输回去

    //调用父组件的代码
    emit('call-parent-method', { id: id.value, textarea: textarea.value })
    closeFun()
}


defineExpose({ open })
</script>
<template>

    <div class="result">
        <el-dialog v-model="resultFlag" :show-close="false" width="500">
            <template #header="{ close, titleId, titleClass }">
                <div class="my-header">
                    <div class="header_result">
                        <img style="width: 20px;height: 20px;" src="../assets/isCai.png" alt="">
                        <div class="result_fon">结果反馈</div>
                        <div class="result_close">
                            <div class="result_im" @click="closeFun"><el-icon>
                                    <Close />
                                </el-icon></div>
                        </div>
                    </div>
                    <div class="middle_result">
                        <el-input v-model="textarea" style="width: 100%" :rows="10" type="textarea"
                            placeholder="输入你觉得回答不满意的地方" />
                    </div>
                    <div class="foot_result">
                        <div class="foot_close" @click="closeFun">关闭</div>
                        <div class="foot_sub" @click="subMit">提交反馈</div>
                    </div>
                </div>
            </template>

        </el-dialog>

    </div>

</template>
<style lang="scss" scoped>
.result {
    :deep(.el-dialog) {
        padding: 0px !important;
        border-radius: 8px !important;
    }

    .header_result {
        // border-radius: 8px !important;
        border-top-left-radius: 8px;
        border-top-right-radius: 8px;
        background-color: rgb(251, 251, 252);
        display: flex;
        line-height: 50px;
        align-items: center;
        padding: 0px 20px;

        .result_fon {
            color: #333333;
            margin-left: 10px;
            font-size: 22px;
            // font-weight: 700;
        }

        .result_close {
            flex: 1;
            display: flex;
            justify-content: end;

            .result_im {
                font-size: 20px;
                font-weight: 100;
                width: 35px;
                height: 35px;
                border-radius: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
            }

            .result_im:hover {
                font-size: 16px;
                font-weight: 100;
                background-color: #c5c3c3;


            }
        }
    }

    .middle_result {
        padding: 0px 20px;
        margin-top: 20px;
    }

    .foot_result {
        padding: 0px 20px;
        margin-top: 20px;
        display: flex;
        justify-content: end;

        .foot_close {
            background-color: #ebebeb;
            padding: 8px 16px;
            border-radius: 4px;
            margin-right: 20px;
            // font-weight: 100;
            color: #333333;
            cursor: pointer;
        }

        .foot_sub {
            background-color: #7d4dff;
            padding: 8px 16px;
            border-radius: 4px;
            color: #ffffff;
            cursor: pointer;
        }
    }
}
</style>