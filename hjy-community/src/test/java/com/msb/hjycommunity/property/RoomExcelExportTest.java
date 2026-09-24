package com.msb.hjycommunity.property;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.property.domain.dto.HjyRoomExcelDto;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 验证真实 Excel 导出结果，不连接数据库或写出文件。 */
class RoomExcelExportTest {
    @Test
    void preservesMissingAndUnknownStatuses() {
        HjyRoomExcelDto row = new HjyRoomExcelDto();
        assertEquals(null, row.getRoomStatus());
        row.setRoomStatus("future_state");
        assertEquals("future_state", row.getRoomStatus());
    }

    @Test
    void exportsAllFourRoomStatusLabels() throws Exception {
        List<HjyRoomExcelDto> rows = new ArrayList<>();
        for (String status : Arrays.asList("has_stay", "none_stay", "none", "has_give")) {
            HjyRoomExcelDto row = new HjyRoomExcelDto();
            row.setRoomStatus(status);
            rows.add(row);
        }
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("房间信息", "房间"), HjyRoomExcelDto.class, rows)) {
            List<String> labels = new ArrayList<>();
            for (Row row : workbook.getSheetAt(0)) {
                Cell cell = row.getCell(4);
                if (cell != null) {
                    String value = new DataFormatter().formatCellValue(cell);
                    if (!value.isEmpty() && !"房屋状态".equals(value)) {
                        labels.add(value);
                    }
                }
            }
            assertEquals(Arrays.asList("已入住", "未入住", "未出售", "已交房"), labels);
        }
    }
}
