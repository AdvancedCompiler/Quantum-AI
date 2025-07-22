from unsloth import FastLanguageModel,standardize_sharegpt
import torch
from trl import SFTTrainer, SFTConfig
from datasets import load_dataset



import os


model_path = "/root/autodl-tmp/markitdown/sft_output_4_1"  # 用户需替换为实际模型路径
dataset_path = "/root/autodl-tmp/quantum4_9.json"  # 用户需替换为实际数据集路径
output_dir = "/root/autodl-tmp/markitdown/sft_output_4_9"



max_seq_length = 3000 # Supports RoPE Scaling interally, so choose any!

model, tokenizer = FastLanguageModel.from_pretrained(
    model_name = model_path,
    max_seq_length = max_seq_length, # Choose any for long context!
    load_in_4bit = False,  # 4 bit quantization to reduce memory
    load_in_8bit = True, # [NEW!] A bit more accurate, uses 2x memory
    full_finetuning = False, # [NEW!] We have full finetuning now!
    fast_inference=True,
    gpu_memory_utilization = 0.7, # Reduce if out of memory

    # token = "hf_...", # use one if using gated models
)

EOS_TOKEN = tokenizer.eos_token # Must add EOS_TOKEN
alpaca_prompt = """Below is an instruction that describes a task, paired with an input that provides further context. Write a response that appropriately completes the request.

### Instruction:
{}

### Input:
{}

### Response:
{}"""
def formatting_prompts_func(examples):
    instructions = examples["instruction"]
    inputs       = examples["input"]
    outputs      = examples["output"]
    texts = []
    for instruction, input, output in zip(instructions, inputs, outputs):
        # Must add EOS_TOKEN, otherwise your generation will go on forever!
        text = alpaca_prompt.format(instruction, input, output) + EOS_TOKEN
        texts.append(text)
    return { "text" : texts, }

# Get LAION dataset
dataset = load_dataset("json", data_files=dataset_path,split="train")
dataset = dataset.map(formatting_prompts_func, batched = True,)


# Do model patching and add fast LoRA weights
model = FastLanguageModel.get_peft_model(
    model,
    r = 16,
    target_modules = ["q_proj", "k_proj", "v_proj", "o_proj",
                      "gate_proj", "up_proj", "down_proj",],
    lora_alpha = 16,
    lora_dropout = 0, # Supports any, but = 0 is optimized
    bias = "none",    # Supports any, but = "none" is optimized
    # [NEW] "unsloth" uses 30% less VRAM, fits 2x larger batch sizes!
    use_gradient_checkpointing = "unsloth", # True or "unsloth" for very long context
    random_state = 3407,
    max_seq_length = max_seq_length,
    use_rslora = False,  # We support rank stabilized LoRA
    loftq_config = None, # And LoftQ
)

trainer = SFTTrainer(
    model = model,
    train_dataset = dataset,
    # eval_dataset = eval_dataset,
    tokenizer = tokenizer,
    # formatting_func=formatting_func,
    args = SFTConfig(
        dataset_text_field = "text",
        max_seq_length = max_seq_length,
        per_device_train_batch_size = 4,
        gradient_accumulation_steps = 4,
        warmup_steps = 10,
        num_train_epochs=3,
        logging_steps = 1,
        output_dir = output_dir+'/adapter',
        optim = "adamw_8bit",
        seed = 3407,
        report_to="tensorboard",
        logging_dir="sft_logs"
    ),
    )



trainer.train()


# 保存adapter
trainer.save_model()

model.save_pretrained_merged(output_dir, tokenizer, save_method = "merged_16bit",)
