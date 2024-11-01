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
        basePackages = "com.github.arc33.springsend.repository.fileoperation",
        entityManagerFactoryRef = "fileOperationsEntityManagerFactory",
        transactionManagerRef = "fileOperationsTransactionManager"
)
public class FileOperationsRepoConfig {
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.file-operations")
    public DataSource fileOperationsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean fileOperationsEntityManagerFactory(
            @Qualifier("fileOperationsDataSource") DataSource dataSource) {
        return new LocalContainerEntityManagerFactoryBean() {{
            setDataSource(dataSource);
            setPackagesToScan("com.github.arc33.springsend.domain.event.fileoperation");
            setPersistenceUnitName("fileOperationsPersistenceUnit");
            setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            setJpaPropertyMap(hibernateProperties());
        }};
    }

    private Map<String,Object> hibernateProperties() {
        Map<String,Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");
        hibernateProperties.put("hibernate.hbm2ddl.auto","create-drop");
        return hibernateProperties;
    }

    @Primary
    @Bean
    public PlatformTransactionManager fileOperationsTransactionManager(
            @Qualifier("fileOperationsEntityManagerFactory") EntityManagerFactory profileEntityManagerFactory) {
        return new JpaTransactionManager(profileEntityManagerFactory);
    }
}