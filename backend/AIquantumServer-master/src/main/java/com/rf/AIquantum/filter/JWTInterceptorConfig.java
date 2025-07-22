package com.rf.AIquantum.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lpf
 * @description:
 * @date 2021/12/2822:06
 */
@Configuration
public class JWTInterceptorConfig implements WebMvcConfigurer {

    /*@Autowired
    private UserCheckInterceptor userCheckInterceptor;

    @Autowired
    private JWTInterceptor jwtInterceptor;*/


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] swaggerExcludes = new String[]{"/swagger-ui.html", "/swagger-resources/**", "/csrf", "/webjars/**"};
        String[] systemApi = new String[]{"/user/addUser","/user/login","/user/show","/user/temp/login"};
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/index**", "/error")
                .excludePathPatterns(systemApi)
                .excludePathPatterns(swaggerExcludes)
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/druid/**")
                .excludePathPatterns("/static/**");
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/index.html", "/error")
                .excludePathPatterns(systemApi)
                .excludePathPatterns(swaggerExcludes)
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/druid/**")
                .excludePathPatterns("/static/**");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
