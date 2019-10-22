package ch.mno.copper.stories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;

public class DiskHelper {


    public static String STORIES_FOLDER = "stories";

    static {
        String folder = System.getProperty("copper.stories.folder");
        if (folder!=null) {
            STORIES_FOLDER = folder;
        }
    }

    /**
     * Save text as new file
     * @param storyName
     * @param storyText
     * @throws FileAlreadyExistsException if file already exists
     * @throws IOException on any IO exception
     */
    public static void saveNewStory(String storyName, String storyText) throws IOException {
        File file = new File(STORIES_FOLDER + '/' + storyName);
        if (file.exists()) {
            throw new FileAlreadyExistsException("Error: the file " + file.getName() + " already exists.");
        }
        if (file.canWrite()) {
            throw new AccessDeniedException("Error: the file " + file.getName() + " cannot be written.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        } catch (Exception e) {
            throw new FileSystemException(e.getMessage());
        }
    }

    public static void updateStory(String storyName, String storyText) throws IOException {
        File file = new File(STORIES_FOLDER + '/' + storyName);
        if (!file.exists()) {
            throw new FileAlreadyExistsException("Error: the file " + file.getName() + " must already exists.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        }
    }

    public static boolean storyExists(String storyName) {
        return new File(STORIES_FOLDER + "/" + storyName).exists();
    }

    public static void deleteStory(String storyName) {
        new File(STORIES_FOLDER + "/" + storyName).delete();
    }

    public static List<String> findStoryNames() {
        File stories = new File(STORIES_FOLDER);
        List<String> files = new ArrayList<>();
        for (File file : stories.listFiles(f -> f.isFile() && !f.getName().toLowerCase().endsWith("swp"))) {
            files.add(file.getName());
        }
        return files;
    }

    public static void ensureStoriesFolderExists() {
        File storiesFolder = new File(STORIES_FOLDER);
        if (storiesFolder.isFile()) throw new RuntimeException("stories should be a folder, not a file");
        if (!storiesFolder.exists()) storiesFolder.mkdir();
    }

    public static FileInputStream getStoryAsStream(String storyName) throws FileNotFoundException {
        return new FileInputStream(STORIES_FOLDER +"/"+storyName);
    }
}
