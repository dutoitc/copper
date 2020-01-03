package ch.mno.copper.stories;

import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiskHelper {

    public final CopperStoriesProperties copperStoriesProperties;
    private final CopperScreensProperties copperScreensProperties;
    public final String storiesFolder;
    private final String screensFolder;

    public DiskHelper(CopperStoriesProperties copperStoriesProperties, CopperScreensProperties copperScreensProperties) {
        this.copperStoriesProperties = copperStoriesProperties;
        this.copperScreensProperties = copperScreensProperties;

        this.storiesFolder = copperStoriesProperties.getFolder();
        this.screensFolder = copperScreensProperties.getFolder();
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

    public Map<String, String> findScreens() {
        Map<String, String> screens = new LinkedHashMap<>();
        if (new File(screensFolder).exists()) {
            try {
                Files.list(Path.of(screensFolder))
                        .sorted(Comparator.comparing(a -> a.getFileName()))
                        .filter(a->a.getFileName().toString().endsWith(".json"))
                        .forEach(a-> {
                            try {
                                screens.put(a.getFileName().toString().replace(".json", ""), FileUtils.readFileToString(a.toFile()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return screens;
    }

}
