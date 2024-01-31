package ch.mno.copper.stories;

import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public static DiskHelperBuilder builder() {
        return new DiskHelperBuilder();
    }

    public static class DiskHelperBuilder {
        private final CopperStoriesProperties storiesProperties = new CopperStoriesProperties();
        private final CopperScreensProperties screensProperties = new CopperScreensProperties();

        public DiskHelperBuilder withStoriesFolder(String x) {
            storiesProperties.setFolder(x);
            return this;
        }

        public DiskHelperBuilder withScreensFolder(String x) {
            screensProperties.setFolder(x);
            return this;
        }

        public DiskHelper build() {
            return new DiskHelper(storiesProperties, screensProperties);
        }
    }

    /**
     * Save text as new file
     *
     * @throws FileAlreadyExistsException if file already exists
     * @throws IOException                on any IO exception
     */
    public void saveNewStory(String storyName, String storyText) throws IOException {
        File file = getSecureFile(storiesFolder, securePath(storyName));
        if (file.exists()) {
            throw new FileAlreadyExistsException(ERROR_THE_FILE + file.getName() + " already exists.");
        }
        if (file.canWrite()) {
            throw new AccessDeniedException(ERROR_THE_FILE + file.getName() + " cannot be written.");
        }
        Files.writeString(file.toPath(), storyText);
    }

    public void updateStory(String storyName, String storyText) throws IOException {
        File file = getSecureFile(storiesFolder, securePath(storyName));
        Validate.isTrue(file.exists(), ERROR_THE_FILE + file.getName() + " must already exists.");
        Files.writeString(file.toPath(), storyText);
    }

    /**
     * Ensure that storyName has no dot and slash
     */
    String securePath(String storyName) {
        Validate.isTrue(!storyName.contains("/"), "Storyname must not contains slash");
        return storyName;
    }

    public boolean storyExists(String storyName) {
        File file = getSecureFile(storiesFolder, securePath(storyName));
        return file.exists();
    }

    public void deleteStory(String storyName) {
        try {
            var file = getSecureFile(storiesFolder, storyName);
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public List<String> findStoryNames() {
        File stories = new File(storiesFolder);
        File[] files = stories.listFiles(f -> f.isFile() && !endsWith(f, "swp"));
        Validate.isTrue(files != null);
        return Stream.of(files)
                .map(File::getName)
                .sorted()
                .toList();
    }

    boolean endsWith(File f, String ext) {
        Validate.isTrue(f != null);
        return f.getName().toLowerCase().endsWith(ext.toLowerCase());
    }

    public void ensureStoriesFolderExists() {
        File fstoriesFolder = new File(storiesFolder);
        Validate.isTrue(!fstoriesFolder.isFile(), "stories should be a folder, not a file");
        if (!fstoriesFolder.exists() && !fstoriesFolder.mkdir()) {
            throw new StoryException("Cannot make folder " + storiesFolder);
        }
    }

    public FileInputStream getStoryAsStream(String storyName) throws FileNotFoundException {
        return new FileInputStream(storiesFolder + File.separatorChar + storyName);
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
        var file = getSecureFile(screensFolder, storyName);
        return IOUtils.toString(new FileReader(file));
    }


    File getSecureFile(String folder, String path) {
        File file = new File(folder + File.separatorChar + path);
        String canonicalDestinationPath;
        try {
            canonicalDestinationPath = file.getCanonicalPath();

            if (!canonicalDestinationPath.startsWith(new File(folder).getCanonicalPath())) {
                throw new StoryException("Entry is outside of the target directory");
            }
        } catch (IOException e) {
            throw new StoryException(e.getMessage());
        }

        return file;
    }

}
