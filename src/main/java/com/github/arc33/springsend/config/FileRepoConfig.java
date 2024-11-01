package com.github.arc33.springsend.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.github.arc33.springsend.repository.file",
        entityManagerFactoryRef = "fileEntityManagerFactory",
        transactionManagerRef = "fileTransactionManager"
)
public class FileRepoConfig {

    @Primary  // If this is your main/default datasource
    @Bean
    @ConfigurationProperties("spring.datasource.file")
    public DataSource fileDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean fileEntityManagerFactory(
            @Qualifier("fileDataSource") DataSource dataSource) {
        return new LocalContainerEntityManagerFactoryBean() {{
            setDataSource(dataSource);
            setPackagesToScan("com.github.arc33.springsend.model");
            setPersistenceUnitName("filePersistenceUnit");
            setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            setJpaPropertyMap(hibernateProperties());
        }};
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        return properties;
    }

    @Primary
    @Bean
    public PlatformTransactionManager fileTransactionManager(
            @Qualifier("fileEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}