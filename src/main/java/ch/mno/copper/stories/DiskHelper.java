package ch.mno.copper.stories;

import ch.mno.copper.CopperException;
import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiskHelper {

    public static final String ERROR_THE_FILE = "Error: the file ";
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
        File file = new File(storiesFolder + '/' + securePath(storyName));
        if (file.exists()) {
            throw new FileAlreadyExistsException(ERROR_THE_FILE + file.getName() + " already exists.");
        }
        if (file.canWrite()) {
            throw new AccessDeniedException(ERROR_THE_FILE + file.getName() + " cannot be written.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        } catch (Exception e) {
            throw new FileSystemException(e.getMessage());
        }
    }

    public void updateStory(String storyName, String storyText) throws IOException {
        File file = new File(storiesFolder + '/' + securePath(storyName));
        if (!file.exists()) {
            throw new NoSuchFileException(ERROR_THE_FILE + file.getName() + " must already exists.");
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(storyText);
            fw.flush();
        }
    }

    /**
     * Ensure that storyName has no dot and slash
     */
    String securePath(String storyName) {
        if (storyName.contains("/")) {
            throw new CopperException("Storyname must not contains slash");
        }
        return storyName;
    }

    public boolean storyExists(String storyName) {
        return new File(storiesFolder + File.separatorChar + securePath(storyName)).exists();
    }

    public void deleteStory(String storyName) {
        try {
            Files.delete(Path.of(storiesFolder + File.separatorChar + securePath(storyName)));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public List<String> findStoryNames() {
        File stories = new File(storiesFolder);

        File[] files = stories.listFiles(f -> f.isFile() && !endsWith(f, "swp"));
        assert files != null;
        return Stream.of(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    boolean endsWith(File f, String ext) {
        assert f != null;
        return f.getName().toLowerCase().endsWith(ext.toLowerCase());
    }

    public void ensureStoriesFolderExists() {
        File fstoriesFolder = new File(storiesFolder);
        if (fstoriesFolder.isFile()) throw new RuntimeException("stories should be a folder, not a file");
        if (!fstoriesFolder.exists() && !fstoriesFolder.mkdir()) {
            throw new RuntimeException("Cannot make folder " + storiesFolder);
        }
    }

    public FileInputStream getStoryAsStream(String storyName) throws FileNotFoundException {
        return new FileInputStream(storiesFolder + '/' + storyName);
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
                                screens.put(a.getFileName().toString().replace(".json", ""), FileUtils.readFileToString(a.toFile(), StandardCharsets.UTF_8));
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

    public String findScreenData(String storyName) throws IOException {
        return IOUtils.toString(new FileReader(screensFolder + File.separatorChar + storyName));
    }
}
