package com.msb.hjycommunity.monitor.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysOperlog;
import com.msb.hjycommunity.monitor.mapper.SysOperlogMapper;
import com.msb.hjycommunity.monitor.service.SysOperlogService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 操作日志Service实现
 */
@Service
public class SysOperlogServiceImpl implements SysOperlogService {

    @Resource
    private SysOperlogMapper operlogMapper;

    @Override
    public List<SysOperlog> selectOperlogList(SysOperlog operlog) {
        return operlogMapper.selectOperlogList(operlog);
    }

    @Override
    @Transactional
    public int deleteOperlogById(Long operId) {
        return operlogMapper.deleteOperlogById(operId);
    }

    @Override
    @Transactional
    public int cleanOperlog() {
        return operlogMapper.cleanOperlog();
    }

    @Override
    public void export(SysOperlog operlog, HttpServletResponse response) {
        List<SysOperlog> list = operlogMapper.selectOperlogList(operlog);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("操作日志", "操作日志"),
                SysOperlog.class, list);
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            String fileName = URLEncoder.encode("操作日志", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void insertOperlog(SysOperlog operlog) {
        operlogMapper.insert(operlog);
    }
}
