<template>
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', { 'user-message': message.isUser }]"
        >
          <div class="avatar">
            <img 
              v-if="!message.isUser"
              src="../assets/ai-avatar.png" 
              alt="AI Avatar"
            >
            <span v-else>👤</span>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-indicator">
          <div class="dot-flashing"></div>
        </div>
      </div>
  
      <!-- 输入区域 -->
      <div class="input-container">
        <textarea
          v-model="inputMessage"
          @keydown.enter.exact.prevent="sendMessage"
          placeholder="输入你的问题..."
          rows="1"
          ref="textarea"
        ></textarea>
        <button @click="sendMessage">
          <svg-icon icon-class="send" />
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted, nextTick } from 'vue'
  import { format } from 'date-fns'
  
  // 响应式数据
  const messages = reactive([])
  const inputMessage = ref('')
  const isLoading = ref(false)
  const messagesContainer = ref(null)
  
  // 自动调整输入框高度
  const textarea = ref(null)
  const adjustHeight = () => {
    textarea.value.style.height = 'auto'
    textarea.value.style.height = textarea.value.scrollHeight + 'px'
  }
  
  // 发送消息
  const sendMessage = async () => {
    if (!inputMessage.value.trim()) return
  
    const userMessage = {
      content: inputMessage.value.trim(),
      isUser: true,
      timestamp: new Date()
    }
    messages.push(userMessage)
    
    // 模拟AI回复
    isLoading.value = true
    inputMessage.value = ''
    
    await nextTick()
    scrollToBottom()
    
    setTimeout(async () => {
      const aiMessage = {
        content: generateResponse(userMessage.content),
        isUser: false,
        timestamp: new Date()
      }
      messages.push(aiMessage)
      isLoading.value = false
      
      await nextTick()
      scrollToBottom()
    }, 1500)
  }
  
  // 工具函数
  const formatTime = (date) => format(date, 'HH:mm')
  const scrollToBottom = () => {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
  
  // 模拟AI生成回复
  const generateResponse = (input) => {
    // 这里可以替换为实际的API调用
    const responses = {
      hello: "你好！我是AI助手，有什么可以帮您？",
      help: "我可以帮助您解答各种问题，请随时提问。",
      default: "已收到您的提问，正在思考最佳回复..."
    }
    return responses[input.toLowerCase()] || responses.default
  }
  
  // 自动调整输入框高度
  onMounted(() => {
    textarea.value.addEventListener('input', adjustHeight)
  })
  </script>
  
  <style scoped>
  .chat-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }
  
  .messages {
    flex: 1;
    overflow-y: auto;
    padding: 1rem;
    background: #f5f5f5;
  }
  
  .message {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }
  
  .user-message {
    flex-direction: row-reverse;
  }
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .message-content {
    max-width: 70%;
    padding: 1rem;
    border-radius: 1rem;
    background: white;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  
  .user-message .message-content {
    background: #007bff;
    color: white;
  }
  
  .message-time {
    font-size: 0.8rem;
    color: #666;
    margin-top: 0.5rem;
  }
  
  .input-container {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background: white;
    box-shadow: 0 -2px 8px rgba(0,0,0,0.05);
  }
  
  textarea {
    flex: 1;
    padding: 1rem;
    border: 1px solid #ddd;
    border-radius: 0.5rem;
    resize: none;
    max-height: 150px;
  }
  
  button {
    padding: 0.5rem 1.5rem;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 0.5rem;
    cursor: pointer;
  }
  
  /* 加载动画 */
  .dot-flashing {
    position: relative;
    width: 10px;
    height: 10px;
    border-radius: 5px;
    background-color: #999;
    animation: dotFlashing 1s infinite linear alternate;
  }
  
  @keyframes dotFlashing {
    0% { opacity: 0.2; transform: translateY(0); }
    50% { opacity: 1; transform: translateY(-5px); }
    100% { opacity: 0.2; transform: translateY(0); }
  }
  </style>