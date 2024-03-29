package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    public DataSource getDataSource(
            @Value("${spring.datasource.url}") String datasourceURL,
            @Value("${spring.datasource.username}") String datasourceUsername,
            @Value("${spring.datasource.password}") String datasourcePassword
            ) {
        LOG.info("Using DB at {}, path is {}", datasourceURL, new File(".").getAbsolutePath());
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url(datasourceURL);
        dataSourceBuilder.username(datasourceUsername);
        dataSourceBuilder.password(datasourcePassword);
        DataSource ds = dataSourceBuilder.build();

        // Clean liquibase lock (could be set at startup)
        try (Connection conn = ds.getConnection()) {
            conn.prepareCall("UPDATE DATABASECHANGELOGLOCK SET LOCKED=FALSE, LOCKGRANTED=null, LOCKEDBY=null where ID=1");
            LOG.info("Liquibase lock cleaned");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Table \"DATABASECHANGELOGLOCK\" not found")) {
                // No database = no need to clean, ignoring error
                LOG.warn("Error while cleaning liquibase lock; continuing {}",e.getMessage(), e);
            }
        }

        return ds;
    }
}