package com.msb.hjycommunity.monitor.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysLogininfor;
import com.msb.hjycommunity.monitor.mapper.SysLogininforMapper;
import com.msb.hjycommunity.monitor.service.SysLogininforService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 登录日志Service实现
 */
@Service
public class SysLogininforServiceImpl implements SysLogininforService {

    @Resource
    private SysLogininforMapper logininforMapper;

    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor) {
        return logininforMapper.selectLogininforList(logininfor);
    }

    @Override
    @Transactional
    public int deleteLogininforById(Long infoId) {
        return logininforMapper.deleteLogininforById(infoId);
    }

    @Override
    @Transactional
    public int cleanLogininfor() {
        return logininforMapper.cleanLogininfor();
    }

    @Override
    public void export(SysLogininfor logininfor, HttpServletResponse response) {
        List<SysLogininfor> list = logininforMapper.selectLogininforList(logininfor);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("登录日志", "登录日志"),
                SysLogininfor.class, list);
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            String fileName = URLEncoder.encode("登录日志", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void insertLogininfor(SysLogininfor logininfor) {
        logininforMapper.insert(logininfor);
    }
}
