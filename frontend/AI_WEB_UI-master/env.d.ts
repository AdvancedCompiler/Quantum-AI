/// <reference types="vite/client" />
declare module '*.vue' {
    import { DefineComponent } from 'vue'
    // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
    const component: DefineComponent<{}, {}, any>
    export default component
  }

  declare module 'markdown-it' {
    import MarkdownIt from 'markdown-it';
    export default MarkdownIt;
  }
  declare module 'crypto-js' {
    import cryptoJs from 'crypto-js';
    export default cryptoJs;
  }
  declare module 'markdown-it-katex-gpt' {
    import markdownItKatexGpt from 'markdown-it-katex-gpt';
    export default markdownItKatexGpt;
  }
  // declare var EventSourcePolyfill: any;
  
  // declare module 'event-source-polyfill' {
  //   import  {EventSourcePolyfill} from ' event-source-polyfill';
  //   export default {EventSourcePolyfill};
  // }