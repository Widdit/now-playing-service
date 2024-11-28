package com.widdit.nowplaying.config;

import com.widdit.nowplaying.json.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 配置类，注册 web 层相关组件
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 扩展 Spring MVC 的消息转换器
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 为消息转换器设置一个对象转换器，把 Java 对象序列化为 JSON 形式
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自己的消息转换器加入到容器中，并放到首位
        converters.add(0, converter);
    }

    /**
     * 支持跨域资源共享
     */
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 开启静态资源访问
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

}
