package com.synergizglobal.dms.config;

import javax.sql.DataSource;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.synergizglobal.dms.repository.dms", 
    entityManagerFactoryRef = "dmsEntityManagerFactory",
    transactionManagerRef = "dmsTransactionManager"
)
public class DmsDataSourceConfig {

    @Primary
    @Bean(name = "dmsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "dmsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("dmsDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.synergizglobal.dms.entity.dms") 
                .persistenceUnit("dmsPU")
                .build();
    }
    
    

    @Primary
    @Bean(name = "dmsTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("dmsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
