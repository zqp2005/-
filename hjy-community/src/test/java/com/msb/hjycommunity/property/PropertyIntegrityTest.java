package com.msb.hjycommunity.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.community.domain.HjyCommunity;
import com.msb.hjycommunity.community.mapper.HjyCommunityMapper;
import com.msb.hjycommunity.property.domain.*;
import com.msb.hjycommunity.property.mapper.*;
import com.msb.hjycommunity.property.service.PropertyHierarchyValidator;
import com.msb.hjycommunity.property.service.impl.*;
import com.msb.hjycommunity.system.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import java.io.InputStream;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 纯 Mock + 实际 MyBatis XML 解析，不触碰业务数据库。 */
class PropertyIntegrityTest {
    private final HjyOwnerRoomMapper bindings = mock(HjyOwnerRoomMapper.class);
    private final HjyRoomMapper rooms = mock(HjyRoomMapper.class);
    private final HjyOwnerRoomServiceImpl service = new HjyOwnerRoomServiceImpl();

    @BeforeEach void setup() {
        SysUser user = new SysUser(); user.setUserId(1L); user.setUserName("test");
        LoginUser principal = new LoginUser(); principal.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
        ReflectionTestUtils.setField(service, "ownerRoomMapper", bindings);
        ReflectionTestUtils.setField(service, "roomMapper", rooms);
    }
    @AfterEach void clean() { SecurityContextHolder.clearContext(); }

    private HjyOwnerRoom relation(String state) {
        HjyOwnerRoom value = new HjyOwnerRoom(); value.setOwnerRoomId(1L);
        value.setRoomId(2L); value.setOwnerId(3L); value.setRoomStatus(state); return value;
    }

    @Test void auditDoesNotChangeOccupancyAndRecordsOnlySuccessfulTransition() {
        when(bindings.selectOwnerRoomById(1L)).thenReturn(relation("Auditing"));
        when(bindings.updateOwnerRoom(any())).thenReturn(1);
        when(bindings.insertRecord(any())).thenReturn(1);
        assertEquals(1, service.auditOwnerRoom(1L, true, "已核实"));
        verify(bindings).updateOwnerRoom(argThat(v -> "Auditing".equals(v.getExpectedState()) && "Binding".equals(v.getRoomStatus())));
        verify(bindings).insertRecord(argThat(v -> "pass".equals(v.getRecordAuditType())));
        verifyNoInteractions(rooms);
    }

    @Test void concurrentAuditFailureCannotCreateHistory() {
        when(bindings.selectOwnerRoomById(1L)).thenReturn(relation("Auditing"));
        assertThrows(CustomException.class, () -> service.auditOwnerRoom(1L, false, "不通过"));
        verify(bindings, never()).insertRecord(any()); verifyNoInteractions(rooms);
    }

    @Test void unbindAndWithdrawLeaveHistoryWithoutChangingOccupancy() {
        for (String state : Arrays.asList("Binding", "Auditing")) {
            when(bindings.selectOwnerRoomById(1L)).thenReturn(relation(state));
            when(bindings.deleteOwnerRoomIfState(1L, state)).thenReturn(1);
            when(bindings.insertRecord(any())).thenReturn(1);
            assertEquals(1, service.deleteOwnerRoomByIds(new Long[]{1L, 1L}));
        }
        verify(bindings, times(2)).insertRecord(any()); verifyNoInteractions(rooms);
    }

    @Test void concurrentUnbindFailureCannotCreateHistory() {
        when(bindings.selectOwnerRoomById(1L)).thenReturn(relation("Binding"));
        assertThrows(CustomException.class, () -> service.deleteOwnerRoomById(1L));
        verify(bindings, never()).insertRecord(any());
    }

    @Test void ordinaryBindingEditCannotMoveHouseResidentOrVerifiedRole() {
        HjyOwnerRoom input = relation("Binding"); input.setCommunityId(9L);
        input.setBuildingId(9L); input.setUnitId(9L); input.setOwnerType("injected");
        service.updateOwnerRoom(input);
        verify(bindings).updateOwnerRoom(argThat(v -> v.getRoomId() == null && v.getOwnerId() == null
                && v.getCommunityId() == null && v.getBuildingId() == null && v.getUnitId() == null
                && v.getOwnerType() == null && v.getRoomStatus() == null));
    }

    @Test void hierarchyRejectsMissingParentsCrossCommunityAndMoves() {
        PropertyHierarchyValidator validator = new PropertyHierarchyValidator();
        HjyCommunityMapper communities = mock(HjyCommunityMapper.class);
        HjyBuildingMapper buildings = mock(HjyBuildingMapper.class);
        HjyUnitMapper units = mock(HjyUnitMapper.class);
        ReflectionTestUtils.setField(validator, "communityMapper", communities);
        ReflectionTestUtils.setField(validator, "buildingMapper", buildings);
        ReflectionTestUtils.setField(validator, "unitMapper", units);
        when(communities.selectById(1L)).thenReturn(new HjyCommunity());
        HjyBuilding building = new HjyBuilding(); building.setBuildingId(2L); building.setCommunityId(1L);
        when(buildings.selectBuildingById(2L)).thenReturn(building);
        HjyUnit unit = new HjyUnit(); unit.setUnitId(3L); unit.setBuildingId(2L); unit.setCommunityId(1L);
        when(units.selectUnitById(3L)).thenReturn(unit);
        HjyRoom room = new HjyRoom(); room.setUnitId(3L);
        validator.room(room, null);
        assertEquals(Long.valueOf(1), room.getCommunityId()); assertEquals(Long.valueOf(2), room.getBuildingId());
        room.setCommunityId(9L); assertThrows(CustomException.class, () -> validator.room(room, null));
        room.setCommunityId(null); room.setUnitId(99L); assertThrows(CustomException.class, () -> validator.room(room, null));
        HjyUnit moved = new HjyUnit(); moved.setBuildingId(99L);
        assertThrows(CustomException.class, () -> validator.unit(moved, unit));
    }

    @Test void repairActionsRejectConcurrentStateChangeAndRequireReasons() {
        HjyRepairMapper mapper = mock(HjyRepairMapper.class);
        HjyRepairServiceImpl repairs = new HjyRepairServiceImpl(); ReflectionTestUtils.setField(repairs, "repairMapper", mapper);
        HjyRepair pending = new HjyRepair(); pending.setRepairId(1L); pending.setRepairState("Pending");
        when(mapper.selectRepairById(1L)).thenReturn(pending);
        assertThrows(CustomException.class, () -> repairs.cancelRepair(1L, " "));
        assertThrows(CustomException.class, () -> repairs.rejectRepair(1L, null));
        assertThrows(CustomException.class, () -> repairs.cancelRepair(1L, "不再需要"));
        verify(mapper).updateRepair(argThat(v -> "Pending".equals(v.getExpectedState())));
    }

    @Test void onlyAssignedWorkerCanReceiveOrCompleteEvenForAdministrator() {
        HjyRepairMapper mapper = mock(HjyRepairMapper.class);
        HjyRepairServiceImpl repairs = new HjyRepairServiceImpl(); ReflectionTestUtils.setField(repairs, "repairMapper", mapper);
        HjyRepair order = new HjyRepair(); order.setAssignmentId(9L); order.setRepairState("Allocated");
        when(mapper.selectRepairById(1L)).thenReturn(order);
        assertThrows(CustomException.class, () -> repairs.receiveRepair(1L));
        order.setRepairState("Processing");
        assertThrows(CustomException.class, () -> repairs.completeRepair(1L));
        verify(mapper, never()).updateRepair(any());
        order.setAssignmentId(1L); order.setRepairState("Allocated");
        when(mapper.updateRepair(any())).thenReturn(1);
        assertEquals(1, repairs.receiveRepair(1L));
    }

    @Test void dispatchRequiresEnabledWorkerWithBothActionPermissions() {
        HjyRepairMapper mapper = mock(HjyRepairMapper.class);
        com.msb.hjycommunity.system.mapper.SysUserMapper users = mock(com.msb.hjycommunity.system.mapper.SysUserMapper.class);
        com.msb.hjycommunity.framework.service.SysPermissionService permissions = mock(com.msb.hjycommunity.framework.service.SysPermissionService.class);
        HjyRepairServiceImpl repairs = new HjyRepairServiceImpl();
        ReflectionTestUtils.setField(repairs, "repairMapper", mapper);
        ReflectionTestUtils.setField(repairs, "userMapper", users);
        ReflectionTestUtils.setField(repairs, "permissionService", permissions);
        assertThrows(CustomException.class, () -> repairs.assignRepair(1L, null));
        SysUser worker = new SysUser(); worker.setUserId(2L); worker.setStatus("1"); worker.setDelFlag("0");
        when(users.selectUserById(2L)).thenReturn(worker);
        assertThrows(CustomException.class, () -> repairs.assignRepair(1L, 2L));
        worker.setStatus("0"); when(permissions.getMenuPermission(worker)).thenReturn(Collections.emptySet());
        assertThrows(CustomException.class, () -> repairs.assignRepair(1L, 2L));
        verify(mapper, never()).updateRepair(any());
        when(permissions.getMenuPermission(worker)).thenReturn(new HashSet<>(Arrays.asList("system:repair:receive", "system:repair:complete")));
        HjyRepair order = new HjyRepair(); order.setRepairState("Pending"); when(mapper.selectRepairById(1L)).thenReturn(order);
        when(mapper.updateRepair(any())).thenReturn(1);
        assertEquals(1, repairs.assignRepair(1L, 2L));
    }

    @Test void complaintActionsRejectConcurrentStateChange() {
        HjySuggestMapper mapper = mock(HjySuggestMapper.class);
        HjySuggestServiceImpl suggestions = new HjySuggestServiceImpl(); ReflectionTestUtils.setField(suggestions, "suggestMapper", mapper);
        HjySuggest pending = new HjySuggest(); pending.setComplaintState("Pending");
        when(mapper.selectSuggestById(1L)).thenReturn(pending);
        assertThrows(CustomException.class, () -> suggestions.acceptSuggest(1L));
        verify(mapper).updateSuggest(argThat(v -> "Pending".equals(v.getExpectedState())));
    }

    @Test void generatedRepairNumbersDifferAndFlowFieldsCannotBeInjected() {
        HjyRepairMapper mapper = mock(HjyRepairMapper.class);
        HjyRepairServiceImpl repairs = new HjyRepairServiceImpl(); ReflectionTestUtils.setField(repairs, "repairMapper", mapper);
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            HjyRepair input = new HjyRepair(); input.setRepairNum("same"); input.setAssignmentId(9L); input.setCompleteTime(new Date());
            repairs.insertRepair(input); assertTrue(numbers.add(input.getRepairNum()));
            assertNull(input.getAssignmentId()); assertNull(input.getCompleteTime());
        }
    }

    @Test void realMapperSqlIncludesExpectedStateAndJsonCannotSupplyIt() throws Exception {
        for (Class<?> type : Arrays.asList(HjyRepair.class, HjySuggest.class, HjyOwnerRoom.class)) {
            Object input = new ObjectMapper().readValue("{\"expectedState\":\"injected\"}", type);
            assertNull(type.getMethod("getExpectedState").invoke(input));
            type.getMethod("setExpectedState", String.class).invoke(input, "Pending");
            Configuration config = new Configuration();
            config.getTypeAliasRegistry().registerAliases("com.msb.hjycommunity.property.domain");
            String resource = "mapper/property/" + type.getSimpleName() + "Mapper.xml";
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
                new XMLMapperBuilder(stream, config, resource, config.getSqlFragments()).parse();
            }
            String name = type.getSimpleName().substring(3);
            String sql = config.getMappedStatement("com.msb.hjycommunity.property.mapper." + type.getSimpleName() + "Mapper.update" + name)
                    .getBoundSql(input).getSql().replaceAll("\\s+", " ");
            String stateColumn = type == HjyRepair.class ? "repair_state" : type == HjySuggest.class ? "complaint_state" : "room_status";
            assertTrue(sql.contains("AND " + stateColumn + " = ?"), sql);
        }
    }
}
