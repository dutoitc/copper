package ch.mno.copper.stories;

import config.CopperStoriesProperties;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;

public class DiskHelper {

    public final String storiesFolder;
    public final CopperStoriesProperties copperStoriesProperties;

    public DiskHelper(CopperStoriesProperties copperStoriesProperties) {
        this.copperStoriesProperties = copperStoriesProperties;

        this.storiesFolder = copperStoriesProperties.getFolder();
        ensureStoriesFolderExists();
    }

    /**
     * Save text as new file
     *
     * @param storyName
     * @param storyText
     * @throws FileAlreadyExistsException if file already exists
     * @throws IOException                on any IO exception
     */
    public void saveNewStory(String storyName, String storyText) throws IOException {
        File file = new File(storiesFolder + '/' + storyName);
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

    public void updateStory(String storyName, String storyText) throws IOException {
        File file = new File(storiesFolder + '/' + storyName);
        if (!file.exists()) {
            throw new FileAlreadyExistsException("Error: the file " + file.getName() + " must already exists.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        }
    }

    public boolean storyExists(String storyName) {
        return new File(storiesFolder + "/" + storyName).exists();
    }

    public void deleteStory(String storyName) {
        new File(storiesFolder + "/" + storyName).delete();
    }

    public List<String> findStoryNames() {
        File stories = new File(storiesFolder);
        List<String> files = new ArrayList<>();
        for (File file : stories.listFiles(f -> f.isFile() && !f.getName().toLowerCase().endsWith("swp"))) {
            files.add(file.getName());
        }
        return files;
    }

    public void ensureStoriesFolderExists() {
        File fstoriesFolder = new File(storiesFolder);
        if (fstoriesFolder.isFile()) throw new RuntimeException("stories should be a folder, not a file");
        if (!fstoriesFolder.exists()) fstoriesFolder.mkdir();
    }

    public FileInputStream getStoryAsStream(String storyName) throws FileNotFoundException {
        return new FileInputStream(storiesFolder + "/" + storyName);
    }
}
