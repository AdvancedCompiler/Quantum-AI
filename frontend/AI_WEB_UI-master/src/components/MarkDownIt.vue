<template>
  <div class="mark_down" v-html="renderedHtml" :html="true"></div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import MarkdownIt from 'markdown-it';
//----markdown-it-latex  开始
// import mk from 'markdown-it-latex';
// import 'markdown-it-latex/dist/index.css'
//----markdown-it-latex  结束

//----markdown-it-latex  开始
// import mk from 'markdown-it-katex';
// import 'katex/dist/katex.min.css'
//----markdown-it-latex  结束

//----markdown-it-latex  开始
import mk from 'markdown-it-katex-gpt';

// import mk from 'markdown-it-mathjax';
// import mk from 'markdown-it-texmath';
// import katex from 'katex';
// import 'katex/dist/katex.min.css';
//----markdown-it-latex  结束
//   import markdownItLatex  from 'markdown-it-latex';
//   import 'katex/dist/katex.min.css'

// import markdownItMathjax from 'markdown-it-mathjax';

// Props 定义
interface Props {
  markdown: any;
}

const props = defineProps<Props>();

// 创建一个 MarkdownIt 实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,

});
// md.use(mk);

md.use(mk, {
  delimiters: [
    { left: '\\[', right: '\\]', display: false },
    { left: '\\(', right: '\\)', display: false },
    { left: '$$', right: '$$', display: false },
    { left: '$', right: '$', display: false },
    // { left: '\[', right: '\]', display: false },
    // { left: '\[', right: '\]', display: false },

  ]
})
// md.use(mk)
// md.use(mk, {
//       engine: katex,
//       delimiters: 'dollars',
//       katexOptions: { macros: { "\\RR": "\\mathbb{R}" } }
//     });
// 定义一个响应式变量来存储渲染后的 HTML
const renderedHtml = ref('');

// 监听 markdown prop 的变化并重新渲染
watch(() => props.markdown, (newData) => {
  // md.use(mk)
//   md.renderer.rules.link_open = () => {
//   return '<a style="display: block; margin-bottom: 8px;">'; // 添加块级样式
// };
//   md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
//   return '<a style="display: block; margin-bottom: 8px;">'; // 添加块级样式
// };
  // md.renderer.rules.link_close = () => {
  //   return '</a><br>';
  // };
  

  renderedHtml.value = md.render(newData);

}, { immediate: true });
</script>

<style lang="scss">
// .reference-links a {
//   display: block; /* 或 inline-block */
//   margin-bottom: 8px; /* 可选间距 */
// }

// /* 或确保容器允许换行 */
// .reference-links {
//   white-space: normal;
// }
.mark_down {
  // word-wrap: break-word;
  //  white-space: pre-wrap;

  img {
    max-width: 100% !important;
    // height: 20px !important;
  }

  // p{
  //   margin: 0px !important;
  //   white-space: pre-wrap;
  // }
  // ol{
  //   margin: 0px !important;
  // }
  // h3{
  //   margin: 0px !important;
  // }
  // pre{

  //   margin: 0px !important;
  // }
  // *{
  //   margin: 0px !important;

  // }


}

/* 你可以在这里添加一些样式 */
</style>