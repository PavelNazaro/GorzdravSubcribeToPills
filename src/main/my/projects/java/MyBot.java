package main.my.projects.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.my.projects.java.Main.DATA_JSON;
import static main.my.projects.java.Main.findOrCreateFile;

public class MyBot extends TelegramLongPollingBot {
    private final Logger logger;
    private final String botToken;
    private final String botUsername;
    private final File dataJsonFile;
    private File dataJsonWithIdFile;
    private Map<Long, Boolean> idsMap;
    private long chatId;
    private String lastDay;
    private String userName;
    private Set<String> districtsSet;
    private String lastCommand;
    private boolean globalError = false;
    private final SendMessage sendMessage = new SendMessage();
    private static final Field[] DATA_WITH_ID_JSON_DECLARED_FIELDS = DataWithIdJson.class.getDeclaredFields();
    private static final Field[] DATA_JSON_DECLARED_FIELDS = DataJson.class.getDeclaredFields();
    private static final String START = "/start";
    private static final String STOP = "/stop";
    private static final String MY_SUBSCRIPTIONS = "/my_subscriptions";
    private static final String SHOW_DISTRICTS = "/show_districts";
    private static final String CHANGE_DISTRICTS = "/change_districts";
    private static final String GET_DATA_FROM = "Get data from: {0}";
    private static final String BOT_STARTED = "Бот запущен!";
    private static final String BOT_ALREADY_STARTED = "Бот уже запущен!";
    private static final String BOT_STOPPED = "Бот остановлен!";
    private static final String BOT_ALREADY_STOPPED = "Бот уже остановлен!";
    private static final String END_OF_CHOOSE = "Завершить выбор";
    private static final String YOU_SUBSCRIBE_SUCCESSFULLY = "Вы успешно подписались!";
    private static final String YOU_UNSUBSCRIBE_SUCCESSFULLY = "Вы успешно отписались!";
    private static final String YOUR_DISTRICTS = "Ваши районы:";
    private static final String CHOOSE_CONVENIENT_DISTRICTS = "Выберите удобные для Вас районы:";
    private static final String YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM = "Вам необходимо выбрать как минимум 1 район!";
    private static final String CHOOSE_ALL_DISTRICTS = "Выбрать все";
    private static final String REMOVE_ALL_DISTRICTS = "Удалить все";
    private static final String BUTTON_ADD = " (Добавить)";
    private static final String BUTTON_DELETE = " (Удалить)";
    private static final String EMPTY = "Пусто";
    private static final String DOT_AND_SPACE = ". ";
    private static final String INFO_MESSAGE = "---------------ИНФО:---------------" + System.lineSeparator() + "Нажмите MENU чтобы посмотреть все команды";
    private static final String INFO_START = "---------------ИНФО:---------------" + System.lineSeparator() + "Выберите в MENU /start чтобы запустить бот";
    private static final String WRONG_COMMAND = "Неверная команда!" + System.lineSeparator();
    private static final String DISTRICT = " район";
    private static final List<String> districts = new ArrayList<>(List.of("Адмиралтейский" + DISTRICT, "Василеостровский" + DISTRICT, "Выборгский" + DISTRICT, "Калининский" + DISTRICT, "Кировский" + DISTRICT, "Колпинский" + DISTRICT, "Красногвардейский" + DISTRICT, "Красносельский" + DISTRICT, "Кронштадтcкий" + DISTRICT, "Курортный" + DISTRICT, "Московский" + DISTRICT, "Невский" + DISTRICT, "Петроградский" + DISTRICT, "Петродворцовый" + DISTRICT, "Приморский" + DISTRICT, "Пушкинский" + DISTRICT, "Фрунзенский" + DISTRICT, "Центральный" + DISTRICT));
    private static final String ROUND_BRACKET_OPEN = " (";
    private static final String ROUND_BRACKET_CLOSE = ")";

    public MyBot(String botToken, String botUsername, File dataJsonFile, Logger logger) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.dataJsonFile = dataJsonFile;
        this.logger = logger;
        this.lastCommand = StringUtils.EMPTY;

        long startTime = System.nanoTime();

        if (!getDataFromJsonFile(false)) {
            logger.log(Level.WARNING, "MyBot Error 1: Error in create dataJson file!");
            globalError = true;
        }

        logger.log(Level.INFO, "Start duration, ms: {0}", (System.nanoTime() - startTime) / 1000000);
    }

    public void onUpdateReceived(Update update) {
        if (globalError) {
            logger.log(Level.WARNING, "MyBot Error 2: Global error!");
            return;
        }

        long startTime = System.nanoTime();

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (!message.hasText()) {
                logger.log(Level.WARNING, "MyBot Error 3: Message is EMPTY!");
                return;
            }

            messageProcessing(message);
        }

        logger.log(Level.INFO, "Duration, ms: {0}", (System.nanoTime() - startTime) / 1000000);
    }

    private void messageProcessing(Message message) {
        chatId = message.getChatId();
        userName = message.getChat().getUserName();
        String text = message.getText();

        if (text.isEmpty()) {
            logger.log(Level.WARNING, "MyBot Error 4: Message is EMPTY!");
            return;
        }

        logger.log(Level.INFO, String.format("User id: %s sent: %s", chatId, text));

        if (!idsMap.containsKey(chatId) || (idsMap.containsKey(chatId) && !idsMap.get(chatId))) {
            logger.log(Level.INFO, "Bot status: stopped");
            if (!text.equals(START)) {
                text = STOP;
            }
        }

        if (!createDataJsonWithIdFile()) {
            return;
        }

        checkTextIsCommandOrUserChoose(text);
    }

    private void checkTextIsCommandOrUserChoose(String text) {
        String editedText = StringUtils.EMPTY;
        if (text.contains(ROUND_BRACKET_OPEN) && text.contains(ROUND_BRACKET_CLOSE)) {
            editedText = text.substring(0, text.lastIndexOf(ROUND_BRACKET_OPEN));
        }
        if (text.equals(START)) {
            proceedStartCommand();
            return;
        }
        if (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS)) {
            if (!editedText.equals(StringUtils.EMPTY) && districts.contains(editedText)) {
                proceedChangeDistrict(editedText);
                return;
            }
            if (districts.contains(text)) {
                proceedChangeDistrict(text);
                return;
            }
        }
        if (text.equals(MY_SUBSCRIPTIONS)) {
            sendDistrictsSubscriptionToBot();
            lastCommand = MY_SUBSCRIPTIONS;
            return;
        }
        if (text.equals(CHANGE_DISTRICTS)) {
            sendDistrictsSubscriptionToBot();
            sendDistrictsToBot();
            lastCommand = CHANGE_DISTRICTS;
            return;
        }
        if (text.equals(END_OF_CHOOSE)) {
            if (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS)) {
                if (districtsSet.isEmpty()) {
                    sendMessageToBot(YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM);
                    sendDistrictsToBot();
                    return;
                }
                sendDistrictsSubscriptionToBot();
                lastCommand = END_OF_CHOOSE;
                return;
            }
        }
        if (text.equals(CHOOSE_ALL_DISTRICTS) && (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS))) {
            proceedChooseAllDistricts();
            return;
        }
        if (text.equals(REMOVE_ALL_DISTRICTS) && (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS))) {
            proceedRemoveAllDistricts();
            return;
        }
        if (text.equals(STOP)) {
            proceedStopCommand();
            return;
        }

        sendMessageToBot(WRONG_COMMAND + INFO_MESSAGE);
    }

    private void proceedStartCommand() {
        if (idsMap.containsKey(chatId) && idsMap.get(chatId)) {
            sendMessageToBot(BOT_ALREADY_STARTED);
        } else {
            sendMessageToBot(BOT_STARTED);
            idsMap.put(chatId, true);
            writeDataToJsonWithIdFile();
            writeDataToFile();
        }

        if (districtsSet.isEmpty()) {
            sendDistrictsToBot();
            lastCommand = START;
        } else {
            sendDistrictsSubscriptionToBot();
            lastCommand = SHOW_DISTRICTS;
        }
    }

    private void proceedChangeDistrict(String text) {
        if (districtsSet.contains(text)) {
            districtsSet.remove(text);
            sendMessageToBot(YOU_UNSUBSCRIBE_SUCCESSFULLY);
        } else {
            districtsSet.add(text);
            sendMessageToBot(YOU_SUBSCRIBE_SUCCESSFULLY);
        }
        writeDataToJsonWithIdFile();
        sendDistrictsSubscriptionToBot();
        sendDistrictsToBot();
    }

    private void proceedChooseAllDistricts() {
        districtsSet.addAll(districts);
        sendDistrictsSubscriptionToBot();
        writeDataToJsonWithIdFile();
        lastCommand = CHOOSE_ALL_DISTRICTS;
    }

    private void proceedRemoveAllDistricts() {
        districtsSet.removeAll(districts);
        sendDistrictsSubscriptionToBot();
        sendDistrictsToBot();
        writeDataToJsonWithIdFile();
        lastCommand = CHANGE_DISTRICTS;
    }

    private void proceedStopCommand() {
        if (idsMap.containsKey(chatId) && !idsMap.get(chatId)) {
            sendMessageToBot(BOT_ALREADY_STOPPED);
            sendMessageToBot(INFO_START);
            return;
        } else {
            sendMessageToBot(BOT_STOPPED);
            sendMessageToBot(INFO_START);
        }

        idsMap.put(chatId, false);
        writeDataToJsonWithIdFile();
        writeDataToFile();
    }

    protected void sendMessageToBot(String text) {
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage(text);
    }

    protected void sendDistrictsToBot() {
        List<KeyboardRow> keyboardRowList = new LinkedList<>();
        List<KeyboardRow> keyboardRowListAdd = new LinkedList<>();
        keyboardRowList.add(
                new KeyboardRow(List.of(
                        new KeyboardButton(END_OF_CHOOSE),
                        new KeyboardButton(REMOVE_ALL_DISTRICTS),
                        new KeyboardButton(CHOOSE_ALL_DISTRICTS)))
        );
        districts.forEach(keyboardRow -> {
            String append;
            if (districtsSet.contains(keyboardRow)) {
                append = BUTTON_DELETE;
                keyboardRowList.add(new KeyboardRow(Collections.singletonList(new KeyboardButton(keyboardRow + append))));
            } else {
                append = BUTTON_ADD;
                keyboardRowListAdd.add(new KeyboardRow(Collections.singletonList(new KeyboardButton(keyboardRow + append))));
            }
        });
        keyboardRowList.addAll(keyboardRowListAdd);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        sendMessage(CHOOSE_CONVENIENT_DISTRICTS);
    }

    private void sendDistrictsSubscriptionToBot() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(YOUR_DISTRICTS).append(System.lineSeparator());

        if (districtsSet.isEmpty()){
            stringBuilder.append(EMPTY).append(System.lineSeparator());
        } else {
            int i = 1;
            for (String value : districtsSet){
                stringBuilder.append(i++).append(DOT_AND_SPACE).append(value).append(System.lineSeparator());
            }
        }

        sendMessageToBot(stringBuilder.toString());
    }

    private void sendMessage(String text) {
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.log(Level.WARNING, "MyBot Error 6: {0}", e.getMessage());
        }
    }

    private boolean createDataJsonWithIdFile() {
        sendMessage.setChatId(chatId);

        File file;
        String fileName = String.format(DATA_JSON, chatId);
        try {
            file = findOrCreateFile(fileName);
        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("MyBot Error 5: Error reading %s: %s", fileName, e.getMessage()));
            return false;
        }
        if (!file.exists()) {
            return false;
        }

        this.dataJsonWithIdFile = file;
        this.districtsSet = new TreeSet<>();

        return getDataFromJsonFile(true);
    }

    protected boolean getDataFromJsonFile(boolean isWithId) {
        File file = this.dataJsonFile;
        if (isWithId) {
            file = this.dataJsonWithIdFile;
        }

        logger.log(Level.INFO, GET_DATA_FROM, file);
        try {
            JSONObject dataJson = getJSONObjectFromFile(file);
            if (dataJson == null) {
                logger.log(Level.WARNING, "MyBot Error 7: JsonObject dataJson null");
                return false;
            }

            if (isWithId) {
                getDataFromJsonWithIdFile(dataJson);
            } else {
                getDataFromJsonFile(dataJson);
            }

            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "MyBot Error 8: {0}", e.getMessage());
            return false;
        }
    }

    private void getDataFromJsonWithIdFile(JSONObject dataJsonWithId) throws JsonProcessingException {
        DataWithIdJson data = new ObjectMapper().readValue(dataJsonWithId.toString(), DataWithIdJson.class);

        Set<String> set = new TreeSet<>();
        if (data.hasDistrictsSet()) {
            set.addAll(data.getDistrictsSet());
        }

        this.lastDay = data.getLastDay();
        this.userName = data.getUserName();
        this.districtsSet = set;
    }

    private void getDataFromJsonFile(JSONObject dataJson) throws JsonProcessingException {
        DataJson data = new ObjectMapper().readValue(dataJson.toString(), DataJson.class);

        Map<Long, Boolean> idsMapFromJson = new HashMap<>();
        if (data.hasIdsSet()) {
            idsMapFromJson.putAll(data.getIds());
        }

        this.idsMap = idsMapFromJson;
    }

    private JSONObject getJSONObjectFromFile(File file) throws FileNotFoundException {
        Scanner myJson = new Scanner(file);
        JSONObject dataJson;
        try {
            if (!myJson.hasNext()) {
                logger.log(Level.INFO, "Empty json file: {0}", file);
                if (file.equals(dataJsonFile)) {
                    writeDataToFile();
                } else {
                    writeDataToJsonWithIdFile();
                }

                myJson = new Scanner(file);
                if (!myJson.hasNext()) {
                    logger.log(Level.WARNING, "MyBot Error 9: Json file still empty: {0}", file);
                    myJson.close();
                    return null;
                }
            }

            String str = myJson.useDelimiter("\\Z").next();
            dataJson = new JSONObject(str);
        } finally {
            myJson.close();
        }
        return dataJson;
    }

    private void writeDataToFile() {
        writeDataToFile(new JSONObject().put(DATA_JSON_DECLARED_FIELDS[0].getName(), idsMap), dataJsonFile);
    }

    private void writeDataToJsonWithIdFile() {
        writeDataToFile(new JSONObject()
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[0].getName(), LocalDate.now())
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[1].getName(), userName)
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[2].getName(), districtsSet)
                , dataJsonWithIdFile);
    }

    private void writeDataToFile(JSONObject jsonObject, File dataJsonFile) {
        try (PrintWriter out = new PrintWriter(dataJsonFile)) {
            out.write(jsonObject.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "MyBot Error 10: {0}", e.getMessage());
        }
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    public boolean isGlobalError() {
        return globalError;
    }
}
