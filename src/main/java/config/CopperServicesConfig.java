package config;

import ch.mno.copper.DataProviderImpl;
import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.report.ReporterWrapperFactory;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.db.DBServerSpring;
import ch.mno.copper.store.db.DBValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.StoriesFacadeImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class CopperServicesConfig {
/*
        DataProvider dataProvider = new DataProviderImpl(storiesFacade, valuesStore);

        // Start daemon
        CopperDaemon daemon = new CopperDaemon(dataProvider, jmxPort);
        CopperMediator mediator = new CopperMediator(valuesStore, dataProvider, storiesFacade, daemon, propertiesProvider); // keep instances (used by services)
        Thread threadDaemon = new Thread(daemon);
        threadDaemon.start();

 */



    @Bean
    public ReporterWrapperFactory reporterWrapperFactory(Environment environment) {
        return new ReporterWrapperFactory(environment);
    }

    @Bean
    public CopperDaemon copperDaemon(DataSource dataSource) {
        return new CopperDaemon(dataProvider(dataSource), "30401");
    }

    @Bean
    public DataProviderImpl dataProvider(DataSource dataSource) {
        return new DataProviderImpl(storiesFacade(), valuesStore(dataSource));
    }

    @Bean
    public StoriesFacade storiesFacade() {
        return new StoriesFacadeImpl();
    }

    @Bean
    public ValuesStore valuesStore(DataSource dataSource) {
        return new DBValuesStore(dbServer(dataSource));
    }

    @Bean
    public DBServerSpring dbServer(DataSource dataSource) {
        return new DBServerSpring(dataSource);
    }
}
