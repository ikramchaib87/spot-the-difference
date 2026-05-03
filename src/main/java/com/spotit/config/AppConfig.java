package com.spotit.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

// Classe de configuration principale : BDD, JPA, Transactions
@Configuration

// Lit les valeurs depuis application.properties
@PropertySource("classpath:application.properties")

// Scanne tous les composants sauf les Controllers (gérés par WebConfig)
@ComponentScan(
    basePackages = "com.spotit",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = org.springframework.stereotype.Controller.class
    )
)

// Active les repositories Spring Data JPA
@EnableJpaRepositories(
    basePackages = "com.spotit.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)

// Active le support des transactions (@Transactional)
@EnableTransactionManagement
public class AppConfig {

    // Injecte l'accès aux propriétés du fichier application.properties
    @Autowired
    private Environment env;

    // Configure la connexion à la base de données MySQL via HikariCP
    // HikariCP maintient un pool de connexions ouvertes pour éviter
    // d'en créer une nouvelle à chaque requête
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        ds.setJdbcUrl(env.getProperty("spring.datasource.url"));
        ds.setUsername(env.getProperty("spring.datasource.username"));
        ds.setPassword(env.getProperty("spring.datasource.password"));
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        return ds;
    }

    // Configure Hibernate comme implémentation JPA
    // Hibernate lit les entités @Entity et génère les tables MySQL automatiquement
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());

        // Indique où se trouvent les entités JPA (@Entity)
        em.setPackagesToScan("com.spotit.model");

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.setProperty("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));

        // "update" = Hibernate crée ou met à jour les tables sans supprimer les données
        props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        props.setProperty("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));
        props.setProperty("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));

        em.setJpaProperties(props);
        return em;
    }

    // Gestionnaire de transactions : garantit que les opérations BDD
    // sont atomiques (tout réussit ou tout échoue)
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory);
        return tm;
    }
}