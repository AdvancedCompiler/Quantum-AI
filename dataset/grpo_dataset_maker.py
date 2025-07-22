import os
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain.schema import Document
from langchain_core.retrievers import BaseRetriever
import pickle
import json
from glob import glob
import re

# 设置环境变量 HF_ENDPOINT
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"


# 自定义 Markdown 分割器，按字数阈值分割
class WordCountMarkdownSplitter:
    def __init__(self, chunk_size=250):
        self.chunk_size = chunk_size

    def split_text(self, text: str) -> list[str]:
        paragraphs = text.split("\n\n")
        chunks = []
        current_chunk = []
        current_length = 0

        for paragraph in paragraphs:
            paragraph = paragraph.strip()
            if not paragraph:
                continue

            paragraph_length = len(paragraph)
            if current_length + paragraph_length > self.chunk_size and current_chunk:
                chunks.append("\n\n".join(current_chunk))
                current_chunk = [paragraph]
                current_length = paragraph_length
            else:
                current_chunk.append(paragraph)
                current_length += paragraph_length

        if current_chunk:
            chunks.append("\n\n".join(current_chunk))
        return chunks

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
        splitter = WordCountMarkdownSplitter(chunk_size=250)

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

# 自定义检索器
class ContextualRetriever(BaseRetriever):
    def __init__(self, vector_store, k=1, context_window=1):
        super().__init__()
        self._vector_store = vector_store
        self._k = k
        self._context_window = context_window
    
    def _get_relevant_documents(self, query: str) -> list[Document]:
        initial_docs = self._vector_store.similarity_search(query, k=self._k)
        all_docs = list(self._vector_store.docstore._dict.values())
        all_docs = sorted(all_docs, key=lambda x: x.metadata["index"])
        result_docs = []
        for doc in initial_docs:
            idx = doc.metadata["index"]
            start_idx = max(0, idx - self._context_window)
            end_idx = min(len(all_docs), idx + self._context_window + 1)
            for i in range(start_idx, end_idx):
                result_docs.append(all_docs[i])
        seen = set()
        final_docs = [doc for doc in result_docs if not (doc.page_content in seen or seen.add(doc.page_content))]
        
        sources = set()
        for doc in final_docs:
            sources.add(doc.metadata['source'])
            
        sources_doc = Document(
            page_content="\n\n来源:\n" + "\n".join(sorted(sources)),
            metadata={"index": -1, "source": "sources"}
        )
        final_docs.append(sources_doc)
        
        for doc in final_docs:
            content = doc.page_content
            img_pattern = r'!\[.*?\]\((.*?)\)'
            matches = re.findall(img_pattern, content)
            for match in matches:
                if not match.startswith('http'):
                    new_path = f'https://serverip/images/{match}'
                    content = content.replace(match, new_path)
            doc.page_content = content

        return final_docs

def setup_rag_system(documents, faiss_index_path="faiss_index"):
    """
    设置RAG系统，包括嵌入、向量存储和检索器
    
    Args:
        documents: 分割后的文档列表
        faiss_index_path: FAISS索引保存路径
    
    Returns:
        ContextualRetriever: 配置好的检索器
    """
    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/paraphrase-mpnet-base-v2")
    
    if os.path.exists(faiss_index_path):
        print("加载现有的向量存储...")
        vector_store = FAISS.load_local(faiss_index_path, embeddings, allow_dangerous_deserialization=True)
        if len(vector_store.docstore._dict) != len(documents):
            print("文档数量不匹配，重新生成向量存储...")
            vector_store = FAISS.from_documents(documents, embeddings)
            vector_store.save_local(faiss_index_path)
    else:
        print("创建新的向量存储...")
        vector_store = FAISS.from_documents(documents, embeddings)
        vector_store.save_local(faiss_index_path)
    
    retriever = ContextualRetriever(vector_store, k=2, context_window=3)
    return retriever

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
    source_documents = retriever.get_relevant_documents(query)
    
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
    references = "**参考文献**:\n"
    for i, source in enumerate(sorted(unique_sources), 1):
        references += f"[{i}] {source}\n"
    
    # 拼接最终结果
    final_result = f"{answer}\n\n{references}"
    return final_result


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
    
    return result


import csv
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
                new_line = line  # 如果未找到，保留原行
        else:
            new_line = line  # 非参考文献行保持不变
        new_lines.append(new_line)
    
    # 将所有行重新组合成字符串
    return '\n'.join(new_lines)

documents = load_or_update_chunks()
    
# 设置RAG系统
retriever = setup_rag_system(documents)

import json 

with open("questions.json", "r", encoding="utf-8") as f:
    questions = json.load(f)

datas=[]
for question in questions:
    query = question["Question"]
    retriever_result = query_rag(retriever, query)
    retriever_result = replace_paths_with_urls(retriever_result,'arxiv.csv')
    retriever_result = extract_and_summarize(retriever_result)
    retriever_result = retriever_result[:6400]

    rag_template = f"""
    您是一个量子领域的专家，您的任务是基于以下检索信息回答用户的问题：

    检索信息：
    {retriever_result}

    用户问题：
    {query}

    请先思考如何回答这个问题，并将您的思考过程写在<think>标签中。

    在思考开始时你需要先检查上下文最后是否有图片引用和公式的汇总，若有则需要在思考过程中进行复述出现的图片引用和公式，若有则需要在思考过程中进行复述出现的图片引用和公式，若存在图片则需要找到上下文中图片的引用和公式出现的位置，结合周围的文本进行组织语言，然后在</think>后输出您结合上下的详细回答并附带图片和公式的引用。

    在回答中，请尽量应用LaTeX公式或图片引用，以使回答更加清晰和生动。

    请在回答的最后附上参考文献的信息，格式与检索信息中的**参考文献**处相同。

    请注意，尽量引用图片url和latex公式但您只能引用检索信息中出现的图片和参考文献，禁止引用非上下文中的图片或参考文献

    你需要组织排版，按照合适的大纲进行回答

    """
    
    data = {
        "prompt": [
            {
                "role": "user",
                "content": rag_template
            }
        ]
    }
    datas.append(data)

with open("grpo_dataset.json", "w", encoding="utf-8") as f:
    json.dump(datas, f, ensure_ascii=False)
