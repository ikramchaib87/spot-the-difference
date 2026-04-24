package com.spotit.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

// ✅ Cette classe remplace web.xml
// Spring la détecte automatiquement au démarrage de Tomcat
// grâce à l'interface ServletContainerInitializer
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // ✅ Configuration racine : BDD, Service, Repository
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{ AppConfig.class };
    }

    // ✅ Configuration Web : MVC, Thymeleaf, Controllers
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{ WebConfig.class };
    }

    // ✅ Le DispatcherServlet intercepte TOUTES les URLs
    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }

    // ✅ Encodage UTF-8 pour les formulaires
    @Override
    protected jakarta.servlet.Filter[] getServletFilters() {
        org.springframework.web.filter.CharacterEncodingFilter filter =
            new org.springframework.web.filter.CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return new jakarta.servlet.Filter[]{ filter };
    }
}