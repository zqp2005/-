package com.msb.hjycommunity.security;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

/** 实际解析全部 XML：检测重复 statement、类型别名错误及权限过滤退化。 */
class MapperConfigurationTest {
    @Test void allMappersParseAndPermissionQueriesRejectDisabledRoles() throws Exception {
        Configuration config = new Configuration();
        config.getTypeAliasRegistry().registerAliases("com.msb.hjycommunity");
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml");
        assertTrue(resources.length > 10);
        for (Resource resource : resources) {
            try (InputStream stream = resource.getInputStream()) {
                new XMLMapperBuilder(stream, config, resource.toString(), config.getSqlFragments()).parse();
            }
        }
        for (String name : new String[]{"SysRoleMapper.selectRolePermissionByUserId",
                "SysMenuMapper.selectMenuPermissionByUserId", "SysMenuMapper.selectMenuPermsByUserId"}) {
            String sql = config.getMappedStatement("com.msb.hjycommunity.system.mapper." + name)
                    .getBoundSql(2L).getSql().replaceAll("\\s+", " ");
            assertTrue(sql.contains("status = '0'"), sql);
            assertTrue(sql.contains("del_flag = '0'"), sql);
        }
    }

    @Test void unimplementedSchedulerCannotReportSuccess() {
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class,
                () -> new com.msb.hjycommunity.monitor.service.impl.SysJobServiceImpl().runJob(null));
    }
}
