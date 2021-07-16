package config;

import ch.mno.copper.report.ReporterWrapperFactory;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.db.DBServerSpring;
import ch.mno.copper.store.db.DBValuesStore;
import ch.mno.copper.stories.DiskHelper;
import ch.mno.copper.stories.StoriesFacadeImpl;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CopperServicesConfig {

    @Bean
    public StoryGrammar storyGrammar() {
        return new StoryGrammar(StoriesFacadeImpl.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Bean
    public DiskHelper diskHelper(CopperStoriesProperties copperStoriesProperties, CopperScreensProperties copperScreensProperties) {
        return new DiskHelper(copperStoriesProperties, copperScreensProperties);
    }

    @Bean
    public ReporterWrapperFactory reporterWrapperFactory(CopperMailProperties copperMailProperties) {
        return new ReporterWrapperFactory(copperMailProperties);
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
