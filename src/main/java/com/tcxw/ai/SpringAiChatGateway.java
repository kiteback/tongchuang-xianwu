package com.tcxw.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
public class SpringAiChatGateway implements AiChatGateway {

    private final ChatClient chatClient;

    public SpringAiChatGateway(ChatClient.Builder builder, ProductTools productTools) {
        this.chatClient = builder
                .defaultSystem("""
                        你是同窗闲物的商品查询助手。
                        必须通过提供的工具查询平台商品，不能编造商品、价格、状态或商品ID。
                        搜索不到时明确告诉用户暂无符合条件的商品。
                        回答应简洁，并优先列出商品ID、标题和价格。
                        """)
                .defaultTools(productTools)
                .build();
    }

    @Override
    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
