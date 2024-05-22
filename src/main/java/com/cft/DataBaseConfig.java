package com.cft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DataBaseConfig {

    private static final Logger LOGGER = LogManager.getLogger(DataBaseConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource serviceDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("cftservice.driver.class.name"));
        dataSource.setUrl(env.getProperty("cftservice.datasource.url"));
        dataSource.setUsername(env.getProperty("cftservice.datasource.user"));
        dataSource.setPassword(env.getProperty("cftservice.datasource.password"));
        System.out.println("DB URL:"+env.getProperty("cftservice.datasource.url"));
        System.out.println("DB Driver name: "+env.getProperty("cftservice.driver.class.name"));
        return dataSource;
    }


    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate(DataSource dataSource) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        LOGGER.info("NamedParameterJdbcTemplate initialized: "+ namedParameterJdbcTemplate);
        return namedParameterJdbcTemplate;
    }
}
