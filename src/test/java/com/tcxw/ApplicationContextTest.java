package com.tcxw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.tcxw.repository.ProductDocumentRepository;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    @MockitoBean
    ProductDocumentRepository productDocumentRepository;

    @Test
    void contextLoadsWithAiDisabled() {
    }
}
