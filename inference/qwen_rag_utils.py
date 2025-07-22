import os
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain.schema import Document
from langchain_core.retrievers import BaseRetriever
import pickle
import json
from glob import glob
import re
from openai import OpenAI
import time
import faiss
from langchain_experimental.text_splitter import SemanticChunker
from langchain_community.docstore.in_memory import InMemoryDocstore
import numpy as np
import re
from urllib.parse import urlparse
import csv
from typing import Dict
import logging
from transformers import AutoTokenizer

import base64
from flask import Flask, request, jsonify, Response
from orb_search import title_path
# from test_picture_v1 import title_path


url_prefix = "http://129.211.221.44:8062/user/show?filePath=./image/"

qwen_client = OpenAI(
    base_url="http://localhost:8001/v1",
    api_key="EMPTY",
)

deepseek_client = OpenAI(
    base_url="http://localhost:8000/v1",
    api_key="EMPTY",
)


# 加载语料
def load_or_update_chunks(base_dir="all_scratch", chunks_file="chunks.pkl", metadata_file="file_metadata.json"):
    all_chunks = []
    file_metadata = {}

    if os.path.exists(chunks_file) and os.path.exists(metadata_file):
        with open(chunks_file, "rb") as f:
            all_chunks = pickle.load(f)
        with open(metadata_file, "r") as f:
            file_metadata = json.load(f)
        print(f"已加载 {len(all_chunks)} 个现有分割块")

    current_files = {}
    for subdir1 in os.listdir(base_dir):
        subdir1_path = os.path.join(base_dir, subdir1)
        
        # 确保是一级子目录
        if os.path.isdir(subdir1_path):
            # 遍历一级子目录下的二级子目录
            for subdir2 in os.listdir(subdir1_path):
                subdir2_path = os.path.join(subdir1_path, subdir2)
                
                # 确保是二级子目录
                if os.path.isdir(subdir2_path):
                    # 查找二级子目录中的所有 .md 文件
                    md_files = glob(os.path.join(subdir2_path, "*.md"))
                    
                    # 遍历 .md 文件并记录修改时间
                    for file_path in md_files:
                        current_files[file_path] = os.path.getmtime(file_path)

    new_or_updated_files = {
        file_path: mtime for file_path, mtime in current_files.items()
        if file_path not in file_metadata or mtime > file_metadata[file_path]
    }

    if new_or_updated_files:
        print(f"检测到 {len(new_or_updated_files)} 个新增或修改的文件，正在处理...")
        global_index = len(all_chunks)
        embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
        splitter = SemanticChunker(embeddings=embeddings, breakpoint_threshold_type="percentile")

        for file_path in new_or_updated_files:
            with open(file_path, "r", encoding="utf-8") as f:
                raw_content = f.read()
            document = Document(page_content=raw_content, metadata={"source": file_path})
            chunks = splitter.split_text(document.page_content)
            for chunk in chunks:
                all_chunks.append(Document(
                    page_content=chunk,
                    metadata={"index": global_index, "source": file_path}
                ))
                global_index += 1
            file_metadata[file_path] = current_files[file_path]

        with open(chunks_file, "wb") as f:
            pickle.dump(all_chunks, f)
        with open(metadata_file, "w") as f:
            json.dump(file_metadata, f)
        print(f"已更新分割结果，总共 {len(all_chunks)} 个块")
    else:
        print("没有检测到新增或修改的文件，使用现有分割结果")

    return all_chunks

# 从文本中提取图片url
def extract_url_path(url: str) -> str:
    """
    提取URL的路径部分，忽略协议、域名和 'images/' 前缀
    例如：https://serverip/images/A_gentle_introduction_to_Quantum_Natural_Language_Processing-with-image-refs_artifacts/image_000001_50c68cbbdf5c458d895bd45562ced3d0c85d06f2dea891f1baf2f53f7459624a.png
    -> A_gentle_introduction_to_Quantum_Natural_Language_Processing-with-image-refs_artifacts/image_000001_50c68cbbdf5c458d895bd45562ced3d0c85d06f2dea891f1baf2f53f7459624a.png
    """
    try:
        parsed = urlparse(url)
        # 移除开头的斜杠
        path = parsed.path.lstrip('/')
        if path.startswith('images/'):
            path = path[len('images/'):]
        return path
    except Exception as e:
        return url


# 替换图片url为对应的图片详解
def replace_images_with_paper_content(input_string: str, json_file: str) -> str:
    """
    将输入字符串中的图片链接替换为JSON中对应的格式化内容
    
    Args:
        input_string: 输入的字符串（可能包含Markdown图片链接）
        json_file: 包含图片URL和描述信息的JSON文件路径
        
    Returns:
        替换后的字符串
    """
    # 加载JSON映射
    url_to_content: Dict[str, dict] = {}
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
            for item in data:
                # 提取URL中的文件名部分作为key
                image_url = item['url'].strip()
                filename = image_url.split('/')[-1]  # 获取文件名
                url_to_content[filename] = {
                    'title': item['image_title'],
                    'description': item['description']
                }
    except Exception as e:
        logger.error("Error loading JSON file %s: %s", json_file, str(e))
        return input_string

    # 图片链接的正则表达式
    image_pattern = r'!\[.*?\]\(([^)]+)\)'
    logger.debug("Using image pattern: %s", image_pattern)

    def replace_match(match):
        """替换匹配到的图片链接"""
        full_image_url = match.group(1)
        filename = full_image_url.split('/')[-1]  # 获取文件名部分
        
        logger.debug("Found image URL in string: full=%s, filename=%s", 
                    full_image_url, filename)
        
        if filename in url_to_content:
            content = url_to_content[filename]
            # 构造替换格式
            replacement = (
                '<paper-image>\n'
                f'![Image]({url_prefix}{filename})\n'
                f'图片名称：{content["title"]}\n'
                f'图片内容：{content["description"]}\n'
                '</paper-image>'
            )
            logger.info("Replacing URL %s with formatted content", full_image_url)
            return replacement
        else:
            logger.warning("No mapping found for image URL: %s", full_image_url)
            return match.group(0)

    # 查找并替换所有匹配的图片链接
    matches = list(re.finditer(image_pattern, input_string))
    if matches:
        logger.info("Found %d image links in input string", len(matches))
        for match in matches:
            logger.debug("Match found: %s", match.group(0))
    else:
        logger.warning("No image links found in input string")

    # 执行替换
    result = re.sub(image_pattern, replace_match, input_string)
    
    return result


# 自定义rag检索器
class ContextualRetriever(BaseRetriever):
    def __init__(self, vector_stores, k=1, context_window=0):
        """
        初始化检索器
        Args:
            vector_stores: 向量库列表 [vector_store1, vector_store2, ...]
            k: 每个向量库返回的文档数量
            context_window: 上下文窗口大小
        """
        super().__init__()
        self._vector_stores = vector_stores if isinstance(vector_stores, list) else [vector_stores]
        self._k = k
        self._context_window = context_window
        print(f"初始化检索器，加载了 {len(self._vector_stores)} 个向量库")
    
    def _get_relevant_documents(self, query: str) -> list[Document]:
        all_docs = []
        # 从每个向量库中获取文档
        for i, vector_store in enumerate(self._vector_stores):
            print(f"\n正在从向量库 {i+1} 中检索...")
            initial_docs = vector_store.similarity_search(query, k=self._k)
            print(f"向量库 {i+1} 检索到 {len(initial_docs)} 个初始文档")
            
            store_docs = list(vector_store.docstore._dict.values())
            store_docs = sorted(store_docs, key=lambda x: x.metadata["index"])
            
            # 为每个文档添加上下文
            docs_with_context = []
            for doc in initial_docs:
                idx = doc.metadata["index"]
                start_idx = max(0, idx - self._context_window)
                end_idx = min(len(store_docs), idx + self._context_window + 1)
                for i in range(start_idx, end_idx):
                    docs_with_context.append(store_docs[i])
            
            print(f"向量库 {i+1} 添加上下文后共有 {len(docs_with_context)} 个文档")
            all_docs.extend(docs_with_context)
        
        print(f"\n合并前总文档数: {len(all_docs)}")
        
        # 去重
        seen = set()
        final_docs = [doc for doc in all_docs if not (doc.page_content in seen or seen.add(doc.page_content))]
        print(f"去重后文档数: {len(final_docs)}")
        
        # 收集所有来源
        sources = set()
        for doc in final_docs:
            sources.add(doc.metadata['source'])
        
        print(f"文档来源数量: {len(sources)}")
        print("文档来源:", sources)
            
        sources_doc = Document(
            page_content="\n\n来源:\n" + "\n".join(sorted(sources)),
            metadata={"index": -1, "source": "sources"}
        )
        final_docs.append(sources_doc)

        # 处理图片替换
        for doc in final_docs:
            doc.page_content = replace_images_with_paper_content(doc.page_content,'/root/autodl-tmp/markitdown/batch_annotation.json')

        # 处理图片URL
        for doc in final_docs:
            content = doc.page_content
            img_pattern = r'!\[.*?\]\((.*?)\)'
            matches = re.findall(img_pattern, content)
            for match in matches:
                if not match.startswith('http'):
                    cut_match = match.split("/")[1]
                    new_path = f'{url_prefix}{cut_match}'
                    content = content.replace(match, new_path)
            doc.page_content = content

        return final_docs

#初始化rag
def setup_rag_system(documents_list, faiss_index_paths=None):
    """
    设置RAG系统，包括嵌入、向量存储和检索器
    
    Args:
        documents_list: 文档列表的列表 [[documents1], [documents2], ...]
        faiss_index_paths: FAISS索引保存路径列表 ["path1", "path2", ...]
    
    Returns:
        ContextualRetriever: 配置好的检索器
    """
    print("\n=== 开始设置RAG系统 ===")
    
    if not isinstance(documents_list, list):
        documents_list = [documents_list]
        print("将单个文档列表转换为列表的列表")
    
    print(f"文档列表数量: {len(documents_list)}")
    for i, docs in enumerate(documents_list):
        print(f"文档集 {i+1} 包含 {len(docs)} 个文档")
    
    if faiss_index_paths is None:
        faiss_index_paths = ["faiss_index_" + str(i) for i in range(len(documents_list))]
        print("使用默认的向量库路径:", faiss_index_paths)
    elif not isinstance(faiss_index_paths, list):
        faiss_index_paths = [faiss_index_paths]
        print("将单个向量库路径转换为列表")
    
    print(f"向量库路径数量: {len(faiss_index_paths)}")
    print("向量库路径:", faiss_index_paths)
        
    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
    print("\n使用模型:", embeddings.model_name)
    
    vector_stores = []
    
    for i, (documents, faiss_index_path) in enumerate(zip(documents_list, faiss_index_paths)):
        print(f"\n处理向量库 {i+1}: {faiss_index_path}")
        if os.path.exists(faiss_index_path):
            print(f"加载现有的向量存储...")
            vector_store = FAISS.load_local(faiss_index_path, embeddings, allow_dangerous_deserialization=True)
            print(f"向量库 {i+1} 包含 {len(vector_store.docstore._dict)} 个文档")
            
            if len(vector_store.docstore._dict) != len(documents):
                print(f"文档数量不匹配（向量库: {len(vector_store.docstore._dict)}, 输入: {len(documents)}），重新生成向量存储...")
                # 创建FAISS索引
                dimension = 384  # all-MiniLM-L6-v2的输出维度是384
                index = faiss.IndexFlatL2(dimension)
                # 生成嵌入向量
                texts = [doc.page_content for doc in documents]
                vectors = embeddings.embed_documents(texts)
                index.add(np.array(vectors, dtype=np.float32))
                # 创建FAISS实例
                docstore = InMemoryDocstore({i: doc for i, doc in enumerate(documents)})
                index_to_docstore_id = {i: i for i in range(len(documents))}
                vector_store = FAISS(
                    embedding_function=embeddings,
                    index=index,
                    docstore=docstore,
                    index_to_docstore_id=index_to_docstore_id
                )
                vector_store.save_local(faiss_index_path)
                print(f"已保存新的向量库到 {faiss_index_path}")
        else:
            print(f"创建新的向量存储...")
            # 创建FAISS索引
            dimension = 384  # all-MiniLM-L6-v2的输出维度是384
            index = faiss.IndexFlatL2(dimension)
            # 生成嵌入向量
            texts = [doc.page_content for doc in documents]
            vectors = embeddings.embed_documents(texts)
            index.add(np.array(vectors, dtype=np.float32))
            # 创建FAISS实例
            docstore = InMemoryDocstore({i: doc for i, doc in enumerate(documents)})
            index_to_docstore_id = {i: i for i in range(len(documents))}
            vector_store = FAISS(
                embedding_function=embeddings,
                index=index,
                docstore=docstore,
                index_to_docstore_id=index_to_docstore_id
            )
            vector_store.save_local(faiss_index_path)
            print(f"已保存向量库到 {faiss_index_path}")
        vector_stores.append(vector_store)
    
    print(f"\n成功加载 {len(vector_stores)} 个向量库")
    retriever = ContextualRetriever(vector_stores, k=2, context_window=1)
    print("=== RAG系统设置完成 ===\n")
    return retriever


#算法关键词检测
def build_algorithm_references(json_file_path, query):
    """
    从 JSON 文件中读取数据，检查"算法名称"中是否包含 query 中的英文关键词。
    对于中文算法名称直接判断是否包含在query中，对于英文算法名称判断是否为独立单词。
    """
    algorithm_references = ""  # 初始化结果字符串
    references = ""
    
    # 打开 JSON 文件并加载数据
    with open(json_file_path, mode='r', encoding='utf-8') as file:
        data = json.load(file)
    
    # 从 query 中提取所有英文关键词（连续的字母序列）
    query_words = re.findall(r"[a-zA-Z]+", query)
    # if not query_words:
    #     return "", ""  # 如果没有英文关键词，返回两个空字符串
    
    # 为每个关键词构建正则表达式（不直接加 \b，稍后手动检查边界）
    patterns = [re.compile(re.escape(word), re.IGNORECASE) for word in query_words]
    
    # 遍历 JSON 数据中的每个对象
    for item in data:
        algorithm_name = item.get("算法名称", "").strip()
        if not algorithm_name:
            continue
            
        # 检查算法名称是否包含中文
        has_chinese = bool(re.search('[\u4e00-\u9fff]', algorithm_name))
        
        if has_chinese:
            # 如果包含中文，直接检查query是否包含在算法名称中
            if  algorithm_name.lower() in query.lower():
                for key, value in item.items():
                    if not value or value.strip() == "":
                        continue
                    if key == "图片名":
                        algorithm_references += f"引用图片：![Image]({url_prefix}{value.strip()})\n"
                    elif key == "来源论文":
                        references += f"**来源论文:**[1] {value.strip()}\n"
                    else:
                        algorithm_references += f"{key}:\n{value.strip()}\n"
                algorithm_references += "-" * 40 + "\n"
        # 检查每个关键词是否出现在算法名称中
        for pattern in patterns:
            matches = pattern.finditer(algorithm_name)
            for match in matches:
                matched_word = match.group()
                start_pos, end_pos = match.span()
                
                # 检查匹配词是否为独立单词
                is_independent = True
                
                # 检查前一个字符（如果存在）
                if start_pos > 0:
                    prev_char = algorithm_name[start_pos - 1]
                    if prev_char.isalnum():  # 如果前一个字符是字母或数字，则不是独立单词
                        is_independent = False
                
                # 检查后一个字符（如果存在）
                if end_pos < len(algorithm_name):
                    next_char = algorithm_name[end_pos]
                    if next_char.isalnum():  # 如果后一个字符是字母或数字，则不是独立单词
                        is_independent = False
                
                # 如果是独立单词，记录该条目内容
                if is_independent:
                    for key, value in item.items():
                        if not value or value.strip() == "":
                            continue
                        if key == "图片名":
                            algorithm_references += f"引用图片：![Image]({url_prefix}{value.strip()})\n"
                        elif key == "来源论文":
                            references += f"**来源论文:**[1] {value.strip()}\n"
                        else:
                            algorithm_references += f"{key}:\n{value.strip()}\n"
                    algorithm_references += "-" * 40 + "\n"
                    break  # 找到匹配后跳出内层循环，避免重复记录
    
    return algorithm_references, references  # 始终返回两个值

# rag查询
def query_rag(retriever, query):
    """
    执行RAG查询并格式化输出结果，包含参考文献
    
    Args:
        retriever: 配置好的检索器
        query: 查询字符串
    
    Returns:
        str: 包含查询结果和参考文献的格式化字符串
    """
    # 获取相关文档
    # source_documents = retriever.get_relevant_documents(query)
    source_documents = retriever.invoke(query)
    
    # 合并所有文档内容作为结果
    result_content = []
    for doc in source_documents:
        content = doc.page_content.strip()
        if content and doc.metadata.get('source') != 'sources':  # 排除来源汇总文档
            result_content.append(content)
    answer = "\n\n".join(result_content)
    # 提取唯一来源
    unique_sources = set()
    for doc in source_documents:
        source = doc.metadata.get('source', '')
        if source and source != 'sources':
            unique_sources.add(source)
    
    # 格式化参考文献
    references = "\n\n### 参考文献\n"
    for i, source in enumerate(sorted(unique_sources), 1):
        references += f"[{i}] {source}\n\n"
    
    return answer,replace_paths_with_urls(references,'/root/autodl-tmp/markitdown/arxiv.csv')


def replace_paths_with_urls(reference_list, csv_file):
    """
    将参考文献中的 Markdown 文件路径替换为 arXiv URL。
    
    参数:
        reference_list (str): 包含文件路径的参考文献字符串
        csv_file (str): arXiv CSV 文件的路径
    
    返回:
        str: 替换为 URL 后的参考文献字符串
    """
    # 读取 CSV 文件，创建题目到 URL 的映射
    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        title_to_url = {row['title']: row['url'] for row in reader}
    
    # 将参考文献按行分割
    lines = reference_list.split('\n')
    new_lines = []
    
    # 处理每一行
    for line in lines:
        # 使用正则表达式匹配 [数字] 路径 格式
        match = re.match(r"\[(\d+)\]\s+(.*)", line)
        if match:
            number = match.group(1)  # 提取编号，如 '1'
            path = match.group(2)    # 提取路径
            # 从路径中提取文件名并解析题目
            file_name = path.split('/')[-1]
            title = file_name.replace("-with-image-refs.md", "")
            # 查找对应的 URL
            if title in title_to_url:
                url = title_to_url[title]
                new_line = f"[{number}] {url}"
            else:
                new_line = line[:3] + '《' + path.split('/')[-1].split('-with-image-refs.md')[0] + '》'
                # new_line = line[:3] + '《' + line[4:] + '》'  # 如果未找到，保留原行
        else:
            new_line = line  # 非参考文献行保持不变
        new_lines.append(new_line)
    
    # 将所有行重新组合成字符串
    return '\n'.join(new_lines)

#编码
def encode_image_to_base64(image_path):
    """Convert a local image file to base64 string if it's a file:// path."""
    if image_path.startswith("/"):
        try:
            # file_path = image_path[7:]  # Remove 'file://' prefix
            file_path = image_path
            if not os.path.exists(file_path):
                return None, f"Image file not found: {file_path}"
            with open(file_path, "rb") as f:
                encoded = base64.b64encode(f.read()).decode("utf-8")
                return f"data:image/jpeg;base64,{encoded}", None
        except Exception as e:
            return None, f"Error encoding image {image_path}: {str(e)}"
    elif image_path.startswith("http://") or image_path.startswith("https://") or image_path.startswith("data:image/jpeg;base64,") or image_path.startswith("data:image/png;base64,"):
        logging.info(f"收到图片: {image_path}")
        return image_path, None  # Pass through HTTP/HTTPS URLs or base64 strings directly
    else:
        return None, f"Invalid image path format: {image_path}. Expected 'file://', 'http://', 'https://', or base64 string."


def extract_and_summarize(input_string):
    """
    提取字符串中的图片引用和LaTeX公式，并将它们汇总插入到"**参考文献**"前面。
    
    参数:
        input_string (str): 输入的字符串，包含图片引用和LaTeX公式。
    
    返回:
        str: 处理后的字符串，包含图片引用和公式的汇总。
    """
    # 匹配图片引用的正则表达式
    image_pattern = r'!\[.*?\]\((.*?)\)'
    # 匹配LaTeX公式的正则表达式
    latex_pattern = r'\$\$.*?\$\$'
    
    # 提取所有图片引用
    images = re.findall(image_pattern, input_string, re.DOTALL)
    # 提取所有LaTeX公式
    latex_formulas = re.findall(latex_pattern, input_string, re.DOTALL)
    
    # 构造图片引用汇总
    image_summary = "\n\n**图片引用汇总:**\n"
    if images:
        image_summary += "\n".join([f"- ![{i+1}]({url})" for i, url in enumerate(images)])
    else:
        image_summary += "- 无图片引用"
    
    # 构造LaTeX公式汇总
    latex_summary = "\n\n**LaTeX公式汇总:**\n"
    if latex_formulas:
        latex_summary += "\n".join([f"- {formula}" for formula in latex_formulas])
    else:
        latex_summary += "- 无LaTeX公式"
    
    # 查找"**参考文献**"的位置
    reference_index = input_string.find("**参考文献**")
    
    # 如果找到"**参考文献**"，将汇总内容插入其前面；否则追加到字符串末尾
    if reference_index != -1:
        result = (
            input_string[:reference_index] 
            + image_summary 
            + latex_summary 
            + "\n\n" 
            + input_string[reference_index:]
        )
    else:
        result = input_string + image_summary + latex_summary
    
    return result,images

def describe_images(image_urls):
    """Use Qwen model to describe multiple images in a single API call."""
    if not image_urls:
        return {}, None  # No images to process

    # Process all URLs and collect them into a content array
    content = []
    processed_urls = {}
    if len(image_urls) > 1:
        # return None, "图片数量超过1张，请减少图片数量"
        logging.info(f"图片数量超过1张,只处理最后一张")
        image_urls = image_urls[-1:]
    
    url = image_urls[0]
    processed_url, error = encode_image_to_base64(url)
    if error:
        return None, error
    processed_urls[url] = processed_url  # Map original URL to processed URL
    content.append({"type": "image_url", "image_url": {"url": processed_url}})
    
    # pdf_url = title_path(url,'../all_data.json')

    # if pdf_url:
    #     logging.info(f"找到相关pdf: {pdf_url}")
    #     processed_pdf_url, error = encode_image_to_base64(pdf_url)
    #     content.append({"type": "image_url", "image_url": {"url": processed_pdf_url}})

    # Add the text prompt at the end of the content array
    #     content.append({"type": "text", "text": "现在请为LLM提供图片的解读，借助图片2中的上下文分析图片1的内容，务必详细"})
    # else:
    #     logging.info(f"未匹配到相关pdf")
    content.append({"type": "text", "text": "现在请为LLM提供图片的解读，请结合你的专业知识，描述图片内容，务必详细"})


    try:
        # Single API call with all images in the content array
        response = qwen_client.chat.completions.create(
            model="qwen_vl",  # Adjusted to match your setup
            messages=[
                {"role": "system", "content": "你是文本大模型的图片分析专家，请根据图片描述图片内容,注意给出公式时要使用$$包裹eg:$C(z) = \sum_{\alpha=1}^{m} C_\alpha(z)$"},
                {"role": "user", "content": content}
            ]
        )
        # Assume the response describes all images in order or as a single block
        response_text = response.choices[0].message.content

        logging.info(f"Qwen模型描述结果: {response_text}")
        return {image_urls[0]: response_text}, None

    except Exception as e:
        logging.error(f"Qwen模型错误: {str(e)}")
        return None, f"Error calling Qwen model: {str(e)}"
    

## 按段落截断超长result
def split_retriever_result(retriever_result, max_tokens, tokenizer):
    paragraphs = retriever_result.split("\n")
    selected = ""
    for para in paragraphs:
        if len(tokenizer.encode(selected + para)) < max_tokens :
            selected += para + "\n\n"
        else:
            break
    return selected


######################
def process_messages(messages):
    """Process messages, describe images if present, and prepare for DeepSeek."""
    if not messages or not isinstance(messages, list):
        return None, "Invalid input: 'messages' must be a non-empty list.",None,None

    has_images = False
    image_urls = []
    new_messages = []


    # Step 1: Check for images and collect URLs
    for msg in messages:
        if not isinstance(msg, dict) or "role" not in msg or "content" not in msg:
            return None, "Invalid message format: Each message must have 'role' and 'content'.",None,None
        content = msg.get("content", [])
        if not isinstance(content, list):
            return None, "Invalid content format: 'content' must be a list.",None,None

        for item in content:
            if item.get("type") == "image_url" and "image_url" in item and "url" in item["image_url"]:
                has_images = True
                image_url = item["image_url"]["url"]
                if image_url:
                    image_urls.append(image_url)
            elif item.get("type") == "image" and "image" in item:  # Backward compatibility
                has_images = True
                image_url = item["image"]
                if image_url:
                    image_urls.append(image_url)

    # Step 2: Describe images if present
    if has_images:
        descriptions, error = describe_images(image_urls)
        if error:
            return None, error,None,None
        logging.info(f"图片描述: {descriptions}")

        # Step 3: Replace image items with descriptions
        for msg in messages:
            new_content = []
            for item in msg["content"]:
                if item.get("type") == "text":
                    # new_content.append({"type": "text", "text": item["text"]})
                    pass
                elif item.get("type") == "image_url" and "image_url" in item and "url" in item["image_url"]:
                    image_url = item["image_url"]["url"]
                    if image_url in descriptions:
                        new_content.append({"type": "text", "text": f"我将向你描述一张图片，请主要根据我的描述和问题再结合检索信息进行回答\n图片信息: {descriptions[image_url]},问题: {messages[-1]['content'][-1]['text']}"})
                    else:
                        new_content.append({"type": "text", "text": f"[Image processing failed: {image_url}]"})          
                elif item.get("type") == "image" and "image" in item:  # Backward compatibility
                    image_url = item["image"]
                    if image_url in descriptions:
                        new_content.append({"type": "text", "text": f"我将向你描述一张图片，请主要根据我的描述和问题再结合检索信息进行回答\n图片信息: {descriptions[image_url]},问题: {messages[-1]['content'][-1]['text']}"})
                    else:
                        new_content.append({"type": "text", "text": f"[Image processing failed: {image_url}]"})
            new_messages.append({"role": msg["role"], "content": new_content})
    else:
        new_messages = messages  # No images, use original messages



    
    
    if new_messages[-1].get("role") == "user":
        query = new_messages[-1]["content"][0]["text"]
        print('='*50,'\n')
        print(query)
        print('='*50,'\n')

    ## 先检索关键词
        references = ""
        algorithm_references,references = build_algorithm_references('/root/autodl-tmp/markitdown/quantum_algorithm.json', query)
        print(algorithm_references)
        ###输出到文件中
        with open('algorithm_references.txt', 'w', encoding='utf-8') as file:
            file.write(algorithm_references)
        print('888888888888888888888888','\n\n')

        if len(algorithm_references) <=0:
        # 使用RAG系统回答问题
            retriever_result,references = query_rag(retriever, query)

            retriever_result,image_urls = extract_and_summarize(retriever_result)

            max_retriever_tokens = 15000  # 留出空间给提示和问题


            retriever_result = split_retriever_result(retriever_result, max_retriever_tokens, tokenizer)
            
        else:
            retriever_result = ""
            ###合并关键字检索和向量库检索结果
        retriever_result = algorithm_references + retriever_result
        

        ## 将retriever_result的内容输出到文件
        with open('last_retriever_result.txt', 'w', encoding='utf-8') as file:
            file.write(retriever_result+references)


### prompt模板
        rag_template = f"""
您是一个量子领域的专家，您的任务是基于您的专业知识和以下的检索信息回答用户的问题：

检索信息：

{retriever_result}

用户问题：

{query}

请按照以下步骤回答用户的问题：
回答流程分为两个部分：
先在<think></think>标签中思考，然后正式回答

----
示例：
<think>用户的问题提到了shor算法在检索信息中有shor算法的相关信息，我将从如下几个方面开始回答...</think>
#### **Shor算法的定义与背景**
Shor算法是一种量子算法，旨在解决大整数因数分解问题...
#### **Shor算法的数学原理**
Shor算法的实现依赖于以下关键数学步骤：
1. **量子傅里叶变换（QFT）**：
   - 量子傅里叶变换是Shor算法的核心操作之一。它将量子态从位置基转换为动量基，利用量子叠加态的并行性加速计算。
   ......
**参考文献：**
[1] 参考文献1
[2] 参考文献2
----

1. 思考过程
在 <think></think> 标签中，简要说明您打算如何回答这个问题。
先检查检索信息 {retriever_result} 是否与用户问题相关：
如果相关，说明您将如何使用检索信息（例如引用文本、图片或公式）来回答。
如果问题与量子或检索信息相关性不高，则忽略检索信息使用具备的量子领域基础知识进行回答。

2. 回答内容
在 <think></think> 标签包裹的思考内容后，提供详细的回答。
如果检索信息与问题相关：
使用检索信息中的内容支持您的回答。
对于图片，引用格式为 ![Image](具体图片url)
示例: 
下图表明了shor算法的量子线路结构![Image](http://example.com/image1)

注意！！！思考和回答的过程隐式进行，仅通过<think></think>标签包裹来进行区分

对于公式，引用格式为 $$具体公式$$，首次引用时编号为“公式1”，后续直接写“公式1”。示例：
首次引用：$$E = mc^2$$（公式1）
后续引用：公式1
确保引用的图片 URL 或公式与检索信息中的一致。
回答的组织：
将内容分成清晰的部分（如段落或小标题），不用为大纲编号。
参考文献：
如果问题与量子文献相关，在正式的回答最后按照标准的序号加参考文献链接的格式列出参考文献：
{references}
（注：参考文献只能使用这里列出的文献项，不要修改或添加任何内容，也不要擅自总结）

如果问题与量子无关，不回答量子相关内容，不列参考文献，也不加额外备注。
注意事项：

回答格式要正确，图片和公式引用要清晰。
在回答中提到图片或公式时，立即引用其编号和链接，不要等到最后才引用。
只引用检索信息中提供的图片和公式，不添加外部内容。
"""

 
        # logging.info(f"RAG模板: {rag_template}")
        new_messages[-1]["content"][0]["text"] = rag_template


    return new_messages, None,references,image_urls



# 设置RAG系统
documents_paper = load_or_update_chunks('/root/autodl-tmp/markitdown/all_scratch_without_annotation','paper_chunks_without_annotation.pkl',"paper_file_metadata_without_annotation.json")
documents_instrcut = load_or_update_chunks('/root/autodl-tmp/markitdown/all_instruct_scratch_without_annotation','instruct_chunks_without_annotation.pkl',"instruct_file_metadata_without_annotation.json")
documents_list = [documents_paper,documents_instrcut]
faiss_index_paths = ["faiss_index_kb1", "faiss_index_kb2"]
retriever = setup_rag_system(documents_list,faiss_index_paths)
tokenizer = AutoTokenizer.from_pretrained('/root/autodl-tmp/markitdown/sft_output_4_9')

# 配置日志
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)