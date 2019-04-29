package ch.mno.copper.stories;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by dutoitc on 27.04.2019.
 */
public class StoryValidationResult implements Serializable {

    private String story;

    /** pattern match ok */
    private Set<MatchedPattern> perfectMatches = new TreeSet<>((a,b)->((b.getEnd()-b.getStart())-a.getEnd()-a.getStart()));

    /** pattern find = partial */
    private Set<MatchedPattern> partialMatches = new TreeSet<>((a,b)->((b.getEnd()-b.getStart())-a.getEnd()-a.getStart()));

    public StoryValidationResult(String story) {
        this.story = story;
    }

    public String getStory() {
        return story;
    }

    public Set<MatchedPattern> getPerfectMatches() {
        return perfectMatches;
    }

    public Set<MatchedPattern> getPartialMatches() {
        return partialMatches;
    }

    public void addPerfectMatch(int start, int end,String patternName,  String patternShort, String patternFull) {
        perfectMatches.add(new MatchedPattern(start, end, patternName, patternShort, patternFull));
    }

    public void addPartialMatch(int start, int end, String patternName, String patternShort, String patternFull) {
        partialMatches.add(new MatchedPattern(start, end, patternName, patternShort, patternFull));
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}