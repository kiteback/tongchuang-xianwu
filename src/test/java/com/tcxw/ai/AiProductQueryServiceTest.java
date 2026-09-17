package com.tcxw.ai;

import com.tcxw.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiProductQueryServiceTest {

    @Test
    void returnsModelAnswer() {
        AiChatGateway gateway = mock(AiChatGateway.class);
        when(gateway.ask("找一本Java教材")).thenReturn("找到商品9：Java教材，20元");

        AiProductQueryService service = new AiProductQueryService(gateway);

        assertEquals("找到商品9：Java教材，20元", service.query("找一本Java教材"));
    }

    @Test
    void convertsModelFailureToBusinessError() {
        AiChatGateway gateway = mock(AiChatGateway.class);
        when(gateway.ask("找教材")).thenThrow(new RuntimeException("provider unavailable"));

        AiProductQueryService service = new AiProductQueryService(gateway);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.query("找教材"));

        assertEquals("AI商品查询暂时不可用，请稍后重试", exception.getMessage());
    }

    @Test
    void rejectsEmptyModelAnswer() {
        AiChatGateway gateway = mock(AiChatGateway.class);
        when(gateway.ask("找教材")).thenReturn(" ");

        AiProductQueryService service = new AiProductQueryService(gateway);

        assertThrows(BusinessException.class, () -> service.query("找教材"));
    }
}
