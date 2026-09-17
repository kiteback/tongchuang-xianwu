package com.tcxw.ai;

import com.tcxw.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
public class AiProductQueryService {

    private static final Logger log = LoggerFactory.getLogger(AiProductQueryService.class);

    private final AiChatGateway gateway;

    public AiProductQueryService(AiChatGateway gateway) {
        this.gateway = gateway;
    }

    public String query(String question) {
        try {
            String content = gateway.ask(question);
            if (content == null || content.isBlank()) {
                throw new BusinessException("AI服务未返回有效内容");
            }
            return content;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("AI product query failed", exception);
            throw new BusinessException("AI商品查询暂时不可用，请稍后重试");
        }
    }
}
