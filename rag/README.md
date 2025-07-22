# 📄 PDF文献转换为RAG的Chunk工作流

## 📌 工作流描述

本项目旨在实现从PDF论文到Markdown文档的自动解析与转换，并通过 `langchain` 构建 RAG（Retrieval-Augmented Generation）服务。在 RAG 初始化时，系统会自动扫描转化后的 Markdown 文件路径。若检测到文件变更，将自动重新创建索引并分割 Chunk。

## 🧾 主要文件说明

- **`pdf_convertor_batch.py`**
   功能：批量解析 PDF 文件，将其转换为 Markdown 格式，并保留图片引用。
- **`annotation_json_maker.py`**
   功能：提取 Markdown 文件中的图片引用，并调用大模型生成图片描述信息，便于后续插入 RAG 检索结果中。

## ⚙️ 运行方式

1. 将待处理的 PDF 文献统一使用 `pdf_convertor_batch.py` 转换为 Markdown 文件，并生成对应的图片目录。
2. 每次处理的 PDF 按“批次”管理：
   - 每个批次的文件置于**同一文件夹**中。
   - 所有批次统一存放于一个**总目录**下，形成一个完整的知识库。

> 📁 知识库结构举例：
>
> - `all_instruct_scratch_without_annotation/` （用于存储基础指令知识）
> - `all_scratch_without_annotation/` （用于存储文献类知识）

1. 在运行 RAG 服务并初始化 `retriever` 实例时，系统会对知识库进行扫描、切分 Chunk 并持久化存储。相关输出文件包括：
   - `faiss_index_kb1`：FAISS 向量索引文件
   - `paper_file_metadata_without_annotation.json`：文件元数据
   - `paper_chunks_without_annotation.pkl`：切分后的 Chunk 数据
2. 若知识库内容未发生变化，则直接加载上次的切分结果；若发生变更，将重新或增量地进行切分与保存。

## ⚠️ 注意事项

- **PDF 转 Markdown 与 Chunk 切分**均依赖 HuggingFace 服务调用。

- 脚本中已配置镜像地址以加速访问。

  
