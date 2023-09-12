package my.projects.java;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static final String CONFIG_PROPERTIES = "config.properties";
    public static final String DATA_JSON = "data%s.json";

    public static void main(String[] args) {
        logger.log(Level.INFO, "Starting bot...");
        File dataJsonFile = null;
        String fileName = String.format(DATA_JSON, "");
        try {
            dataJsonFile = findOrCreateFile(fileName);
        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Main Error 1: Error reading %s: %s", fileName, e.getMessage()));
            shutdownJar();
        }

        if (!dataJsonFile.exists()) {
            logger.log(Level.WARNING, "Main Error 2: {0} still not exist", CONFIG_PROPERTIES);
            shutdownJar();
        }

        File configFile = new File(CONFIG_PROPERTIES);
        if (!configFile.exists()){
            logger.log(Level.WARNING, "Main Error 3: File {0} not found", CONFIG_PROPERTIES);
            shutdownJar();
        }

        try(InputStream configStream = new FileInputStream(configFile)){
            Properties properties = new Properties();
            properties.load(configStream);
            if (properties.isEmpty()) {
                logger.log(Level.WARNING, "Main Error 4: Properties is EMPTY!");
                shutdownJar();
            }

            String botToken = properties.getProperty("botToken");
            String botUsername = properties.getProperty("botUsername");
            String pathToJsonFromWeb = properties.getProperty("pathToJsonFromWeb");

            if (botToken.isEmpty() || botUsername.isEmpty() || pathToJsonFromWeb.isEmpty()) {
                logger.log(Level.WARNING, "Main Error 5: Any of properties is EMPTY!");
                return;
            }

            MyBot bot = new MyBot(botToken, botUsername, pathToJsonFromWeb, dataJsonFile, logger);
            if (bot.isGlobalError()) {
                logger.log(Level.WARNING, "Main Error 6: Bot {0} started with error!", botUsername);
                shutdownJar();
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            logger.log(Level.INFO, "Bot {0} started successfully!", botUsername);
        } catch (IOException | TelegramApiException e){
            logger.log(Level.WARNING, String.format("Main Error 7: Error reading %s: %s", CONFIG_PROPERTIES, e.getMessage()));
        }

//        logger.log(Level.INFO, "Bot finished");
    }

    protected static File findOrCreateFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.createNewFile()){
            logger.log(Level.INFO, "File {0} created", file.getAbsolutePath());
        } else {
            logger.log(Level.INFO, "File {0} found successfully", file.getAbsolutePath());
        }
        return file;
    }

    private static void shutdownJar() {
        logger.log(Level.INFO, "System exit");
        System.exit(0);
    }
}
