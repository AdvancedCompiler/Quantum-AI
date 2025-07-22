<script lang='ts' setup>
import { ref } from 'vue'
const isOpen = ref<boolean>(false)
const url = ref<string>('')
//调用开关的模态框
const open = (path: string) => {
  isOpen.value = true;
  url.value = path
}
const close = () => {
  isOpen.value = false;
}

defineExpose({ open, close })
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click="close">
    <!-- 模态框内容@click.stop -->
    <div class="modal-content" >
      <!-- 关闭按钮 -->
      <span class="close-btn" @click="close">&times;</span>
      <!-- 插槽：用于自定义内容 -->
      <slot>
        <img style='width:500px;border-radius: 20px;' :src='url'  />
      </slot>
    </div>
  </div>
</template>
<style scoped lang="scss">
.modal-overlay {
  background-color: rgba(0, 0, 0, 0.2);
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  // overflow: hidden;
}

/* 模态框内容 */
.modal-content {
  // overflow: hidden;
  background-color: rgba(0, 0, 0, 0.2);
  // padding: 20px;
  // border-radius: 8px;
  // max-width: 500px;
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 40px;
  right: 40px;
  font-size: 24px;
  cursor: pointer;
  color: #888;
  background-color: #ffffff;
  padding:0px 10px;
  line-height: 30px;
  border-radius: 4px;
}

.close-btn:hover {
  color: #333;
}
</style>
