package ch.mno.copper.stories.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by dutoitc on 27.04.2019.
 */
public class MatchedPattern implements Serializable {

    private int start;
    private int end;
    private String patternName;
    private String patternShort;
    private String patternFull;

    public MatchedPattern(int start, int end, String patternName, String patternShort, String patternFull) {
        this.start = start;
        this.end = end;
        this.patternName = patternName;
        this.patternShort = patternShort;
        this.patternFull = patternFull;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getPatternName() {
        return patternName;
    }

    public String getPatternShort() {
        return patternShort;
    }

    public String getPatternFull() {
        return patternFull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchedPattern that = (MatchedPattern) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        if (!Objects.equals(patternName, that.patternName)) return false;
        if (!Objects.equals(patternShort, that.patternShort)) return false;
        return Objects.equals(patternFull, that.patternFull);
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + (patternName != null ? patternName.hashCode() : 0);
        result = 31 * result + (patternShort != null ? patternShort.hashCode() : 0);
        result = 31 * result + (patternFull != null ? patternFull.hashCode() : 0);
        return result;
    }
}