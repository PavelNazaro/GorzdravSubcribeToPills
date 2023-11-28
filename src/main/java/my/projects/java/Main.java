package my.projects.java;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    protected static String absolutePath =
            new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
    private static final String ERROR_FILE_NOT_FOUND = "Error: File {} not found";
    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final String STOP = "stop";

    public static void main(String[] args) {
        LOGGER.debug("----------Java started----------");
        long startTime = System.nanoTime();

        checkAbsolutePath();

        PropertiesDTO propertiesDTO = checkConfigFileAndReturnProperties();
        if (propertiesDTO == null || !propertiesDTO.isAllPropertiesNotEmpty()) {
            LOGGER.error("Main Error 7: Properties DTO null or empty!");
            shutdownJar();
            return;
        }

        startMyBot(propertiesDTO, startTime);
        startConsoleHandler();
    }

    private static void startMyBot(PropertiesDTO propertiesDTO, long startTime) {
        ConnectionsToDB connectionsToDB = new ConnectionsToDB(propertiesDTO);
        if (!connectionsToDB.createConnection()){
            LOGGER.error("Error in create connection!");
            shutdownJar();
        }

        MyBot bot = new MyBot(connectionsToDB, propertiesDTO, startTime);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            LOGGER.debug("Bot {} started successfully!", propertiesDTO.getBotUsername());
        } catch (TelegramApiException e) {
            LOGGER.error(String.format("Main Error 10: Error in register bot %s: %s", propertiesDTO.getBotUsername(), e.getMessage()));
        }
        new MyTimer(bot);
    }

    private static void checkAbsolutePath() {
        if (absolutePath.endsWith("\\target")) {
            absolutePath = absolutePath.replace("\\target", "") + File.separator;
        }

        absolutePath += File.separator;
        LOGGER.debug("Absolute path: {}", absolutePath);
    }

    private static PropertiesDTO checkConfigFileAndReturnProperties() {
        File configFile = new File(absolutePath + CONFIG_PROPERTIES);
        LOGGER.debug("Config file absolute path: {}", configFile.getAbsolutePath());
        if (!configFile.exists()) {
            LOGGER.error(ERROR_FILE_NOT_FOUND, configFile.getAbsolutePath());
            shutdownJar();
        }

        PropertiesDTO propertiesDTO = null;
        try (InputStream configStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(configStream);
            if (properties.isEmpty()) {
                LOGGER.error("Main Error 4: Properties is EMPTY!");
                shutdownJar();
            }

            propertiesDTO = new PropertiesDTO(properties.getProperty(PropertiesDTO.BOT_TOKEN, StringUtils.EMPTY),
                    properties.getProperty(PropertiesDTO.BOT_USERNAME, StringUtils.EMPTY),
                    properties.getProperty(PropertiesDTO.PATH_TO_JSON_FROM_WEB, StringUtils.EMPTY),
                    Long.parseLong(properties.getProperty(PropertiesDTO.ADMIN_ID, "0")),
                    properties.getProperty(PropertiesDTO.DATABASE_URL, StringUtils.EMPTY),
                    properties.getProperty(PropertiesDTO.DATABASE_USERNAME, StringUtils.EMPTY),
                    properties.getProperty(PropertiesDTO.DATABASE_PASSWORD, StringUtils.EMPTY)
            );
        } catch (IOException e) {
            LOGGER.error(String.format("Main Error 6: Error reading %s: %s", CONFIG_PROPERTIES, e.getMessage()));
        }
        return propertiesDTO;
    }

    private static void startConsoleHandler() {
        Thread waitingThread = new Thread(() -> {
            LOGGER.debug("Console handler started. Write 'stop' in console to stop jar. Waiting for input...");
            Scanner scanner = new Scanner(System.in);
            String input;
            while (!(input = scanner.nextLine()).equalsIgnoreCase(STOP)) {
                LOGGER.debug(String.format("Received: '%s'. Write 'stop' in console to stop jar", input));
            }
            LOGGER.debug("Stop command received. Exiting...");
            shutdownJar();
        });

        waitingThread.start();
    }

    protected static void shutdownJar() {
        LOGGER.debug("System exit");
        System.exit(0);
    }
}
