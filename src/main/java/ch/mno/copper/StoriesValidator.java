package ch.mno.copper;

import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by dutoitc on 02.04.2016.
 */
public class StoriesValidator {

    private final StoryGrammar storyGrammar;


    public StoriesValidator() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    private void validate(String[] args) {

        for (String arg: args) {
            File file = new File(arg);
            if (file.isFile()) {
                validate(file);
            } else if (file.isDirectory()) {
                Arrays.asList(file.listFiles()).forEach(this::validate);
            }
        }
    }

    private void validate(File file) {
        System.out.println("=========================================");
        System.out.println("Validation of " + file.getName());
        String patternMain = storyGrammar.getPatternFull("MAIN");

        try {
            String storyText = IOUtils.toString(file.toURI());

            // Check Story Syntax
            // TODO: mark storyText as invalid, and permit WEB update
            try {
                SyntaxHelper.checkSyntax(storyGrammar, patternMain, storyText);
            } catch (SyntaxException e) {
                System.out.println("Invalid:\n" + e.getMessage());
            }
            System.out.println("OK");
        } catch (IOException e){
            System.out.println("KO: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        new StoriesValidator().validate(args);
    }

}
