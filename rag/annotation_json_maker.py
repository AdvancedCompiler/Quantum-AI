import os
import re
import json
import base64
import mimetypes
from urllib.parse import urlparse, unquote
from openai import OpenAI
from tqdm import tqdm
from concurrent.futures import ThreadPoolExecutor, as_completed

# 初始化 OpenAI 客户端
client = OpenAI(
    api_key='xxxxxxxx',
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

# 将本地图片编码为 base64 格式
def encode_image(image_path):
    """将本地图片文件编码为 base64 字符串"""
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    mime_type, _ = mimetypes.guess_type(image_path)
    if mime_type is None:
        mime_type = "image/jpeg"
    return f"data:{mime_type};base64,{encoded_string}"

# 提取图片上下文
def extract_context(text, start_pos, end_pos, max_tokens=2000):
    """从 Markdown 文本中提取图片引用周围的上下文，限制 token 数"""
    max_chars = max_tokens * 4
    left_context = ""
    pos = start_pos - 1
    while pos >= 0 and len(left_context) < max_chars // 2:
        left_context = text[pos] + left_context
        pos -= 1
    right_context = ""
    pos = end_pos
    while pos < len(text) and len(right_context) < max_chars // 2:
        right_context += text[pos]
        pos += 1
    context = left_context + right_context
    if len(context) > max_chars:
        context = context[:max_chars]
    return context

# 调用模型生成图片描述和名称
def get_image_description(prompt, image_url, max_retries=3):
    """调用 Qwen-VL-Max-Latest 模型生成图片名称和描述，支持重试机制"""
    for attempt in range(max_retries):
        try:
            completion = client.chat.completions.create(
                model="qwen-vl-max-latest",
                messages=[{
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": image_url}}
                    ]
                }],
                response_format={"type": "json_object"}
            )
            response_text = completion.choices[0].message.content
            response_json = json.loads(response_text)
            if "image_title" in response_json and "description" in response_json:
                return response_json
            else:
                print(f"尝试 {attempt+1}: 响应中未找到 'image_title' 或 'description' 字段")
        except json.JSONDecodeError:
            print(f"尝试 {attempt+1}: 返回的 JSON 格式无效")
        except Exception as e:
            print(f"尝试 {attempt+1}: 错误 - {str(e)}")
    return None

# 处理单个 Markdown 文件
def process_single_markdown(md_file_path, progress_bar):
    """处理单个 Markdown 文件，返回图片解读结果，并更新进度条"""
    with open(md_file_path, 'r', encoding='utf-8') as f:
        md_text = f.read()

    image_pattern = r'!\[[^\]]*\]\(([^)]+)\)'
    matches = list(re.finditer(image_pattern, md_text))
    results = []

    for index, match in enumerate(matches):
        image_rel_path = match.group(1)
        image_rel_path = unquote(image_rel_path)

        if image_rel_path.startswith("http://") or image_rel_path.startswith("https://"):
            image_url = image_rel_path
            image_abs_path = image_rel_path
        elif os.path.isabs(image_rel_path):
            image_abs_path = image_rel_path
            if not os.path.exists(image_abs_path):
                print(f"图片文件未找到: {image_abs_path}")
                progress_bar.update(1)
                continue
            image_url = encode_image(image_abs_path)
        else:
            image_abs_path = os.path.abspath(os.path.join(os.path.dirname(md_file_path), image_rel_path))
            if not os.path.exists(image_abs_path):
                print(f"图片文件未找到: {image_abs_path}")
                progress_bar.update(1)
                continue
            image_url = encode_image(image_abs_path)

        print(f"处理图片引用: {match.group(0)} (文件: {md_file_path})")
        print(f"计算得到的绝对路径: {image_abs_path}")

        context = extract_context(md_text, match.start(), match.end())
        line_number = md_text[:match.start()].count('\n') + 1

        prompt = (
            f"请根据以下上下文和提供的图片内容进行解读，生成一个简洁的图片名称（image_title）和描述（description），以便单模态大模型能够理解图片的内容。上下文如下：\n\n"
            f"{context}\n\n"
            f"在这个上下文中，当前处理的图片是第 {index + 1} 个图片引用（位于第 {line_number} 行，引用为 {match.group(0)}）。"
            f"请专注于这个图片，忽略其他可能存在的图片引用。图片名称应从上下文提取或根据图片内容总结，不使用图片文件名。\n\n"
            f"请以 JSON 格式返回结果，包含 \"image_title\" 和 \"description\" 两个字段。如果图片与上下文明显无关，请返回 {{\"image_title\": \"无关图片\", \"description\": \"无关图片\"}}。"
        )

        response = get_image_description(prompt, image_url)
        progress_bar.update(1)
        if response is None or response.get("image_title") == "无关图片":
            continue
        else:
            results.append({
                "url": image_abs_path,
                "image_title": response["image_title"],
                "description": response["description"]
            })

    return results

# 追加结果到 JSON 文件（线程安全）
def append_to_json_file(output_json_path, results, lock):
    """将结果追加到 JSON 文件中，使用锁确保线程安全"""
    with lock:
        if not os.path.exists(output_json_path):
            with open(output_json_path, 'w', encoding='utf-8') as f:
                json.dump([], f, ensure_ascii=False, indent=4)

        with open(output_json_path, 'r', encoding='utf-8') as f:
            existing_data = json.load(f)

        existing_data.extend(results)

        with open(output_json_path, 'w', encoding='utf-8') as f:
            json.dump(existing_data, f, ensure_ascii=False, indent=4)

# 批处理所有 Markdown 文件
def batch_process_markdown(root_dir, output_json_path, processed_log="batch_processed_files.log", max_workers=4):
    """批处理指定路径下所有 Markdown 文件，使用多线程，逐文件追加结果，并显示图片处理进度条"""
    from threading import Lock
    lock = Lock()  # 创建线程锁，确保 JSON 文件写入安全

    # 加载已处理文件记录
    processed_files = set()
    if os.path.exists(processed_log):
        with open(processed_log, 'r', encoding='utf-8') as f:
            processed_files = set(line.strip() for line in f)

    # 收集所有待处理的 Markdown 文件并计算总图片数
    md_files = []
    total_images = 0
    for root, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.md'):
                md_file_path = os.path.abspath(os.path.join(root, file))
                if md_file_path not in processed_files:
                    md_files.append(md_file_path)
                    with open(md_file_path, 'r', encoding='utf-8') as f:
                        md_text = f.read()
                        image_pattern = r'!\[[^\]]*\]\(([^)]+)\)'
                        total_images += len(re.findall(image_pattern, md_text))

    # 初始化进度条
    print(f"总共需要处理 {total_images} 张图片")
    with tqdm(total=total_images, desc="处理图片进度", unit="image") as pbar:
        # 使用线程池处理 Markdown 文件
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            # 提交任务
            future_to_file = {executor.submit(process_single_markdown, md_file, pbar): md_file for md_file in md_files}
            
            # 处理完成的任务
            for future in as_completed(future_to_file):
                md_file_path = future_to_file[future]
                try:
                    results = future.result()
                    if results:
                        append_to_json_file(output_json_path, results, lock)  # 线程安全追加结果
                    # 记录已处理文件
                    with lock:  # 确保 processed_log 写入也是线程安全的
                        with open(processed_log, 'a', encoding='utf-8') as f:
                            f.write(f"{md_file_path}\n")
                        processed_files.add(md_file_path)
                    print(f"\n完成处理: {md_file_path}")
                except Exception as e:
                    print(f"处理 {md_file_path} 时出错: {str(e)}")

    print(f"\n所有结果已保存至: {output_json_path}")
    print(f"已处理文件记录保存至: {processed_log}")

# 示例用法
if __name__ == "__main__":
    root_dir = "all_instruct_scratch_without_annotation"
    output_json_path = "batch_annotation.json"
    batch_process_markdown(root_dir, output_json_path, max_workers=30)