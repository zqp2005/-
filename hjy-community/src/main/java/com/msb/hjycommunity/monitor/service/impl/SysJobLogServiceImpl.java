package com.msb.hjycommunity.monitor.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.monitor.domain.SysJobLog;
import com.msb.hjycommunity.monitor.mapper.SysJobLogMapper;
import com.msb.hjycommunity.monitor.service.SysJobLogService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 调度日志Service实现
 */
@Service
public class SysJobLogServiceImpl implements SysJobLogService {

    @Resource
    private SysJobLogMapper jobLogMapper;

    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    @Override
    @Transactional
    public int deleteJobLogById(Long jobLogId) {
        return jobLogMapper.deleteJobLogById(jobLogId);
    }

    @Override
    @Transactional
    public int cleanJobLog() {
        return jobLogMapper.cleanJobLog();
    }

    @Override
    public void export(SysJobLog jobLog, HttpServletResponse response) {
        List<SysJobLog> list = jobLogMapper.selectJobLogList(jobLog);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("调度日志", "调度日志"),
                SysJobLog.class, list);
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            String fileName = URLEncoder.encode("调度日志", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
