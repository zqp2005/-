package com.msb.hjycommunity.property;

import com.msb.hjycommunity.property.constant.RepairState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 报修状态机合法性矩阵测试（纯逻辑，不依赖 Spring 上下文）
 */
public class RepairStateFlowTest {

    @Test
    public void 待处理可派单() {
        Assertions.assertTrue(RepairState.canTransit("Pending", "Allocated"));
    }

    @Test
    public void 待处理可取消可拒绝() {
        Assertions.assertTrue(RepairState.canTransit("Pending", "Cancelled"));
        Assertions.assertTrue(RepairState.canTransit("Pending", "No_Processed"));
    }

    @Test
    public void 已分派可接单可取消但不能拒绝() {
        Assertions.assertTrue(RepairState.canTransit("Allocated", "Processing"));
        Assertions.assertTrue(RepairState.canTransit("Allocated", "Cancelled"));
        Assertions.assertFalse(RepairState.canTransit("Allocated", "No_Processed"));
    }

    @Test
    public void 处理中只能完成() {
        Assertions.assertTrue(RepairState.canTransit("Processing", "Processed"));
        Assertions.assertFalse(RepairState.canTransit("Processing", "Cancelled"));
        Assertions.assertFalse(RepairState.canTransit("Processing", "Allocated"));
    }

    @Test
    public void 终态不能再流转() {
        Assertions.assertFalse(RepairState.canTransit("Processed", "Processing"));
        Assertions.assertFalse(RepairState.canTransit("No_Processed", "Allocated"));
        Assertions.assertFalse(RepairState.canTransit("Cancelled", "Allocated"));
    }
}
