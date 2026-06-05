package com.hjc.backend.service.impl;

import com.hjc.backend.dto.MaterialApplicationExportRequest;
import com.hjc.backend.dto.ScoreSummaryPageRequest;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.service.ExcelExportService;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.OperationLogService;
import com.hjc.backend.service.ScoreSummaryService;
import com.hjc.backend.vo.MaterialApplicationExportVO;
import com.hjc.backend.vo.ScoreSummaryVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScoreSummaryService scoreSummaryService;

    private final MaterialApplicationService materialApplicationService;

    private final OperationLogService operationLogService;

    @Override
    public byte[] exportScoreSummaries(ScoreSummaryPageRequest request) {
        List<ScoreSummaryVO> records = scoreSummaryService.listScoreSummariesForExport(request);
        List<String> headers = List.of("学号", "姓名", "专业", "班级", "总分", "班级排名", "专业排名", "计算时间", "状态");
        List<List<Object>> rows = records.stream()
                .map(record -> List.<Object>of(
                        value(record.getStudentNo()),
                        value(record.getStudentName()),
                        value(record.getMajorName()),
                        value(record.getClassName()),
                        value(record.getTotalScore()),
                        value(record.getClassRank()),
                        value(record.getMajorRank()),
                        value(record.getCalculateTime()),
                        value(record.getStatus())))
                .toList();
        byte[] content = buildWorkbook("成绩汇总", headers, rows);
        operationLogService.recordSuccess("EXPORT", "SCORE_SUMMARY", "Export score summaries", "recordCount=" + records.size());
        return content;
    }

    @Override
    public byte[] exportClassRanking(Long classId) {
        List<ScoreSummaryVO> records = scoreSummaryService.listClassRanking(classId);
        byte[] content = exportRanking("班级排名", records, true);
        operationLogService.recordSuccess("EXPORT", "CLASS_RANKING", "Export class ranking", "classId=" + classId + ",recordCount=" + records.size());
        return content;
    }

    @Override
    public byte[] exportMajorRanking(Long majorId) {
        List<ScoreSummaryVO> records = scoreSummaryService.listMajorRanking(majorId);
        byte[] content = exportRanking("专业排名", records, false);
        operationLogService.recordSuccess("EXPORT", "MAJOR_RANKING", "Export major ranking", "majorId=" + majorId + ",recordCount=" + records.size());
        return content;
    }

    @Override
    public byte[] exportMaterialApplications(MaterialApplicationExportRequest request) {
        List<MaterialApplicationExportVO> records = materialApplicationService.listMaterialApplicationsForExport(request);
        List<String> headers = List.of("材料ID", "标题", "学号", "学生姓名", "专业", "班级", "综测分类", "综测项目",
                "申请分", "认定分", "状态", "附件数量", "提交时间", "审核时间", "驳回原因");
        List<List<Object>> rows = records.stream()
                .map(record -> List.<Object>of(
                        value(record.getId()),
                        value(record.getTitle()),
                        value(record.getStudentNo()),
                        value(record.getStudentName()),
                        value(record.getMajorName()),
                        value(record.getClassName()),
                        value(record.getCategoryName()),
                        value(record.getItemName()),
                        value(record.getApplyScore()),
                        value(record.getFinalScore()),
                        value(record.getStatus()),
                        value(record.getAttachmentCount()),
                        value(record.getSubmitTime()),
                        value(record.getReviewTime()),
                        value(record.getRejectReason())))
                .toList();
        byte[] content = buildWorkbook("材料申报明细", headers, rows);
        operationLogService.recordSuccess("EXPORT", "MATERIAL_APPLICATION", "Export material applications", "recordCount=" + records.size());
        return content;
    }

    private byte[] exportRanking(String sheetName, List<ScoreSummaryVO> records, boolean classRanking) {
        List<String> headers = List.of("排名", "学号", "姓名", "专业", "班级", "总分", "计算时间");
        List<List<Object>> rows = records.stream()
                .map(record -> List.<Object>of(
                        value(classRanking ? record.getClassRank() : record.getMajorRank()),
                        value(record.getStudentNo()),
                        value(record.getStudentName()),
                        value(record.getMajorName()),
                        value(record.getClassName()),
                        value(record.getTotalScore()),
                        value(record.getCalculateTime())))
                .toList();
        return buildWorkbook(sheetName, headers, rows);
    }

    private byte[] buildWorkbook(String sheetName, List<String> headers, List<List<Object>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<Object> values = rows.get(rowIndex);
                for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                    row.createCell(columnIndex).setCellValue(formatCellValue(values.get(columnIndex)));
                }
            }
            for (int i = 0; i < headers.size(); i++) {
                sheet.setColumnWidth(i, Math.min(30, Math.max(headers.get(i).length() + 8, 12)) * 256);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Excel export failed");
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private Object value(Object value) {
        return value == null ? "" : value;
    }

    private String formatCellValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.format(DATE_TIME_FORMATTER);
        }
        return String.valueOf(value);
    }
}
