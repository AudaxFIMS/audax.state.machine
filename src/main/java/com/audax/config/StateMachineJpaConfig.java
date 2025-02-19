package com.audax.config;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"org.springframework.statemachine.data.jpa"},  // Ensures JpaStateMachineRepository uses this EMF
		entityManagerFactoryRef = "stateMachineEntityManagerFactory",
		transactionManagerRef = "stateMachineTransactionManager"
)
public class StateMachineJpaConfig {
	@Bean
	@ConfigurationProperties(prefix = "statemachine.datasource")
	public DataSource stateMachineDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean stateMachineEntityManagerFactory(
			EntityManagerFactoryBuilder builder,
			@Qualifier("stateMachineDataSource") DataSource dataSource) {
		return builder
				.dataSource(dataSource)
				.packages("org.springframework.statemachine.data.jpa")  // Scan Spring State Machine JPA entities
				.persistenceUnit("stateMachinePU")
				.build();
	}
	
	@Bean
	public PlatformTransactionManager stateMachineTransactionManager(
			@Qualifier("stateMachineEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
