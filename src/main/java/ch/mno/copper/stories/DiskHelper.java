package ch.mno.copper.stories;

import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class DiskHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DiskHelper.class);

    public final CopperStoriesProperties copperStoriesProperties;
    public final String storiesFolder;
    private final String screensFolder;

    public DiskHelper(CopperStoriesProperties copperStoriesProperties, CopperScreensProperties copperScreensProperties) {
        this.copperStoriesProperties = copperStoriesProperties;

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
        return new File(storiesFolder + File.separatorChar + storyName).exists();
    }

    public void deleteStory(String storyName) {
        try {
            Files.delete(Path.of(storiesFolder + File.separatorChar + storyName));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
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
        LOG.info("Searching screens in {}", new File(screensFolder).getAbsolutePath());
        if (new File(screensFolder).exists()) {
            try (Stream<Path> list = Files.list(Path.of(screensFolder))) {
                list
                    .sorted(Comparator.comparing(Path::getFileName))
                    .filter(a -> a.getFileName().toString().endsWith(".json"))
                    .forEach(a -> {
                        try {
                            screens.put(a.getFileName().toString().replace(".json", ""), FileUtils.readFileToString(a.toFile()));
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.info("Found {} json screens.", screens.size());
        }
        return screens;
    }

}
