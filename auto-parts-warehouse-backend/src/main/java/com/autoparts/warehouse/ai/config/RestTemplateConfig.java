package com.autoparts.warehouse.ai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 专用 RestTemplate 配置
 * 配置连接和读取超时，防止 LLM 调用长时间阻塞
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    private AIConfigProperties aiConfig;

    /**
     * 创建用于 AI API 调用的 RestTemplate
     * 配置了连接超时和读取超时
     */
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiConfig.getTimeout().getConnect());
        factory.setReadTimeout(aiConfig.getTimeout().getRead());
        return new RestTemplate(factory);
    }
}
