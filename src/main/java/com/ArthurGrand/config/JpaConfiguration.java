package com.ArthurGrand.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.ArthurGrand.admin.tenants.repository",
                "com.ArthurGrand.module.employee.repository",
                "com.ArthurGrand.module.Client.repository",
                "com.ArthurGrand.module.payment.repository"
        },
        entityManagerFactoryRef = "jpaSharedEM_entityManagerFactory",
        transactionManagerRef = "jpaSharedEM_transactionManager"
)
public class JpaConfiguration {

    @Primary
    @Bean(name = "jpaSharedEM_entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource,
            JpaProperties jpaProperties) {

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        // FIXED: Add essential Hibernate properties
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", false);
        properties.put("hibernate.connection.autocommit", true);
        properties.put("hibernate.enable_lazy_load_no_trans", false);

        // FIXED: Connection pool integration
        properties.put("hibernate.connection.provider_disables_autocommit", false);
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.batch_versioned_data", true);

        return builder
                .dataSource(dataSource)
                .packages(
                        "com.ArthurGrand.admin.tenants.entity",
                        "com.ArthurGrand.module.employee.entity",
                        "com.ArthurGrand.module.Client.entity",
                        "com.ArthurGrand.module.payment.entity"
                )
                .persistenceUnit("default")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "jpaSharedEM_transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("jpaSharedEM_entityManagerFactory") EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);

        // FIXED: Transaction timeout settings
        transactionManager.setDefaultTimeout(30);
        transactionManager.setRollbackOnCommitFailure(true);

        return transactionManager;
    }
}