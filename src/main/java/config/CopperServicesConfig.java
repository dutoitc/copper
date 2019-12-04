package config;

import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.db.DBServerSpring;
import ch.mno.copper.store.db.DBValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.StoriesFacadeImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CopperServicesConfig {

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
