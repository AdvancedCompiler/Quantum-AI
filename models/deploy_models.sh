#!/bin/bash

# 启动 DeepSeek 模型在显卡 0 上
export VLLM_IMAGE_FETCH_TIMEOUT=30
CUDA_VISIBLE_DEVICES=0 vllm serve /root/autodl-tmp/markitdown/QuantumAI  --trust-remote-code --served-model-name deepseek --port 8000  &


# 启动 Qwen 模型在显卡 1 上
CUDA_VISIBLE_DEVICES=1 vllm serve  /root/autodl-tmp/models/Qwen/Qwen2.5-VL-7B-Instruct --trust-remote-code --served-model-name qwen_vl --limit-mm-per-prompt image=2 --port 8001 &

# 等待所有后台进程完成
wait 