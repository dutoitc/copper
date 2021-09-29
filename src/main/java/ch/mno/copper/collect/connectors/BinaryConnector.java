package ch.mno.copper.collect.connectors;

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

    private static final Logger LOG = LoggerFactory.getLogger(BinaryConnector.class);
    public static final String EXCEPTION = "Exception: ";

    private BinaryConnector() {
        // private
    }

    public static String executeCommand(String cmd){
        ensureNotHavingCommandInjection(cmd);
        try {
            Process process = Runtime.getRuntime().exec(String.format(cmd));
            StringBuilder sb = new StringBuilder();
            StringBuilder sbErr = new StringBuilder();
            Consumer<String> consumer = (sb::append);
            Consumer<String> consumerErr = (sbErr::append);
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
            StreamGobbler streamGobblerErr = new StreamGobbler(process.getErrorStream(), consumerErr);
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.submit(streamGobbler);
            executorService.submit(streamGobblerErr);
            int exitCode = process.waitFor();
            Thread.sleep(100);
            executorService.shutdown();
            return sb + sbErr.toString() + (exitCode == 0 ? "" : "EXIT_" + exitCode);
        } catch (InterruptedException e) {
            LOG.trace(EXCEPTION + e.getMessage(), e);
            Thread.currentThread().interrupt();
            return EXCEPTION + e.getMessage();
        } catch (IOException e) {
            LOG.trace(EXCEPTION + e.getMessage(), e);
            return EXCEPTION + e.getMessage();
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
