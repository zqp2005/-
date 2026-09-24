package com.msb.hjy.ai.config;

import com.msb.hjy.ai.client.HjyCommunityClient;
import com.msb.hjy.ai.tools.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TruthfulToolsTest {
    @Test void repairNumberResolvesToRecordIdAndRejectsPaths() {
        HjyCommunityClient client = mock(HjyCommunityClient.class);
        RepairTool tool = new RepairTool(); ReflectionTestUtils.setField(tool, "communityClient", client);
        when(client.get(eq("/system/repair/list"), anyMap())).thenReturn("{\"rows\":[{\"repairNum\":\"RX123\",\"repairId\":7}]}");
        when(client.get("/system/repair/7")).thenReturn("{\"data\":{\"repairNum\":\"RX123\",\"repairId\":7}}");
        assertTrue(tool.getRepairDetail("RX123").contains("RX123"));
        verify(client).get("/system/repair/7");
        reset(client);
        assertTrue(tool.getRepairDetail("../owner/1").contains("有效"));
        verifyNoInteractions(client);
    }
    @Test void communityUsesRealEndpointAndNeverFallsBackToDemoFacts() {
        HjyCommunityClient client = mock(HjyCommunityClient.class);
        CommunityTool tool = new CommunityTool(); ReflectionTestUtils.setField(tool, "communityClient", client);
        when(client.get(eq("/community/list"), anyMap())).thenReturn("{\"code\":200,\"total\":1,\"rows\":[{\"communityName\":\"测试小区\",\"communityDetailedAddress\":\"测试地址\"}]}");
        String result = tool.queryCommunityInfo("basic");
        assertTrue(result.contains("测试地址")); assertFalse(result.contains("1500")); assertFalse(result.contains("400-888"));
        when(client.get(eq("/community/list"), anyMap())).thenThrow(new SecurityException());
        assertTrue(tool.queryCommunityInfo("basic").contains("失败"));
        assertTrue(tool.getFacilities().contains("未接入"));
        assertTrue(tool.getAccessCardInfo("测试").contains("未接入"));
        assertFalse(tool.getConvenientServices("cleaning").contains("50元"));
    }

    @Test void announcementsUseRealTypePublishedFilterAndServerPagination() {
        HjyCommunityClient client = mock(HjyCommunityClient.class);
        AnnouncementTool tool = new AnnouncementTool(); ReflectionTestUtils.setField(tool, "communityClient", client);
        when(client.get(eq("/system/notice/list"), anyMap())).thenReturn("{\"code\":200,\"total\":1,\"rows\":[{\"noticeId\":1,\"noticeTitle\":\"真实通知\",\"noticeType\":\"1\",\"status\":\"0\"}]}");
        assertTrue(tool.queryAnnouncements("notice").contains("真实通知"));
        verify(client).get("/system/notice/list", Map.of("noticeType", "1", "status", "0", "pageNum", 1, "pageSize", 10));
        tool.getAnnouncementDetail("真实");
        verify(client).get("/system/notice/list", Map.of("noticeTitle", "真实", "status", "0", "pageNum", 1, "pageSize", 10));
        assertTrue(tool.getActivities("upcoming").contains("未接入"));
        assertTrue(tool.queryAnnouncements("activity").contains("未接入"));
    }

    @Test void ownerSearchIsFilteredAndDoesNotInventIdentityOrRelatedEntities() {
        HjyCommunityClient client = mock(HjyCommunityClient.class);
        OwnerInfoTool tool = new OwnerInfoTool(); ReflectionTestUtils.setField(tool, "communityClient", client);
        assertTrue(tool.queryOwnerInfo(null, null).contains("检索条件")); verifyNoInteractions(client);
        when(client.get(eq("/system/owner/list"), anyMap())).thenReturn("{\"code\":200,\"total\":1,\"rows\":[{\"ownerRealName\":\"测试\",\"ownerPhoneNumber\":\"13800000000\"}]}");
        String result = tool.queryOwnerInfo("测试", "13800000000");
        assertTrue(result.contains("138****0000")); assertFalse(result.contains("13800000000"));
        verify(client).get("/system/owner/list", Map.of("pageNum", 1, "pageSize", 5, "ownerRealName", "测试", "ownerPhoneNumber", "13800000000"));
        assertTrue(tool.getOwnerVehicles("测试").contains("未接入"));
        assertTrue(tool.getFamilyMembers("测试").contains("不等于"));
        assertTrue(tool.queryVisitors("测试", "2026-09-24").contains("不能"));
    }
}
