package ch.mno.copper.report;

import ch.mno.copper.CopperException;
import ch.mno.copper.stories.data.StoryGrammar;
import config.CopperMailProperties;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class ReporterWrapperFactory {

    private final CopperMailProperties copperMailProperties;

    public ReporterWrapperFactory(CopperMailProperties copperMailProperties) {
        this.copperMailProperties = copperMailProperties;
    }

    public <T extends AbstractReporterWrapper> T buildReporterWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("PUSHOVER"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return (T) PushoverReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile(grammar.getPatternFull("WEBEX"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return (T) WebexDeltaReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile(grammar.getPatternFull("MAIL"), Pattern.DOTALL).matcher(storyGiven).find()) {
            if (copperMailProperties==null) {
                throw new CopperException("Please specify mail parameters");
            }
            return (T) MailReporterWrapper.buildReporter(grammar, storyGiven + '\n',
                    copperMailProperties.getServer(),
                    copperMailProperties.getUsername(),
                    copperMailProperties.getPassword(),
                    copperMailProperties.getPort(),
                    copperMailProperties.getFrom(),
                    copperMailProperties.getReplyTo());
        } else if (Pattern.compile(grammar.getPatternFull("CSV"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return (T) CsvReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile("STORE VALUES").matcher(storyGiven).find()) {
            //return JdbcCollectorWrapper.buildStoryTask(grammar, storyGiven + '\n');
            return null;
        }
        throw new RuntimeException("Cannot find a valid REPORT expression builder");
    }
}
