package com.msb.hjycommunity.property;

import com.msb.hjycommunity.property.constant.SuggestState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 投诉状态机合法性矩阵测试（纯逻辑，不依赖 Spring 上下文）
 */
public class SuggestStateFlowTest {

    @Test
    public void 待受理可受理可直接关闭() {
        Assertions.assertTrue(SuggestState.canTransit("Pending", "Processing"));
        Assertions.assertTrue(SuggestState.canTransit("Pending", "Closed"));
    }

    @Test
    public void 处理中只能回复() {
        Assertions.assertTrue(SuggestState.canTransit("Processing", "Replied"));
        Assertions.assertFalse(SuggestState.canTransit("Processing", "Closed"));
    }

    @Test
    public void 已回复只能关闭_已关闭是终态() {
        Assertions.assertTrue(SuggestState.canTransit("Replied", "Closed"));
        Assertions.assertFalse(SuggestState.canTransit("Closed", "Processing"));
    }
}
