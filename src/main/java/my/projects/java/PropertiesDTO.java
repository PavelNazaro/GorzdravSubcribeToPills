package my.projects.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesDTO {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private String botToken;
    private String botUsername;
    private String pathToJsonFromWeb;
    private long adminId;
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;

    protected static final String BOT_TOKEN = "botToken";
    protected static final String BOT_USERNAME = "botUsername";
    protected static final String PATH_TO_JSON_FROM_WEB = "pathToJsonFromWeb";
    protected static final String ADMIN_ID = "adminId";
    protected static final String DATABASE_URL = "databaseUrl";
    protected static final String DATABASE_USERNAME = "databaseUsername";
    protected static final String DATABASE_PASSWORD = "databasePassword";

    public PropertiesDTO(String botToken, String botUsername, String pathToJsonFromWeb, long adminId, String databaseUrl, String databaseUsername, String databasePassword) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.pathToJsonFromWeb = pathToJsonFromWeb;
        this.adminId = adminId;
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getPathToJsonFromWeb() {
        return pathToJsonFromWeb;
    }

    public long getAdminId() {
        return adminId;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public boolean isAllPropertiesNotEmpty(){
        boolean result = true;
        if (botToken.isEmpty()){
            LOGGER.debug("botToken empty!");
            result = false;
        }
        if (botUsername.isEmpty()){
            LOGGER.debug("botUsername empty!");
            result = false;
        }
        if (pathToJsonFromWeb.isEmpty()){
            LOGGER.debug("pathToJsonFromWeb empty!");
            result = false;
        }
        if (adminId <= 0){
            LOGGER.debug("adminId empty!");
            result = false;
        }
        if (databaseUrl.isEmpty()){
            LOGGER.debug("databaseUrl empty!");
            result = false;
        }
        if (databaseUsername.isEmpty()){
            LOGGER.debug("databaseUsername empty!");
            result = false;
        }
        if (databasePassword.isEmpty()){
            LOGGER.debug("databasePassword empty!");
            result = false;
        }
        return result;
    }
}
