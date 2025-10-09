package com.synergizglobal.dms.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig2 implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")           // Intercept all paths
                .excludePathPatterns("/error.html", "/**.css", "/**.js", "/**.xlsx", "/images/**"); // exclude static resources
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")  // all static resources
                .addResourceLocations("classpath:/static/") // or /public/
                .setCachePeriod(0)// disables caching
                .setCacheControl(CacheControl.noStore())
                .resourceChain(false);
    }
}