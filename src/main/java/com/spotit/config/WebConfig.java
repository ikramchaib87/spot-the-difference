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

// Configuration de la couche web : Thymeleaf, Controllers, ressources statiques
@Configuration

// Active Spring MVC : enregistre le DispatcherServlet, HandlerMapping, etc.
@EnableWebMvc

// Scanne tous les composants de l'application
@ComponentScan(basePackages = "com.spotit")

// Importe AppConfig pour que les beans BDD soient disponibles ici aussi
@Import(AppConfig.class)
public class WebConfig implements WebMvcConfigurer {

    // Indique à Thymeleaf où chercher les fichiers HTML
    // et avec quelle extension (.html)
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }

    // Le moteur Thymeleaf qui traite les expressions th:text, th:if, th:href...
    // et les remplace par les vraies valeurs envoyées depuis les Controllers
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    // Fait le lien entre Spring MVC et Thymeleaf
    // Quand un Controller retourne "menu", ce resolver cherche templates/menu.html
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        return resolver;
    }

    // Déclare les chemins des fichiers statiques accessibles depuis le navigateur
    // Sans cela, Tomcat bloquerait l'accès aux fichiers CSS, JS et images
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