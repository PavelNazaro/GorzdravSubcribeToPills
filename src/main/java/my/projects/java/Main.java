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
    protected static final String DATA_JSON_FILES_FOLDER_NAME = "data files";
    private static final String LOG_FILE_START_PATTERN = "log_";
    private static final String LOG_FILE_END_PATTERN = ".txt";
    private static final String LOG_FILE_PATTERN = LOG_FILE_START_PATTERN + "%s" + LOG_FILE_END_PATTERN;
    private static String logFilePath;
    private static String logMessage = StringUtils.EMPTY;
    protected static String absolutePath =
            new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
    private static final String ERROR_FILE_NOT_FOUND = "Error: File %s not found";
    private static final String CONFIG_PROPERTIES = "config.properties";
    protected static final String DATA_JSON = "data%s.json";
    protected static final String PROPERTY_BOT_TOKEN = "botToken";
    protected static final String PROPERTY_BOT_USERNAME = "botUsername";
    protected static final String PROPERTY_PATH_TO_JSON_FROM_WEB = "pathToJsonFromWeb";
    protected static final String PROPERTY_ADMIN_ID = "adminId";
    private static final String STOP = "stop";

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        checkAbsolutePath();
        File configFile = checkConfigFile();
        checkLogFile();
        File dataJsonFile = checkDataJsonFile();

        printToLogFirst("Absolute path: " + absolutePath + System.lineSeparator() + logMessage.trim());

        startConsoleHandler();

        try (InputStream configStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(configStream);
            if (properties.isEmpty()) {
                printToLog("Main Error 4: Properties is EMPTY!");
                shutdownJar();
            }

            String botUsername = properties.getProperty(PROPERTY_BOT_USERNAME);

            if (properties.getProperty(PROPERTY_BOT_TOKEN).isEmpty()
                    || botUsername.isEmpty()
                    || properties.getProperty(PROPERTY_PATH_TO_JSON_FROM_WEB).isEmpty()
                    || properties.getProperty(PROPERTY_ADMIN_ID).isEmpty()) {
                printToLog(String.format("Error: Any of properties is EMPTY! Properties names: %s, %s, %s, %s",
                        PROPERTY_BOT_TOKEN, PROPERTY_BOT_USERNAME, PROPERTY_PATH_TO_JSON_FROM_WEB, PROPERTY_ADMIN_ID));
                return;
            }

            MyBot bot = new MyBot(properties, dataJsonFile);
            if (bot.isGlobalError()) {
                printToLog(String.format("Main Error 6: Bot %s started with error!", botUsername));
                shutdownJar();
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            printToLog(String.format("Bot %s started successfully!", botUsername));

            new MyTimer(bot);
        } catch (IOException | TelegramApiException e) {
            printToLog(String.format("Main Error 7: Error reading %s: %s", CONFIG_PROPERTIES, e.getMessage()));
        }
    }

    private static void checkAbsolutePath() {
        if (absolutePath.endsWith("\\target")) {
            absolutePath = absolutePath.replace("\\target", "") + File.separator;
        }

        absolutePath += File.separator;
    }

    private static File checkConfigFile() {
        File configFile = new File(absolutePath + CONFIG_PROPERTIES);
        printToConsoleAndAddToLog("Config file absolute path: " + configFile.getAbsolutePath());
        if (!configFile.exists()) {
            System.out.println(String.format(ERROR_FILE_NOT_FOUND, configFile.getAbsolutePath()));
            shutdownJar();
        }
        return configFile;
    }

    private static void checkLogFile() {
        File logFilesFolder = new File(absolutePath + LOG_FILES_FOLDER_NAME);
        if (!logFilesFolder.exists()) {
            printToConsoleAndAddToLog("Log files folder created: " + logFilesFolder.mkdirs());
        }

        logFilePath = logFilesFolder.getAbsolutePath() + File.separator + String.format(LOG_FILE_PATTERN, LocalDate.now());
        File logFile = new File(logFilePath);
        try {
            if (!logFile.exists()) {
                printToConsoleAndAddToLog("Log file created: " + logFile.createNewFile());
            }
            printToConsoleAndAddToLog("Log file absolute path: " + logFile.getAbsolutePath());
            if (!logFile.exists()) {
                throw new IOException(String.format(ERROR_FILE_NOT_FOUND, logFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            shutdownJar();
        }
    }

    private static File checkDataJsonFile() {
        File dataJsonFilesFolder = new File(absolutePath + DATA_JSON_FILES_FOLDER_NAME);
        if (!dataJsonFilesFolder.exists()) {
            printToConsoleAndAddToLog("Data json files folder created: " + dataJsonFilesFolder.mkdirs());
        }

        File dataJsonFile = new File(dataJsonFilesFolder.getAbsolutePath() + File.separator + String.format(DATA_JSON, ""));
        try {
            if (!dataJsonFile.exists()) {
                printToConsoleAndAddToLog("Data json file created: " + dataJsonFile.createNewFile());
            }
            printToConsoleAndAddToLog("Data json file absolute path: " + dataJsonFile.getAbsolutePath());
            if (!dataJsonFile.exists()) {
                throw new IOException(String.format(ERROR_FILE_NOT_FOUND, dataJsonFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            shutdownJar();
        }
        return dataJsonFile;
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

            checkLogFile();
            Files.write(Paths.get(logFilePath), logText.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void printToConsoleAndAddToLog(String logMessageLog) {
        System.out.println(logMessageLog);
        logMessage += logMessageLog + System.lineSeparator();
    }

    protected static void shutdownJar() {
        printToLog("System exit");
        System.exit(0);
    }
}
