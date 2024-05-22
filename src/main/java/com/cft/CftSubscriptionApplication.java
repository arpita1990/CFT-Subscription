package com.cft;

import com.cft.subscription.serviceImpl.UserSubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cft"})
@EnableJpaRepositories(basePackages = {"com.cft.persistence.repository", "com.cft.subscription.repository"})
@EntityScan(basePackages = {"com.cft.persistence.model", "com.cft.subscription.model"})
@EnableScheduling
public class CftSubscriptionApplication implements CommandLineRunner {

	@Autowired
	private UserSubscriptionServiceImpl userSubscriptionServiceImpl;

	public static void main(String[] args) {

		ApplicationContext applicationContext = SpringApplication.run(CftSubscriptionApplication.class, args);
	}

	@Override
	public void run(String...args) throws Exception {
		userSubscriptionServiceImpl.getUserSubscriptions();

	}

}
