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
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.github.arc33.springsend.repository.userregistration",
        entityManagerFactoryRef = "userRegistrationEventEntityManagerFactory",
        transactionManagerRef = "userRegistrationEventTransactionManager"
)
public class UserRegistrationEventRepoConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.user-registration-events")
    public DataSource userRegistrationEventDataSource() {  // Fixed typo in method name
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean userRegistrationEventEntityManagerFactory(
            @Qualifier("userRegistrationEventDataSource") DataSource dataSource) {
        return new LocalContainerEntityManagerFactoryBean() {{
            setDataSource(dataSource);
            setPackagesToScan("com.github.arc33.springsend.domain.event.userregistration");
            setPersistenceUnitName("userRegistrationPersistenceUnit");
            setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            setJpaPropertyMap(hibernateProperties());
        }};
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");  // For development
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.jdbc.batch_size", "20");
        return props;
    }

    @Primary
    @Bean
    public PlatformTransactionManager userRegistrationEventTransactionManager(
            @Qualifier("userRegistrationEventEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}