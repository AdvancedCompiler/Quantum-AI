import "./assets/main.css";

import { createApp } from "vue";
import pinia from "@/stores";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);
// 注册指令
app.directive("click-outside", {
  mounted(el, binding) {
    el.clickOutsideEvent = function (event:any) {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event);
      }
    };
    document.addEventListener("click", el.clickOutsideEvent);
  },
  unmounted(el) {
    document.removeEventListener("click", el.clickOutsideEvent);
  },
});

app.use(pinia);
app.use(router);
app.use(ElementPlus);
app.mount("#app");
