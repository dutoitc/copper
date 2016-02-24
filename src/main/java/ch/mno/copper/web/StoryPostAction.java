package ch.mno.copper.web;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.Story;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 24.02.2016.
 */
public class StoryPostAction {

    public static void process(HttpServletRequest request, HttpServletResponse response, PrintWriter pw, String requestUri) throws IOException, ConnectorException {
        StoriesFacade sf = StoriesFacade.getInstance();

        Matcher matcher = Pattern.compile("\\/story\\/(.*)").matcher(requestUri);
        if (matcher.find()) {
            Story story = sf.getStoryByName(matcher.group(1));

            if (story==null) {
                response.setContentType("text/text");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                pw.write("Story " + matcher.group(1) + " was not found");
            } else {
                StringBuffer jb = new StringBuffer();
                String line = null;
                try {
                    BufferedReader reader = request.getReader();
                    while ((line = reader.readLine()) != null)
                        jb.append(line);
                } catch (Exception e) { /*report an error*/ }

                StoryPost post = new GsonBuilder().create().fromJson(jb.toString(), StoryPost.class);
                if (post.originalStoryName==null) {
                    String msg = sf.saveNewStory(post.storyName, post.storyText);
                    if (msg=="Ok") {
                        response.setContentType("text/text");
                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.write("Ok");
                    } else {
                        response.setContentType("text/text");
                        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                        pw.write(msg);
                    }
                } else {
                    String msg = sf.updateStory(post.originalStoryName, post.storyName, post.storyText);
                    if (msg=="Ok") {
                        response.setContentType("text/text");
                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.write("Ok");
                    } else {
                        response.setContentType("text/text");
                        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                        pw.write(msg);
                    }
                }
            }
        } else {
            response.setContentType("text/text");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            pw.write("Story name was not found");
        }
    }


    private static class StoryPost {
        private String originalStoryName;
        private String storyName;
        private String storyText;

        public static StoryPost build(HttpServletRequest request) {
            StoryPost p = new StoryPost();
            p.originalStoryName = request.getParameter("originalStoryName");
            p.storyName = request.getParameter("storyName");
            p.storyText = request.getParameter("storyText");
            return p;
        }
    }

}
