package com.msb.hjycommunity.property;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.community.service.HjyCommunityService;
import com.msb.hjycommunity.property.service.HjyBuildingService;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import com.msb.hjycommunity.property.service.HjyRoomService;
import com.msb.hjycommunity.property.service.HjyUnitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 级联删除校验集成测试（需本地 MySQL/Redis 在线）
 *
 * 使用的数据为演示库真实数据，ID 硬编码如下（查询方式见各测试内注释）：
 * - 社区 1623660256618201090：下有 2 栋楼栋
 * - 楼栋 1623659483150794753：下有 1 个单元
 * - 单元 1623672195486367746：下有 2 个房间
 * - 房间 1338460226656833538：有 2 条有效业主绑定
 * - 业主 1339439232424800258：名下有 2 条有效房屋绑定
 */
@SpringBootTest
public class CascadeDeleteGuardTest {

    /** 演示库中真实存在的有下级数据的 ID（删除会被拒绝，数据不会被真正删掉） */
    private static final Long COMMUNITY_WITH_BUILDINGS = 1623660256618201090L;
    private static final Long BUILDING_WITH_UNITS = 1623659483150794753L;
    private static final Long UNIT_WITH_ROOMS = 1623672195486367746L;
    private static final Long ROOM_WITH_BINDINGS = 1338460226656833538L;
    private static final Long OWNER_WITH_BINDINGS = 1339439232424800258L;

    @Autowired
    private HjyCommunityService communityService;

    @Autowired
    private HjyBuildingService buildingService;

    @Autowired
    private HjyUnitService unitService;

    @Autowired
    private HjyRoomService roomService;

    @Autowired
    private HjyOwnerService ownerService;

    @Test
    public void 删除有楼栋的小区应被拒绝() {
        // SELECT c.community_id FROM hjy_community c JOIN hjy_building b ON c.community_id=b.community_id LIMIT 1
        CustomException e = Assertions.assertThrows(CustomException.class,
                () -> communityService.deleteHjyCommunity(new Long[]{COMMUNITY_WITH_BUILDINGS}));
        Assertions.assertTrue(e.getMsg().contains("无法删除"), e.getMsg());
    }

    @Test
    public void 删除有单元的楼栋应被拒绝() {
        // SELECT b.building_id FROM hjy_building b JOIN hjy_unit u ON b.building_id=u.building_id LIMIT 1
        CustomException e = Assertions.assertThrows(CustomException.class,
                () -> buildingService.deleteBuildingByIds(new Long[]{BUILDING_WITH_UNITS}));
        Assertions.assertTrue(e.getMsg().contains("无法删除"), e.getMsg());
    }

    @Test
    public void 删除有房间的单元应被拒绝() {
        // SELECT u.unit_id FROM hjy_unit u JOIN hjy_room r ON u.unit_id=r.unit_id LIMIT 1
        CustomException e = Assertions.assertThrows(CustomException.class,
                () -> unitService.deleteUnitByIds(new Long[]{UNIT_WITH_ROOMS}));
        Assertions.assertTrue(e.getMsg().contains("无法删除"), e.getMsg());
    }

    @Test
    public void 删除有绑定关系的房间应被拒绝() {
        // SELECT r.room_id FROM hjy_room r JOIN hjy_owner_room orr ON r.room_id=orr.room_id WHERE orr.room_status != 'Rejected' LIMIT 1
        CustomException e = Assertions.assertThrows(CustomException.class,
                () -> roomService.deleteRoomByIds(new Long[]{ROOM_WITH_BINDINGS}));
        Assertions.assertTrue(e.getMsg().contains("请先解绑"), e.getMsg());
    }

    @Test
    public void 删除有房屋绑定的业主应被拒绝() {
        // SELECT o.owner_id FROM hjy_owner o JOIN hjy_owner_room orr ON o.owner_id=orr.owner_id WHERE orr.room_status != 'Rejected' LIMIT 1
        CustomException e = Assertions.assertThrows(CustomException.class,
                () -> ownerService.deleteOwnerByIds(new Long[]{OWNER_WITH_BINDINGS}));
        Assertions.assertTrue(e.getMsg().contains("请先解绑"), e.getMsg());
    }
}
