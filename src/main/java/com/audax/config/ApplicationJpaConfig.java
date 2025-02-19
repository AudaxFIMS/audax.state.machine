package com.audax.config;

import javax.sql.DataSource;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.audax.jpa"},
		entityManagerFactoryRef = "applicationEntityManagerFactory",
		transactionManagerRef = "applicationTransactionManager"
)
public class ApplicationJpaConfig {
	@Primary
	@Bean
	@ConfigurationProperties(prefix = "audax.datasource")
	public DataSource applicationDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean applicationEntityManagerFactory(
			EntityManagerFactoryBuilder builder,
			@Qualifier("applicationDataSource") DataSource dataSource) {
		return builder
				.dataSource(dataSource)
				.packages("com.audax.jpa")
				.persistenceUnit("applicationPU")
				.build();
	}
	
	@Primary
	@Bean
	public PlatformTransactionManager applicationTransactionManager(
			@Qualifier("applicationEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
