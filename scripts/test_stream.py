import requests
import json
import os
import sys

# 尝试设置控制台编码为UTF-8，尤其在Windows环境下
def setup_console_encoding():
    # 对于Windows系统，尝试设置控制台代码页
    if sys.platform.startswith('win'):
        try:
            # 设置控制台代码页为65001 (UTF-8)
            import subprocess
            subprocess.run(['chcp', '65001'], shell=True, check=False)
            
            # 设置Python控制台输出编码
            import io
            sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
            print("已将Windows控制台编码设置为UTF-8")
        except Exception as e:
            print(f"设置Windows控制台编码时出错: {e}")
    
    # 检查并打印当前控制台编码
    print(f"当前控制台编码: {sys.stdout.encoding}")
    
    # 设置请求库的默认编码
    requests.utils.default_encoding = 'utf-8'

def process_sse_stream(response, chunk_size=8):
    """处理SSE流式响应，正确解析多行data字段并保留换行符
    
    Args:
        response: HTTP响应对象，支持iter_content方法
        chunk_size: 每次读取的字节数，较大的值有助于处理多字节UTF-8字符
    Returns:
        str: 完整的响应内容
    """
    full_response = ""
    buffer = b""  # 字节缓冲区
    text_buffer = ""  # 文本缓冲区
    
    print("开始解析SSE流...")
    
    for chunk in response.iter_content(chunk_size=chunk_size):
        if not chunk:
            continue
        
        buffer += chunk
        
        try:
            text = buffer.decode('utf-8')
            buffer = b""
            text_buffer += text
            
            # 处理所有完整的SSE消息（以<quan-cut>分隔）
            while '<quan-cut>' in text_buffer:
                message_end = text_buffer.index('<quan-cut>')
                message = text_buffer[:message_end]
                text_buffer = text_buffer[message_end + 10:]  # 跳过<quan-cut>
                
                # print(message)
                if not message.strip():
                    continue
                data = json.loads(message)
                if data['event'] == 'start':
                    print('开始接收响应...')
                elif data['event'] == 'message':
                    sys.stdout.write(data['data'])
                    sys.stdout.flush()
                    full_response += data['data']
                elif data['event'] == 'done':
                    print('done')
                elif data['event'] == 'error':
                    raise Exception(f"错误: {data['data']}")
                
                # lines = message.split('\n')
                
                # data_lines = []  # 收集所有data行
                # event_type = None
                
                # for line in lines:
                #     line = line.strip()
                #     if line.startswith('data:'):
                #         if line[6:]:
                #             data_lines.append(line[6:])
                #         else:
                #             data_lines.append('')
                #     elif line.startswith('event:'):
                #         event_type = line[6:].strip()
                
                # if data_lines:
                #     # 将多个data行用换行符连接
                #     data_content = '\n'.join(data_lines)
                    
                #     if event_type == "start":
                #         print("开始接收响应...")
                #     elif event_type == "done":
                #         print("\n响应完成")
                #     elif event_type == "error":
                #         print(f"\n错误: {data_content}")
                #     else:  # 默认为message或未指定事件类型
                #         sys.stdout.write(data_content)
                #         sys.stdout.flush()
                #         full_response += data_content
        
        except UnicodeDecodeError:
            # 继续累积字节以处理多字节字符
            if len(buffer) > 1024:
                print(f"\n[解码错误: 缓冲区过大 ({len(buffer)} 字节), 清空缓冲区]")
                buffer = b""
        except Exception as e:
            print(f""" 当前块：{buffer}
                  处理SSE流时发生错误: {e}""")
            break
    
    # 处理可能未完成的最后一个消息
    if text_buffer.strip():
        lines = text_buffer.split('\n')
        data_lines = [line[5:].lstrip() for line in lines if line.startswith('data:')]
        if data_lines:
            data_content = '\n'.join(data_lines)
            sys.stdout.write(data_content)
            sys.stdout.flush()
            full_response += data_content
    
    return full_response

def send_stream_request(url, payload, description="请求"):
    """发送流式请求并处理响应"""
    headers = {
        "Content-Type": "application/json",
        "Accept": "text/event-stream"
    }
    
    print(f"发送{description}...")
    
    try:
        # 设置超时和编码
        response = requests.post(
            url, 
            json=payload, 
            headers=headers, 
            stream=True, 
            timeout=60
        )
        
        if response.status_code == 200:
            print(f"接收SSE流式响应 (HTTP {response.status_code}):")
            full_response = process_sse_stream(response)
            print("\n===========================\n完整响应:")
            print(full_response)
            return True, full_response
        else:
            print(f"请求失败: HTTP {response.status_code}")
            print(response.text)
            return False, None
    except requests.exceptions.RequestException as e:
        print(f"请求异常: {e}")
        return False, None

def test_chat_stream_text():
    """测试纯文本消息的流式响应"""
    url = "http://localhost:6006/chat"
    
    # 构建请求体 - 纯文本消息
    payload = {
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "你好"
                    }
                ]
            },

             {
                "role": "assistant",
                "content": [
                    {
                        "type": "text",
                        "text": "你好"
                    }
                ]
            },
            {
                "role": "user",
                "content": [
                    
                    {
                        "type": "text",
                        "text": """
量子纠缠
"""
                    }
                ]
            }
        ],
        "stream": True
    }
    
    success, response = send_stream_request(url, payload, "纯文本请求")
    if success:
        print("\n纯文本测试完成")

def test_chat_stream_with_image(image_path):
    """测试包含图片的消息的流式响应"""
    # if not os.path.exists(image_path):
    #     print(f"图片不存在: {image_path}")
    #     return
        
    
    url = "http://localhost:6006/chat"
    
    # 构建请求体 - 包含图片的消息
    payload = {
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "image_url",
             "image_url": {"url": image_path}},
                    {
                        "type": "text",
                        "text": "图片中描述了什么"
                    },
                ]
            }
        ],
        "stream": True
    }
    
    success, response = send_stream_request(url, payload, "带图片的请求")
    if success:
        print("\n图片测试完成")

if __name__ == "__main__":
    # 设置控制台编码
    setup_console_encoding()
    
    # 确保控制台使用UTF-8编码
    if sys.stdout.encoding.lower() != 'utf-8':
        print(f"警告: 控制台编码不是UTF-8 (当前: {sys.stdout.encoding})，可能会显示乱码")
    
    # 测试纯文本消息
    # test_chat_stream_text()
    
    # 测试带图片的消息 (需要提供有效的图片路径)
    # 例如：本地图片路径
    image_path = "https://cn.bing.com/th?id=OHR.PandaSnow_ZH-CN5981854301_1920x1080.jpg&w=720"  # 替换为实际图片路径
    test_chat_stream_with_image(image_path)