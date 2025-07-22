from qwen_rag_utils import *
# 设置环境变量 HF_ENDPOINT镜像
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
url_prefix = "http://129.211.221.44:8062/user/show?filePath=./image/"


# 配置日志
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)



app = Flask(__name__)

# Initialize OpenAI clients for Qwen and DeepSeek
qwen_client = OpenAI(
    base_url="http://localhost:8001/v1",
    api_key="EMPTY",
)

deepseek_client = OpenAI(
    base_url="http://localhost:8000/v1",
    api_key="EMPTY",
)



@app.route("/chat", methods=["POST"])
def chat():
    """API endpoint to handle chat requests with optional streaming support."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "Invalid input: Request body must be JSON."}), 400
        
        with open('last_request.txt', 'w', encoding='utf-8') as file:
            data_str = json.dumps(data)
            file.write(data_str)

        messages = data.get("messages", [])
        row_messages = messages
        model = data.get("model", "default")  # Ignored for now, always uses deepseek for final response
        stream = data.get("stream", False)  # Get stream parameter, default is False

        # Process messages
        processed_messages, error,references,image_urls = process_messages(messages)
        if error:
            logging.error(f"处理消息错误: {error}")
            return jsonify({"error": error}), 400
        
        try:
            cutter = "<quan-cut>"
            # 根据stream参数决定是否使用流式响应
            if stream:
                def generate_response():
                    try:
                        response = deepseek_client.chat.completions.create(
                            model="deepseek",
                            messages=processed_messages,
                            stream=True 
                        )
                        first_chunk = True
                        msg_id = 0
                        yield json.dumps({'id':msg_id,'event':'start','data':''})+cutter
                        # yield f"event: start\ndata: \n\n"
                        
                        # 用于存储所有内容的列表
                        all_content = []
                        
                        for chunk in response:
                            content = chunk.choices[0].delta.content
                            if content:
                                msg_id += 1
                                # 处理换行符
                                
                                if first_chunk:
                                    # 第一个数据块的首行添加<think>
                                    content = f'<think>{content}'
                                    first_chunk = False
                                
                                # 将当前chunk的内容添加到all_content中
                                all_content.extend(content)
                                
                                # 构造多行data字段
                                yield json.dumps({'id':msg_id,'event':'message','data':content})+cutter
                        
                        # 检查是否存在图片但一个也没被引用
                        image_pattern = r'!\[.*?\]\((.*?)\)'
                        
                        output = ''.join(all_content)

                        with open('last_response.txt', 'w', encoding='utf-8') as file:
                            file.write(output)
                        # 提取所有图片引用
                        image_urls_from_output = re.findall(image_pattern, output, re.DOTALL)
                        intersection = set(image_urls_from_output) & set(image_urls)
                    
                        if not references:
                            print('参考文献为空','\n','='*50)
                        # 在所有内容处理完成后，添加references
                        yield json.dumps({'id':msg_id+1,'event':'done','data':''})
                    except Exception as e:
                        logging.error(f"流式响应错误: {str(e)}")
                        yield json.dumps({'id':msg_id+1,'event':'error','data':str(e)})
                
                
                # 添加更多SSE相关的响应头
                response = Response(generate_response(), content_type='text/event-stream')
                response.headers['Cache-Control'] = 'no-cache'
                response.headers['X-Accel-Buffering'] = 'no'  # 禁用Nginx缓冲
                response.headers['Connection'] = 'keep-alive'
                return response
            else:
                # 非流式响应
                print(processed_messages)
                response = deepseek_client.chat.completions.create(
                    model="deepseek",
                    messages=processed_messages
                )
                # 在非流式响应的开头添加<think>标签
                return jsonify({"response": "<think>" + response.choices[0].message.content}), 200
        except Exception as e:
            logging.error(f"DeepSeek模型错误: {str(e)}")
            return jsonify({"error": f"Error calling DeepSeek model: {str(e)}"}), 500

    except Exception as e:
        logging.error(f"服务器错误: {str(e)}")
        logging.error(row_messages)
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=6006,debug=False,threaded=True)  # Runs on HTTP (adjust to HTTPS if needed)