package com.msb.hjycommunity.web.controller.system;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.system.domain.SysDictData;
import com.msb.hjycommunity.system.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {
@Autowired
    private SysDictDataService dictDataService;
@RequestMapping("/list")
    public PageResult list(SysDictData dictData)
{
    startPage();
    List<SysDictData> list=dictDataService.selectDictDataList(dictData);
    return getData(list);
}
/**
 * 根据Id查询字典数据信息
 */
@GetMapping(value="/{dictCode}")
public BaseResponse getInfo(@PathVariable Long dictCode)
    {
        SysDictData dictData = dictDataService.selectDictDataById(dictCode);
        return BaseResponse.success(dictData);
    }
    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value="/type/{dictType}")
    public BaseResponse dictType(@PathVariable String dictType)
        {

            return BaseResponse.success(dictDataService.selectDictDataByType(dictType));
        }
        /**
         * 新增字典数据信息
         */
        @PostMapping
    public BaseResponse add(@RequestBody SysDictData sysDictData)
        {
            sysDictData.setCreateBy(SecurityUtils.getUserName ());
            return toAjax(dictDataService.insertDictData(sysDictData));
        }
        /**
         * 修改字典数据信息
         */
        @PutMapping
        public BaseResponse edit(@RequestBody SysDictData sysDictData)
            {
            sysDictData.setUpdateBy(SecurityUtils.getUserName ());
            return toAjax(dictDataService.updateDictData(sysDictData));
        }
        /**
         * 删除字典数据信息
         */
        @DeleteMapping("/{dictCodes}")
        public BaseResponse remove(@PathVariable Long[] dictCodes)
            {
                return toAjax(dictDataService.deleteDictDataByIds(dictCodes));
            }
}
