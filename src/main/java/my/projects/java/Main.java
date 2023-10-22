package my.projects.java;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String LOG_FILES_FOLDER_NAME = "logs";
    private static final String LOG_FILE_START_PATTERN = "log_";
    private static final String LOG_FILE_END_PATTERN = ".txt";
    private static final String LOG_FILE_PATTERN = LOG_FILE_START_PATTERN + "%s" + LOG_FILE_END_PATTERN;
    private static String logFilePath;
    protected static String absolutePath =
            new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
    private static final String ERROR_FILE_NOT_FOUND = "Error: File %s not found";
    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final String STOP = "stop";

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        checkAbsolutePath();
        String logMessage = checkLogFile(true);
        printToLogFirst("Absolute path: " + absolutePath);
        printToLog(logMessage);

        PropertiesDTO propertiesDTO = checkConfigFileAndReturnProperties();
        if (propertiesDTO == null || !propertiesDTO.isAllPropertiesNotEmpty()) {
            printToLog("Main Error 7: Properties DTO null or empty!");
            shutdownJar();
            return;
        }

        startMyBot(propertiesDTO);
        startConsoleHandler();
    }

    private static void startMyBot(PropertiesDTO propertiesDTO) {
        MyBot bot = new MyBot(propertiesDTO);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            printToLog(String.format("Bot %s started successfully!", propertiesDTO.getBotUsername()));
        } catch (TelegramApiException e) {
            printToLog(String.format("Main Error 10: Error in register bot %s: %s", propertiesDTO.getBotUsername(), e.getMessage()));
        }
        new MyTimer(bot);
    }

    private static void checkAbsolutePath() {
        if (absolutePath.endsWith("\\target")) {
            absolutePath = absolutePath.replace("\\target", "") + File.separator;
        }

        absolutePath += File.separator;
    }

    private static PropertiesDTO checkConfigFileAndReturnProperties() {
        File configFile = new File(absolutePath + CONFIG_PROPERTIES);
        printToLog("Config file absolute path: " + configFile.getAbsolutePath());
        if (!configFile.exists()) {
            printToLog(String.format(ERROR_FILE_NOT_FOUND, configFile.getAbsolutePath()));
            shutdownJar();
        }

        PropertiesDTO propertiesDTO = null;
        try (InputStream configStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(configStream);
            if (properties.isEmpty()) {
                printToLog("Main Error 4: Properties is EMPTY!");
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
            printToLog(String.format("Main Error 6: Error reading %s: %s", CONFIG_PROPERTIES, e.getMessage()));
        }
        return propertiesDTO;
    }

    private static String checkLogFile(boolean isFirstCheck) {
        String logMessageLog = StringUtils.EMPTY;
        String logMessage;
        File logFilesFolder = new File(absolutePath + LOG_FILES_FOLDER_NAME);
        if (!logFilesFolder.exists()) {
            logMessage = "Log files folder created: " + logFilesFolder.mkdirs();
            System.out.println(logMessage);
            logMessageLog += logMessage + System.lineSeparator();
        }

        logFilePath = logFilesFolder.getAbsolutePath() + File.separator + String.format(LOG_FILE_PATTERN, LocalDate.now());
        File logFile = new File(logFilePath);
        try {
            if (!logFile.exists()) {
                logMessage = "Log file created: " + logFile.createNewFile();
                System.out.println(logMessage);
                logMessageLog += logMessage + System.lineSeparator();
            }
            logMessage = "Log file absolute path: " + logFile.getAbsolutePath();
            if (isFirstCheck) {
                System.out.println(logMessage);
            }
            logMessageLog += logMessage;
            if (!logFile.exists()) {
                throw new IOException(String.format(ERROR_FILE_NOT_FOUND, logFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            shutdownJar();
        }
        return logMessageLog;
    }

    private static void startConsoleHandler() {
        Thread waitingThread = new Thread(() -> {
            printToLog("Console handler started. Write 'stop' in console to stop jar. Waiting for input...");
            Scanner scanner = new Scanner(System.in);
            String input;
            while (!(input = scanner.nextLine()).equalsIgnoreCase(STOP)) {
                printToLog(String.format("Received: '%s'. Write 'stop' in console to stop jar", input));
            }
            printToLog("Stop command received. Exiting...");
            shutdownJar();
        });

        waitingThread.start();
    }

    private static void printToLogFirst(String text) {
        printToLog(text, false, true);
    }

    protected static void printToLog(String text) {
        printToLog(text, true, false);
    }

    private static void printToLog(String text, boolean isPrintToConsole, boolean isFirstPrint) {
        try {
            String logText = Calendar.getInstance().getTime() + " " + text;
            if (isFirstPrint) {
                logText = System.lineSeparator() + "----------Java started----------" + System.lineSeparator() + logText;
            }
            if (isPrintToConsole) {
                System.out.println(logText);
            }
            logText += System.lineSeparator();

            checkLogFile(false);
            Files.write(Paths.get(logFilePath), logText.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static void shutdownJar() {
        printToLog("System exit");
        System.exit(0);
    }
}
