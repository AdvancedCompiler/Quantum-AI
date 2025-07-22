from unsloth import FastLanguageModel, is_bfloat16_supported
import torch
max_seq_length = 8000 # Can increase for longer reasoning traces
lora_rank = 16 # Larger rank = smarter, but slower
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
logging.getLogger().setLevel(logging.CRITICAL)
###


# 设置 CUDA_VISIBLE_DEVICES
model, tokenizer = FastLanguageModel.from_pretrained(
    model_name = "/root/autodl-tmp/models/deepseek-lla",
    max_seq_length = max_seq_length,
    load_in_4bit = False, # False for LoRA 16bit
    fast_inference = True, # Enable vLLM fast inference
    max_lora_rank = lora_rank,
    gpu_memory_utilization = 0.8, # Reduce if out of memory
    device_map="auto"
)



model = FastLanguageModel.get_peft_model(
    model,
    r = lora_rank, # Choose any number > 0 ! Suggested 8, 16, 32, 64, 128
    target_modules = [
        # "q_proj", "k_proj", "v_proj", "o_proj",
        "gate_proj", "up_proj", "down_proj",
    ], # Remove QKVO if out of memory
    lora_alpha = lora_rank,
    use_gradient_checkpointing = "unsloth", # Enable long context finetuning
    random_state = 3407,
)




from trl import GRPOConfig, GRPOTrainer
training_args = GRPOConfig(
    use_vllm = True, # use vLLM for fast inference!
    learning_rate = 5e-6,
    adam_beta1 = 0.9,
    adam_beta2 = 0.99,
    weight_decay = 0.1,
    warmup_ratio = 0.1,
    lr_scheduler_type = "cosine",
    optim = "paged_adamw_8bit",
    logging_steps = 1,
    bf16 = is_bfloat16_supported(),
    fp16 = not is_bfloat16_supported(),
    # per_device_train_batch_size = 2,
    gradient_accumulation_steps = 4, # Increase to 4 for smoother training
    num_generations = 4, # Decrease if out of memory
    max_prompt_length = 7000,
    max_completion_length = 2000,
    num_train_epochs = 3, # Set to 1 for a full training run
    max_steps = 120,
    save_steps = 60,
    max_grad_norm = 0.1,
    report_to = "none", # Can use Weights & Biases
    output_dir = "outputs_3.15",
)


###########

import re
import logging

# 设置日志记录
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Helper Functions
def extract_reference_section(prompt):
    """Extract the reference section from the prompt."""
    content = prompt[-1]['content']
    match = re.search(r'\*\*参考文献\*\*:\n(.*?)(?=\n\n|$)', content, re.DOTALL)
    ref_text = match.group(1).strip() if match else ""
    logger.info(f"Extracted reference section from prompt: {ref_text}")
    return ref_text

def extract_images(prompt):
    """Extract set of image URLs from the image summary section."""
    content = prompt[-1]['content']
    match = re.search(r'\*\*图片引用汇总:\*\*\n(.*?)(?=\n\n|$)', content, re.DOTALL)
    if match:
        image_section = match.group(1)
        images = set(re.findall(r'!\[\d+\]\((.*?)\)', image_section))
        logger.info(f"Extracted images from prompt: {images}")
        return images
    logger.info("No image summary section found in prompt.")
    return set()

def extract_formulas(prompt):
    """Extract set of LaTeX formulas from the formula summary section."""
    content = prompt[-1]['content']
    match = re.search(r'\*\*LaTeX公式汇总:\*\*\n(.*?)(?=\n\n|$)', content, re.DOTALL)
    if match:
        formula_section = match.group(1)
        formulas = set(re.findall(r'\$\$(.*?)\$\$', formula_section, re.DOTALL))
        logger.info(f"Extracted formulas from prompt: {formulas}")
        return formulas
    logger.info("No formula summary section found in prompt.")
    return set()

def parse_completion(completion, reference_section):
    """
    Parse completion if it matches the required format, return think content.
    Modified to only require <think> tag and reference section.
    """
    response = completion[0]['content']
    
    # logger.info(f"Completion: {response}")
    pattern = r"(?s)^<think>(.*?)\n</think>\s*(.+)"
    match = re.match(pattern, response)
    if match:
        think_content = match.group(1)
        # logger.info(f"Parsed think content from completion: {think_content}")
        return think_content
    logger.info("Completion does not match the required format.")
    # logger.info(f"Completion: {completion[0]['content']}")    
    return None

def extract_prompt_reference_urls(prompt):
    """从prompt的参考文献部分提取所有URL"""
    ref_text = extract_reference_section(prompt)
    ### 匹配类似https://arxiv.org/abs/2202.12496
    urls = set(re.findall(r'https://arxiv.org/abs/\d{4}\.\d{4,5}', ref_text))
    logger.info(f"Extracted URLs from prompt reference: {urls}")
    return urls

def extract_completion_reference_urls(completion_content):
    """从回答中灵活提取参考文献部分的URL（宽松匹配）"""
    ref_match = re.search(
        r'参考文献[：:\s]*(.*)', 
        completion_content, 
        re.DOTALL | re.IGNORECASE
    )
    if ref_match:
        ref_section = ref_match.group(1)
        urls = set(re.findall(r'https://arxiv.org/abs/\d{4}\.\d{4,5}', ref_section))
        logger.info(f"回答中的reference部分Extracted reference section from completion: {ref_section}")
        logger.info(f"Extracted URLs from completion reference: {urls}")
        return urls
    logger.info("No reference section found in completion.")
    return set()

# Scoring Functions
def reference_score_func(prompts, completions, **kwargs) -> list[float]:
    """新版参考文献评分：仅验证URL存在性，不检查格式"""
    for completion in completions:
        completion[0]['content'] = "<think>" + completion[0]['content']

    scores = []
    for i, (prompt, completion) in enumerate(zip(prompts, completions)):
        logger.info(f"\n\n### Scoring Reference for Pair {i+1} ###")
        # logger.info(f"Prompt: {prompt}")
        # logger.info(f"Completion: {completion}")
        
        # 提取标准答案URL集合
        ref_urls = extract_prompt_reference_urls(prompt)
        if not ref_urls:
            logger.info("prompt中没有参考文献, score: 0.0")
            scores.append(0.0)
            continue
            
        # 提取回答中的参考文献URL
        completion_urls = extract_completion_reference_urls(completion[0]['content'])
        
        # 计算覆盖率
        correct_urls = ref_urls & completion_urls
        score = len(correct_urls) / len(ref_urls) if ref_urls else 0.0
        logger.info(f"\n\n正确的 URLs: {correct_urls}\n\n")
        logger.info(f"\n\n参考得分 score: {score}\n\n")
        scores.append(score)
    
    return scores

def think_content_score_func(prompts, completions, **kwargs) -> list[float]:
    """
    Evaluate the proportion of images and formulas restated in <think> for each completion.
    Modified to only check <think> tag.
    """
 
    
    scores = []
    for i, (prompt, completion) in enumerate(zip(prompts, completions)):
        logger.info(f"\n\n### Scoring Think Content for Pair {i+1} ###")
        reference_section = extract_reference_section(prompt)
        parsed = parse_completion(completion, reference_section)
        if not parsed:
            logger.info("Completion does not match the required format, score: 0.0")
            scores.append(0.0)
            continue
        logger.info(f"找到了think部分\n\n")
        think_content = parsed
        images = extract_images(prompt)
        formulas = extract_formulas(prompt)
        
        mentioned_images = set(re.findall(r'!\[.*?\]\((.*?)\)', think_content))
        mentioned_formulas = set(re.findall(r'\$\$(.*?)\$\$', think_content, re.DOTALL))
        correct_images = mentioned_images & images
        correct_formulas = mentioned_formulas & formulas
        
        logger.info(f"\n\nMentioned images in think: {mentioned_images}")
        logger.info(f"\n\nMentioned formulas in think: {mentioned_formulas}")
        logger.info(f"\n\nCorrect images: {correct_images}")
        logger.info(f"\n\nCorrect formulas: {correct_formulas}")
        
        total_items = len(images) + len(formulas)
        if total_items == 0:
            logger.info("\n\nprompt中没有图片和公式, score: 0.0")
            scores.append(0.0)
            continue
        
        score = (len(correct_images) + len(correct_formulas)) / total_items
        logger.info(f"Think content score: {score}")
        scores.append(score)
    
    return scores

def answer_content_score_func(prompts, completions, **kwargs) -> list[float]:
    """
    Evaluate the proportion of images and formulas cited in the completion for each completion.
    Modified to check the entire completion content (excluding <think> and reference section).
    """

    
    scores = []
    for i, (prompt, completion) in enumerate(zip(prompts, completions)):
        logger.info(f"\n\n### Scoring Answer Content for Pair {i+1} ###")
        reference_section = extract_reference_section(prompt)
        parsed = parse_completion(completion, reference_section)
        if not parsed:
            logger.info("未匹配到**参考文献**, score: 0.0")
            scores.append(0.0)
            continue
        
        think_content = parsed
        full_content = completion[0]['content']
        answer_content = re.sub(r'(?s)^<think>\n.*?\n</think>\s*', '', full_content)
        ##找到<think>之后的内容
        answer_content = re.sub(re.escape(reference_section) + r'$', '', answer_content)
        logger.info(f"Extracted answer content: {answer_content}")
        
        images = extract_images(prompt)
        print("文献中的图片:",images)
        formulas = extract_formulas(prompt)
        
        cited_images = set(re.findall(r'!\[.*?\]\((.*?)\)', answer_content))
        cited_formulas = set(re.findall(r'\$\$(.*?)\$\$', answer_content, re.DOTALL))
        correct_images = cited_images & images
        correct_formulas = cited_formulas & formulas
        
        logger.info(f"\n\n答案中引用图片Cited images in answer: {cited_images}")
        logger.info(f"\n\n答案中引用公式Cited formulas in answer: {cited_formulas}")
        logger.info(f"\n\n正确引用图片Correct images: {correct_images}")
        logger.info(f"\n\n正确引用公式Correct formulas: {correct_formulas}")
        
        total_items = len(images) + len(formulas)
        if total_items == 0:
            logger.info("No images or formulas in prompt, score: 0.0")
            scores.append(0.0)
            continue
        
        score = (len(correct_images) + len(correct_formulas)) / total_items
        logger.info(f"\n\nAnswer content score: {score}")
        scores.append(score)
    
    return scores

def image_bonus_score_func(prompts, completions, **kwargs) -> list[float]:
    """
    Award 0.5 points per correct image citation in the completion for each completion.
    Modified to check the entire completion content (excluding <think> and reference section).
    """
    
    
    scores = []
    for i, (prompt, completion) in enumerate(zip(prompts, completions)):
        logger.info(f"\n\n### Scoring Image Bonus for Pair {i+1} ###")
        reference_section = extract_reference_section(prompt)
        parsed = parse_completion(completion, reference_section)
        if not parsed:
            logger.info(" from parse completion: Completion does not match the required format, score: 0.0")
            scores.append(0.0)
            continue
        
        think_content = parsed
        full_content = completion[0]['content']
        answer_content = re.sub(r'(?s)^<think>\n.*?\n</think>\s*', '', full_content)
        answer_content = re.sub(re.escape(reference_section) + r'$', '', answer_content)
        logger.info(f"Extracted answer content for bonus: {answer_content}")
        
        images = extract_images(prompt)
        cited_images_list = re.findall(r'!\[.*?\]\((.*?)\)', answer_content)
        correct_citations = [url for url in cited_images_list if url in images]
        
        logger.info(f"Cited images in answer: {cited_images_list}")
        logger.info(f"Correct image citations: {correct_citations}")
        
        score = 0.5 * len(correct_citations)
        logger.info(f"Image bonus score: {score}")
        scores.append(score)
    
    return scores

from datasets import load_dataset, Dataset
dataset = load_dataset("json", data_files="/root/autodl-tmp/markitdown/grpo_dataset.json",
                       split="train"
                      )

trainer = GRPOTrainer(
    model = model,
    processing_class = tokenizer,
    reward_funcs=[
        reference_score_func,     # 参考文献URL覆盖率
        think_content_score_func, # <think>内容合规性 
        answer_content_score_func,# 正文引用合规性
        image_bonus_score_func    # 图片引用加分项
    ],
    args = training_args,
    train_dataset = dataset,
    
)
trainer.train()

# Merge to 16bit
if True: model.save_pretrained_merged("model_3.15", tokenizer, save_method = "merged_16bit",)
if True: model.push_to_hub_merged("hf/model", tokenizer, save_method = "merged_16bit", token = "")

# Merge to 4bit
if False: model.save_pretrained_merged("model", tokenizer, save_method = "merged_4bit",)
if False: model.push_to_hub_merged("hf/model", tokenizer, save_method = "merged_4bit", token = "")

# Just LoRA adapters
if False: model.save_pretrained_merged("model", tokenizer, save_method = "lora",)
if False: model.push_to_hub_merged("hf/model", tokenizer, save_method = "lora", token = "")