# 训练数据集说明

训练数据集分为微调、强化学习两个部分数据集

本项目先基于**DeepSeek-R1-Distill-Qwen-32B 后迭代优化升级为QWQ-32B** 模型，两个模型都带有thinking深度思考模式

数据集一共分为三部分：

（1）基于**EasyData**工作流构建的量子领域带有思考链的数据集   

​        **sft_easydata_01.json  、sft_easydata_02.json**

（2）基于**KiMi** 强大的学术科研能力，调用api构造的量子领域知识问答对

​       **sft_base.json**、**sft_quantum.json**

（3）用于调整输出内容排版、强化知识库检索输出的强化学习数据集

​       **grpo_dataset.json、grpo_dataset2.json**

## 📌 EasyData工作流描述

本工作流基于开源项目 EasyData，用于自动化生成适用于模型微调的高质量训练数据集。通过该流程可实现从原始语料或对话记录到结构化指令微调数据集（Instruction Tuning Dataset）的全流程构建。

- **项目地址**：https://github.com/ConardLi/easy-dataset
- **语言支持**：中文、英文等多语言

### 另外提供数据集处理代码和过程文件

**data_process.py**

​     arxiv.csv

​     questions_with_answers.json

​    dataset.jsonl

**grpo_dataset_maker.py**

​     questions.json

## 免责声明

请各位严格遵守如下约定：

   1.本项目任何资源**可供学术研究使用，若有商业用途需求请联系我们 canaanleen@163.com**。

   2.模型输出受多种不确定性因素影响，本项目当前无法保证其准确性，**严禁用于真实场景**。

   3.本项目不承担任何法律责任，亦不对因使用相关资源和输出结果而可能产生的任何损失承担责任。

## 致谢

本项目参考了以下开源项目，在此对相关项目和研究开发人员表示感谢。

Easy Dataset ：https://github.com/ConardLi/easy-dataset

LangChain：https://github.com/langchain-ai/langchain
