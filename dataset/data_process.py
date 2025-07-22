import json
import csv
from pathlib import Path

# 读取 arxiv.csv，创建 title 到 (title, url) 的映射
# 这里假设 title 字段直接对应来源中的文件名主干（不含路径和扩展名）
with open('arxiv.csv', 'r') as f:
    reader = csv.DictReader(f)
    title_to_info = {row['title']: (row['title'], row['url']) for row in reader}

# 读取原始 JSON 数据集
with open('questions_with_answers.json', 'r') as f:
    data = json.load(f)
    # 如果数据不是列表，则将其包装为列表
    if not isinstance(data, list):
        data = [data]

# 处理每个数据项并写入 dataset.jsonl
with open('dataset.jsonl', 'w') as f:
    for item in data:
        # 提取字段
        question = item['Question']
        sources = item['Sources']  # 来源文件名列表
        context = item['Context']
        
        # 将来源文件名映射到 title 和 url
        references = []
        for source in sources:
            # 提取文件名主干（去掉路径和扩展名）
            filename = Path(source).stem
            # 假设文件名去掉 "-with-image-refs" 等后缀后与 title 匹配
            # 这里简化为直接使用文件名与 title 比较，实际情况可能需要调整
            title_key = filename.replace('-with-image-refs', '')
            if title_key in title_to_info:
                title, url = title_to_info[title_key]
                references.append(f"[{len(references)+1}] {title} - {url}")
            else:
                references.append(f"[{len(references)+1}] Unknown - Unknown")
        
        # 格式化参考文献
        references_str = "### **参考文献**\n" + "\n".join(references)
        
        # 构造提示文本
        prompt = (
            "请基于以下上下文，回答用户的问题。\n\n"
            f"**用户问题：**\n{question}\n\n"
            f"**上下文：**\n{context}\n\n"
            f"{references_str}\n\n"
            "请提供一个专业性的回答。"
        )
        
        # 创建 JSON 对象
        json_obj = {
            "prompt": prompt,
            "completion": ""
        }
        
        # 写入 dataset.jsonl，每行一个 JSON 对象
        f.write(json.dumps(json_obj, ensure_ascii=False) + '\n')