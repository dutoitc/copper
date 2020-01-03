package ch.mno.copper.collect.connectors;

import ch.mno.copper.collect.BinaryCollectorWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BinaryConnector {

    private static Logger LOG = LoggerFactory.getLogger(BinaryCollectorWrapper.class);

    public static String executeCommand(String cmd){
        ensureNotHavingCommandInjection(cmd);
        try {
            Process process = Runtime.getRuntime().exec(String.format(cmd));
            StringBuilder sb = new StringBuilder();
            Consumer<String> consumer = (s->sb.append(s));
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            Thread.sleep(100);
            executorService.shutdown();
            return sb.toString();
        } catch (IOException | InterruptedException e) {
            LOG.trace("Exception: " + e.getMessage(), e);
            return "Exception: " + e.getMessage();
        }
    }

    private static void ensureNotHavingCommandInjection(String cmd) {
        if (cmd.contains(";") || cmd.contains("&")) { // Simple command injection protection: "dummy && rm -rf /"
            throw new RuntimeException("Security alert: path contains forbidden character");
        }
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

}
