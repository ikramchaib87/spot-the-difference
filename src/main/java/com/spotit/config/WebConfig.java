package com.spotit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

// ✅ Configuration Spring MVC
@Configuration
@EnableWebMvc  // ← Active Spring MVC : DispatcherServlet, HandlerMapping, etc.

// ✅ Scanne UNIQUEMENT les controllers
@ComponentScan(basePackages = "com.spotit")
@Import(AppConfig.class)
public class WebConfig implements WebMvcConfigurer {

    // ─── THYMELEAF ────────────────────────────────────────

    // ✅ Résolveur de templates : cherche dans /WEB-INF/templates/
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");   // ← dossier des HTML
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // false en dev, true en production
        return resolver;
    }

    // ✅ Moteur de templates Thymeleaf
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    // ✅ Vue Resolver : lie Spring MVC avec Thymeleaf
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        return resolver;
    }

    // ─── RESSOURCES STATIQUES ─────────────────────────────

    // ✅ Sert les fichiers CSS, JS, images depuis /static/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/style.css")
                .addResourceLocations("classpath:/static/style.css");
        registry.addResourceHandler("/game.js")
                .addResourceLocations("classpath:/static/game.js");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}