<script setup lang="ts">
// import {} from 'router'
import { ref } from 'vue'
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router'
import Foot from '@/components/Foot.vue';
import {userInfoStore} from '@/stores'

const userInfo =userInfoStore()
const router = useRouter()
// import TheWelcome from '../components/TheWelcome.vue'

const isHome = ref<boolean>(true)

const to_login = () => {
  //开始跳转到登录页面
  router.push({ name: 'login' })
}
const word = ref<string>('')
const startFun = () => {
  ElMessage({
    message: '尚未登录',
    type: 'warning'
  })

  //将你输入的文字存入缓存
  //如果点击开始对话
  userInfo.savePreChat(word.value)

  router.push({ name: 'login' })
}

//
const hotList = ref<any>([
  {name:'量子噪声研究如何提升比特稳定性？',index:1},
  {name:'如何优化量子态减少退相干效应？',index:2},
  {name:'过参数化如何影响量子神经网络？',index:3},
  {name:'如何评估量子神经网络模型的可扩展性？',index:4}
])

const selectList =(item:string)=>{
 word.value=item;
}

const homeChange = () => {
  isHome.value = !isHome.value
}



</script>

<template>
  <div>
    <div class="header_com">

      <div class="header_com_logo">Quantum AI</div>
      <div class="header_com_right">
        <div @click="homeChange"><span v-show="isHome">关于</span><span v-show="!isHome">首页</span></div>
        <div class="header_com_lo_bu" @click="to_login">登录</div>
      </div>

    </div>
    <div class="middle_com" v-show="isHome">
      <div class="middle_first">
        <div class="middle_first_left">
          <div class="title_parent">量子AI智能体</div>
          <div class="title_sub">基于50万+专业论文的智能问答引擎，引领量子科技新纪元</div>
          <div class="com_in">
            <el-input v-model="word" placeholder="请输入您的量子领域相关问题" />
            <div class="start_bu" @click="startFun">开始对话</div>
          </div>
          <div class="ts_parent">探索热门问题</div>
          <div class="question">
            <div class="question_sub" v-for="item,index in hotList" :key="index" @click="selectList(item.name)">{{item.name}}</div>
            <!-- <div class="question_sub">量子噪声研究如何提升比特稳定性？</div>
            <div class="question_sub">如何优化量子态减少退相干效应？</div>
            <div class="question_sub"> 过参数化如何影响量子神经网络？</div>
            <div class="question_sub">如何评估量子神经网络模型的可扩展性？</div> -->
          </div>

        </div>
        <div class="middle_first_right">
          <img width="100%" src="../assets/1.png" alt="">
        </div>
      </div>
      <div class="middle_second">
        <div class="middle_zs">前沿科研助手</div>
        <div class="middle_md">量子AI智能体基于新一代LLM和RAG技术，为量子科学研究提供精准、可靠的知识支持</div>
        <div class="middle_hx">
          <div class="middle_ys">

            <div class="hx">核心优势</div>
            <div class="sub"><span class="dian">.</span>DeepSeek定制化微调提升检索精度</div>
            <div class="sub"><span class="dian">.</span>50万+量子计算论文提供知识支持</div>
            <div class="sub"><span class="dian">.</span>私有化部署满足敏感领域需求</div>
          </div>
          <div class="middle_cx">
            <div>
              <div class="cx">创新特性</div>
              <div class="sub"><span class="dian">.</span>量子计算领域智能问答</div>
              <div class="sub"><span class="dian">.</span>多模态增强检索与解析</div>

            </div>
          </div>
        </div>
      </div>
      <Foot></Foot>
    </div>

    <div v-show="!isHome" class="about">
      <div class="about_title">关于我们</div>
      <div class="about_lz">
        量子AI智能体由先进编译实验室研发，基于新一代LLM和RAG技术，为量子科学研究提供精准、可靠的知识支持。
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.about {
  height: 100vh;
  background: linear-gradient(rgba(55, 96, 233, 0.1), rgb(245, 243, 244));
  box-sizing: border-box;
  padding-top: 60px;
  padding: 60px;

  .about_title {
    margin-top: 2rem;
    font-size: 2.2rem;
    font-weight: 700;
  }

  .about_lz {
    color: rgba(75 85 99);
    margin-top: 3rem;
    font-size: 1.2rem;
  }
}


.header_com {
  box-sizing: border-box;
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  position: fixed;
  top: 0px;
  display: flex;
  align-items: center;
  height: 60px;
  background-color: rgba(250, 250, 250, 0.5);
  width: 100%;

  .header_com_logo {
    font-size: 2rem;
    font-weight: 700;
    flex: 1;
  }

  .header_com_right {
    display: flex;
    align-items: center;

    div {
      margin-left: 10px;
      margin-right: 10px;
      cursor: pointer;
      color: #333333;
    }

    .header_com_lo_bu {
      // padding: 0.1rem;
      padding: 0.4rem 1.5rem;
      background-color: antiquewhite;
      color: rgb(241, 163, 60);
    }

  }
}

.middle_com {
  // text-align: center;
  // display: flex;
  height: 100%;
  // background-color: #000000;


  .middle_first {
    width: 100%;
    box-sizing: border-box;
    padding-left: 1.5rem;
    padding-right: 1.5rem;
    height: 100vh;
    // background-color: #000000;
    // background: linear-gradient(rgb(238, 33, 238, 0.1), rgb(245, 243, 244));
    background: (url(../assets/first_bg.png) no-repeat);
    display: flex;
    align-items: center;

    .middle_first_left {
      flex: 1;

      .title_parent {
        font-size: 3rem;
        font-weight: 700;
        margin-bottom: 0.6rem;
      }

      .title_sub {
        font-size: 1.4rem;
        color: rgb(75, 85, 99, 1);
        font-weight: 500;
        margin-bottom: 2rem;
        margin-top: 1rem;
      }

      .ts_parent {
        font-size: 1rem;
        color: rgb(75, 85, 99, 0.5);
        margin-top: 2rem;

      }

      .question {
        margin-top: 2rem;
        width: 100%;
        display: flex;
        flex-wrap: wrap;

        .question_sub {
          cursor: pointer;
          // font-size: ;
          color: #9f9f9f;
          opacity: 1;
          background-color: #f3f3f3;
          padding: 0.4rem 1rem;
          margin-left: 1rem;
          margin-bottom: 1rem;
          border-radius: 0.4rem;
        }
      }

      .com_in {
        display: flex;
        width: 80%;

        .start_bu {
          cursor: pointer;
          width: 10rem;
          padding: 1rem 1rem 1rem 1rem;
          background-color: #000000;
          color: #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 0.4rem;

          font-size: 1.2rem;
          margin-left: 2rem;
        }

      }
    }

    .middle_first_right {
      flex: 1;
    }

  }

  .middle_second {
    box-sizing: border-box;
    padding-top: 20vh;
    text-align: center;
    height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    // background-color: aqua;
    background: linear-gradient(rgba(55, 96, 233, 0.1), rgb(245, 243, 244));

    .middle_zs {

      font-size: 2.2rem;
      font-weight: 700;
    }

    .middle_md {
      margin-top: 1rem;
      font-size: 1.2rem;
      color: #747474;
    }

    .middle_hx {
      width: 50%;
      display: flex;
      justify-content: center;
      margin-top: 2rem;

      .middle_ys {
        background: linear-gradient(#f7e0e0, #f1ebeb);
        box-sizing: border-box;
        border: 0 solid #e5e7eb;
        border-color: rgba(255 237 213);
        border-radius: 10px;
        padding: 20px;
        margin: 20px;
        width: 45%;

        .hx {
          width: 100%;
          line-height: 3rem;
          color: rgba(234 88 12);
          font-weight: bold;
          font-size: 1.2rem;
          text-align: left;
        }

        .sub {
          font-size: 1rem;
          width: 100%;
          text-align: left;
          line-height: 2.5rem;
          color: rgba(31 41 55);

          .dian {
            box-sizing: border-box;
            font-size: 3rem;
            color: rgba(234 88 12);
            padding-right: 0.4rem;
          }
        }

        &:hover {
          box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
        }
      }

      .middle_cx {
        border-radius: 10px;
        box-sizing: border-box;

        border: 0 solid #e5e7eb;
        border-color: rgba(255 237 213);
        background: linear-gradient(#f7e0e0, #f1ebeb);
        padding: 20px;
        margin: 20px;
        width: 45%;

        .cx {
          width: 100%;
          font-size: 1.2rem;
          line-height: 3rem;
          color: rgba(234 88 12);
          font-weight: bold;
          text-align: left;
        }

        .sub {
          font-size: 1rem;
          width: 100%;
          text-align: left;
          line-height: 2.5rem;
          color: rgba(31 41 55);

          .dian {
            box-sizing: border-box;
            font-size: 3rem;
            color: rgba(234 88 12);
            padding-right: 0.4rem;
          }
        }

        &:hover {
          box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.17);
        }
      }
    }
  }
}
</style>
