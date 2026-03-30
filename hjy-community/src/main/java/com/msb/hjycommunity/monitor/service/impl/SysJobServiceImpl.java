package com.msb.hjycommunity.monitor.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysJob;
import com.msb.hjycommunity.monitor.mapper.SysJobMapper;
import com.msb.hjycommunity.monitor.service.SysJobService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务Service实现
 */
@Service
public class SysJobServiceImpl implements SysJobService {

    @Resource
    private SysJobMapper jobMapper;

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobMapper.selectJobList(job);
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        SysJob job = jobMapper.selectJobById(jobId);
        if (job != null && StringUtils.isNotEmpty(job.getCronExpression())) {
            job.setNextValidTime("");
        }
        return job;
    }

    @Override
    @Transactional
    public int insertJob(SysJob job) {
        job.setCreateBy(SecurityUtils.getUserName());
        return jobMapper.insertJob(job);
    }

    @Override
    @Transactional
    public int updateJob(SysJob job) {
        job.setUpdateBy(SecurityUtils.getUserName());
        return jobMapper.updateJob(job);
    }

    @Override
    @Transactional
    public int deleteJobById(Long jobId) {
        return jobMapper.deleteJobById(jobId);
    }

    @Override
    @Transactional
    public int deleteJobByIds(Long[] jobIds) {
        return jobMapper.deleteJobByIds(jobIds);
    }

    @Override
    @Transactional
    public int changeJobStatus(SysJob job) {
        job.setUpdateBy(SecurityUtils.getUserName());
        return jobMapper.updateJob(job);
    }

    @Override
    public int runJob(SysJob job) {
        return 1;
    }

    @Override
    public void export(SysJob job, javax.servlet.http.HttpServletResponse response) {
    }
}
