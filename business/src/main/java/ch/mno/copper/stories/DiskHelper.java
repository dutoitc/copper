package ch.mno.copper.stories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class DiskHelper {


    public static String STORIES_FOLDER = "stories";

    static {
        String folder = System.getenv("copper.stories.folder");
        if (folder!=null) {
            STORIES_FOLDER = folder;
        }
    }

    /**
     * Save text as new file
     * @param file
     * @param storyText
     * @throws FileAlreadyExistsException if file already exists
     * @throws IOException on any IO exception
     */
    public static void saveNewStory(File file, String storyText) throws IOException {
        if (file.exists()) {
            throw new FileAlreadyExistsException("Error: the file " + file.getName() + " already exists.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        }
    }

    public static void updateStory(File file, String storyText) throws IOException {
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
        File stories = new File("stories");
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
