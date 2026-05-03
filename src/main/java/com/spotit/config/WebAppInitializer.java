package com.spotit.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

// Cette classe remplace web.xml
// Tomcat la détecte automatiquement au démarrage grâce à l'interface ServletContainerInitializer
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // Configuration racine chargée en premier : BDD, Service, Repository
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{ AppConfig.class };
    }

    // Configuration web chargée après : Controllers, Thymeleaf, ressources statiques
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{ WebConfig.class };
    }

    // Le DispatcherServlet intercepte toutes les URLs de l'application
    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }

    // Filtre qui force l'encodage UTF-8 sur toutes les requêtes et réponses
    // Évite les problèmes d'affichage des caractères spéciaux (accents, etc.)
    @Override
    protected jakarta.servlet.Filter[] getServletFilters() {
        org.springframework.web.filter.CharacterEncodingFilter filter =
            new org.springframework.web.filter.CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return new jakarta.servlet.Filter[]{ filter };
    }
}