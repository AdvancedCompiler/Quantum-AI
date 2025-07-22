import logging
import time
from pathlib import Path
from typing import Iterable
from docling_core.types.doc import ImageRefMode, PictureItem, TableItem
from docling.datamodel.base_models import ConversionStatus, InputFormat
from docling.datamodel.document import ConversionResult
from docling.datamodel.pipeline_options import PdfPipelineOptions
from docling.document_converter import DocumentConverter, PdfFormatOption

_log = logging.getLogger(__name__)
IMAGE_RESOLUTION_SCALE = 2.0

class FormulaUnderstandingPipelineOptions(PdfPipelineOptions):
    do_formula_understanding: bool = True

def export_document_images(
    conv_res: ConversionResult,
    base_output_dir: Path,
):
    """为单个文档导出图片到独立的子目录"""
    if conv_res.status != ConversionStatus.SUCCESS:
        _log.info(f"Document {conv_res.input.file} failed to convert, skipping image export.")
        return

    doc_filename = conv_res.input.file.stem
    # 为每个 PDF 创建独立的图片输出目录
    output_dir = base_output_dir / f"{doc_filename}_images"
    output_dir.mkdir(parents=True, exist_ok=True)

    # 保存页面图片，为节省内存，不需要可以注释掉
    for page_no, page in conv_res.document.pages.items():
        page_image_filename = output_dir / f"page-{page.page_no}.png"
        with page_image_filename.open("wb") as fp:
            page.image.pil_image.save(fp, format="PNG")

    # 保存表格和图片元素
    table_counter = 0
    picture_counter = 0
    for element, _level in conv_res.document.iterate_items():
        if isinstance(element, TableItem):
            table_counter += 1
            element_image_filename = output_dir / f"table-{table_counter}.png"
            with element_image_filename.open("wb") as fp:
                element.get_image(conv_res.document).save(fp, "PNG")
        elif isinstance(element, PictureItem):
            picture_counter += 1
            element_image_filename = output_dir / f"picture-{picture_counter}.png"
            with element_image_filename.open("wb") as fp:
                element.get_image(conv_res.document).save(fp, "PNG")

    # 保存 Markdown 文件（带图片引用）
    md_filename = output_dir / f"{doc_filename}-with-image-refs.md"
    conv_res.document.save_as_markdown(md_filename, image_mode=ImageRefMode.REFERENCED)

def process_all_pdfs(input_dir: Path, output_dir: Path):
    """处理指定目录下的所有 PDF 文件，跳过已处理的 PDF"""
    logging.basicConfig(level=logging.INFO)

    # 获取所有 PDF 文件
    input_doc_paths = list(input_dir.glob("*.pdf"))
    if not input_doc_paths:
        _log.info(f"No PDF files found in {input_dir}")
        return

    pipeline_options = FormulaUnderstandingPipelineOptions()
    pipeline_options.do_formula_enrichment = True
    pipeline_options.images_scale = IMAGE_RESOLUTION_SCALE
    pipeline_options.generate_page_images = True
    pipeline_options.generate_picture_images = True
    artifacts_path = "/root/autodl-tmp/docling_models"

    doc_converter = DocumentConverter(
        format_options={
            InputFormat.PDF: PdfFormatOption(pipeline_options=pipeline_options,
                                             artifacts_path=artifacts_path)
        }
    )

    start_time = time.time()
    success_count = 0
    failure_count = 0
    skipped_count = 0

    for pdf_path in input_doc_paths:
        doc_filename = pdf_path.stem
        output_subdir = output_dir / f"{doc_filename}_images"
        
        # 检查是否已经处理过（输出目录已存在）
        if output_subdir.exists():
            _log.info(f"Skipping {pdf_path} - already processed (output directory exists)")
            skipped_count += 1
            continue

        _log.info(f"Processing {pdf_path}")
        try:
            conv_res = doc_converter.convert(pdf_path, max_num_pages=2000)
            if conv_res.status == ConversionStatus.SUCCESS:
                export_document_images(conv_res, output_dir)
                success_count += 1
            else:
                failure_count += 1
                _log.info(f"Failed to process {pdf_path}")
        except Exception as e:
            failure_count += 1
            _log.error(f"Error processing {pdf_path}: {str(e)}")

    end_time = time.time() - start_time
    _log.info(f"Processed {len(input_doc_paths)} PDFs in {end_time:.2f} seconds.")
    _log.info(f"Success: {success_count}, Failures: {failure_count}, Skipped: {skipped_count}")

def main():
    input_dir = Path("/root/autodl-tmp/markitdown/quantum_paper_dlc_2")
    output_dir = Path("scratch_dlc_2")
    output_dir.mkdir(parents=True, exist_ok=True)
    process_all_pdfs(input_dir, output_dir)

if __name__ == "__main__":
    main()