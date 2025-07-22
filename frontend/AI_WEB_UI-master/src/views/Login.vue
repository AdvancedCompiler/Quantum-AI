<script setup lang='ts'>
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { md5 } from 'js-md5';
import { loginApi, loginTmpApi, registerApi } from '@/api/login';
import { userInfoStore } from '@/stores'
import { publicIpv4 } from 'public-ip';
const userInfo = userInfoStore()
const router = useRouter()
const ruleFormRef = ref<any>()
const ruleForm = ref<any>({
    password: '',
    phone: '',
})
//校验手机号   登录的时候
const checkPhone = (rule: any, value: any, callback: any) => {
    if (!value) {
        return callback(new Error('请输入手机号'))
    } else if (!(/^1[3456789]\d{9}$/.test(value))) {
        callback(new Error('请输入正确的手机号'))
    } else {
        callback()
    }

}
//校验密码    登录的时候
const validatePass = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入密码'))
    } else if (value.length < 6) {

        callback(new Error("密码需要大于等于6位"))
    } else {
        callback()
    }
}

//校验用户   注册的时候
const validateUserName = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入用户名'))
    } else {

        callback()
    }
}
//校验手机号   注册的时候
const validateRegisterPhone = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入手机号'))
    } else if (!(/^1[3456789]\d{9}$/.test(value))) {
        callback(new Error('请输入正确的手机号'))
    } {
        callback()
    }
}
//校验密码   注册的时候
const validateRegisterPassword = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入密码'))
    } else if (value.length < 6) {

        callback(new Error("密码需要大于等于6位"))
    } {
        callback()
    }
}

//校验密码   注册的时候
const validateRegisterRePassword = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入确认密码'))
    } else if (value.length < 6) {

        callback(new Error("密码需要大于等于6位"))
    } else if (value != registerForm.value.password) {
        callback(new Error('两次密码需要一致'))
    } else {
        callback()
    }
}


const rules = ref<FormRules<typeof ruleForm>>({
    password: [{ validator: validatePass, trigger: 'blur' }],
    phone: [{ validator: checkPhone, trigger: 'blur' }],
})

const submitForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            console.log('submit!')
            let params = { ...ruleForm.value }
            params.password = md5(params.password)
            //调用登录
            loginMethod(params)
        } else {
            console.log('error submit!')
        }
    })
}
//用户登录接口
const loginMethod = async (val: any) => {
    const res: any = await loginApi(val)

    if (res.code == 200) {
        userInfo.saveToken(res.data.token)
        userInfo.saveUserInfo(res.data.user)
        //跳转到 首页
        router.push({
            name: 'home'
        })
    } else {
        ElMessage({
            message: res.msg,
            type: 'error'
        })
    }
}
// const resetForm = (formEl: FormInstance | undefined) => {
//     if (!formEl) return
//     formEl.resetFields()
// }


const login_temporarily = async () => {
    //临时账号进行登录
    //跳转到问答页面
    // let params ={
    //     phone:18595924671,
    //     password: md5('123456')
    // }

    const ip = await publicIpv4()

    const res: any = await loginTmpApi(ip)
    // console.log(res)
    if (res.code == 200) {
        userInfo.saveToken(res.data.token)
        userInfo.saveUserInfo(res.data.user)
        //跳转到 首页
        router.push({
            name: 'home'
        })
    } else {
        ElMessage({
            message: res.msg,
            type: 'error'
        })
    }
    // loginMethod(params)
    // router.push({ name: 'home' })
}

//注册所需要的字段
//ref标志
const registerRef = ref<any>()
//字段form
const registerForm = ref<any>({
    userName: '',
    phone: '',
    password: '',
    rePassword: ""
})
//注册字段
const registerRules = ref<any>({
    userName: [{ validator: validateUserName, trigger: 'blur' }],
    phone: [{ validator: validateRegisterPhone, trigger: 'blur' }],
    password: [{ validator: validateRegisterPassword, trigger: 'blur' }],
    rePassword: [{ validator: validateRegisterRePassword, trigger: 'blur' }]
})
//注册接口
const registerSubmit = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            console.log('submit!')
            let params = { ...registerForm.value }
            params.password = md5(params.password)
            //开始调用注册接口
            registerMethod(params)
        } else {
            console.log('error submit!')
        }
    })
}
const registerMethod = async (val: any) => {
    const res: any = await registerApi(val)
    console.log(res)
    if (res.code == 200) {
        ElMessage({
            type: 'success',
            message: res.msg
        })
        registerFlag.value = false
    } else {
        ElMessage({
            type: 'info',
            message: res.msg
        })
    }

}
//打开页面注册页面的标志
const registerFlag = ref<boolean>(false)

//打开注册页面的方法
const registerFun = () => {
    registerFlag.value = true
}
//关闭注册页面
const cancleRegister = (formEl: FormInstance | undefined) => {
    //清除校验
    if (!formEl) return
    formEl.resetFields()
    registerFlag.value = false

}


//在这个页面的初始化方法
onMounted(() => {
    //初次进入这个页面应该将所有的信息进行查清空
    userInfo.clearActiveIndex()
    userInfo.saveDialogueId('')
}) 
</script>
<template>
    <div class="login_main">
        <div class="login_second">
            <div class="login_left">
                <div class="login_left_title">智能解析.深度洞察</div>
                <div class="login_left_title_sub">基于50万+论文的智能问答引擎</div>
                <div class="login_product">先进实验室研发</div>
            </div>
            <div class="login_right">

                <div class="main_right">
                    <div class="wellcom">欢迎使用</div>
                    <div class="wellcom">量子AI智能体</div>
                    <div class="login_input">
                        <el-form ref="ruleFormRef" style="width: 100%;" :model="ruleForm" status-icon :rules="rules"
                            label-width="auto" class="demo-ruleForm">
                            <el-form-item label="" prop="phone">
                                <el-input v-model="ruleForm.phone" placeholder="请输入手机号" autocomplete="off" />
                            </el-form-item>
                            <el-form-item label="" prop="password">
                                <el-input v-model="ruleForm.password" type="password" show-password
                                    placeholder="请输入密码" />
                            </el-form-item>

                            <el-form-item>
                                <div class="login_bu" @click="submitForm(ruleFormRef)">登录</div>
                                <!-- <el-button type="primary" @click="submitForm(ruleFormRef)">
                                    Submit
                                </el-button> -->
                                <!-- <el-button @click="resetForm(ruleFormRef)">Reset</el-button> -->
                            </el-form-item>
                        </el-form>

                    </div>
                    <div class="login_bottom">
                        <div class="forget">&nbsp;</div>
                        <!-- <div class="forget">忘记密码</div> -->
                        <div class="register" @click="registerFun">立即注册</div>
                    </div>
                    <div class="quikly">
                        <el-divider>

                            <span class="yanshi"> 体验账号快速登录</span>
                        </el-divider>
                        <div style="width: 100%;text-align: center;">
                            <el-button style="width:40%" @click="login_temporarily">体验账号快速登录</el-button>
                        </div>
                        <!-- <el-button style="width:40%">演示账号快速登录</el-button> -->
                    </div>

                </div>
            </div>
        </div>
    </div>
    <div class="dialog_out">
        <el-dialog v-model="registerFlag" :show-close="false" width="500">
            <template #header="{ close, titleId, titleClass }">
                <div class="register_top">
                    注册
                </div>
            </template>
            <div class="register_middle">
                <el-form ref="registerRef" style="width: 100%;" :model="registerForm" status-icon :rules="registerRules"
                    label-width="auto" class="demo-ruleForm">
                    <el-form-item label="" prop="userName">
                        <el-input v-model="registerForm.userName" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="" prop="phone">
                        <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
                    </el-form-item>
                    <el-form-item label="" prop="password">
                        <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item label="" prop="rePassword">
                        <el-input v-model="registerForm.rePassword" type="password" show-password placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item>
                        <div class="form_foot">
                            <div class="register_cancle" @click="cancleRegister(registerRef)">取消</div>
                            <div class="register_form" @click="registerSubmit(registerRef)"> 注册</div>
                        </div>
                        <!-- <el-button @click="cancleRegister(registerRef)">取消</el-button>
                        <el-button type="primary" @click="registerSubmit(registerRef)">
                            注册
                        </el-button> -->

                    </el-form-item>
                </el-form>
            </div>
        </el-dialog>
    </div>

</template>

<style scoped lang="scss">
.form_foot {
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: space-around;

    .register_cancle {
        display: flex;
        padding: 4px 20px;
        border: 1px solid rgba(145, 144, 144, 0.5);
        border-radius: 4px;
        cursor: pointer;
    }

    .register_form {
        display: flex;
        padding: 4px 20px;
        border: 1px solid rgba(0, 0, 0, 0.5);
        border-radius: 4px;
        cursor: pointer;
        background-color: rgba(0, 0, 0);
        color: #ffffff;
    }
}


.dialog_out {
    :deep(.el-dialog) {
        padding: 0px;
        border-radius: 2px;
    }

    :deep(.el-input__wrapper) {
        border: none;
        box-shadow: none;
        background-color: #e7e6e6;
        height: 40px;
        border-radius: 0.4rem;
    }
}

.register_top {
    line-height: 4rem;
    background-color: #222020;
    padding: 0rem 1rem;
    // border-radius: 4px;
    color: #ffffff;
    font-weight: 700;

}

.register_middle {
    padding: 1rem 1rem;
}


.login_main {
    height: 100vh;
    background: url(../assets/bg.png) no-repeat;
    background-position: center;
    object-fit: cover;
    background-size: cover;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100vw;
    // background-color: aquamarine;

    .login_second {
        display: flex;
        width: 80%;
        height: 70vh;

        // background-color: rgb(206, 106, 206);
        .login_left {
            display: flex;
            flex-direction: column;
            flex: 1;
            height: 100%;
            background: url(../assets/login.png) no-repeat;
            background-size: 100%;
            // object-fit: cover;
            object-fit: cover;
            background-position: center;
            background-color: #f7f7f7;

            .login_left_title {
                text-align: center;
                margin-top: 5%;
                font-weight: 700;
                font-size: 1.6rem;
                color: #333333;
            }

            .login_left_title_sub {
                color: #525151;
                margin-top: 2rem;
                text-align: center;
                font-size: 1.2rem;

            }

            .login_product {
                // bottom: 20px;
                bottom: 20px;
                color: #525151;
                margin-top: auto;
                margin-bottom: 60px;
                text-align: center;
                // text-align: center;
                font-size: 0.8rem;
            }
        }

        .login_right {
            flex: 1;
            height: 100%;
            background-color: rgb(255, 255, 255);

            .main_right {
                height: 100%;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;

                .wellcom {
                    width: 70%;
                    text-align: left;
                    font-size: 1.2rem;
                    font-weight: bold;
                    margin-bottom: 1rem;
                }

                .login_input {
                    width: 70%;

                    :deep(.el-input__inner) {
                        height: 3rem !important;
                        line-height: 3rem !important;
                        border-radius: 0.4rem;
                    }

                    :deep(.el-input__wrapper) {
                        border-radius: 1rem;
                        background-color: #e7e6e6;
                    }

                    .login_bu {
                        width: 100%;
                        background-color: #333333;
                        border-radius: 1rem;
                        line-height: 3rem;
                        color: #ffffff;
                        text-align: center;
                    }
                }

                .login_bottom {
                    width: 70%;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;

                    .forget {
                        color: #868282;
                        // font-weight: 700;
                    }

                    .register {
                        border: 1px solid #333333;
                        color: #333333;
                        padding: 0.6rem 1rem;
                        border-radius: 0.6rem;
                        cursor: pointer;
                    }
                }

                .quikly {
                    width: 70%;
                    display: flex;
                    flex-direction: column;

                    .yanshi {
                        color: #7c7b7b;
                    }
                }


            }
        }
    }
}
</style>