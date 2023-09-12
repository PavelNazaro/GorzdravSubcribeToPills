package my.projects.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

import static my.projects.java.Main.DATA_JSON;
import static my.projects.java.Main.findOrCreateFile;

public class MyBot extends TelegramLongPollingBot {
    private final Logger logger;
    private final String botToken;
    private final String botUsername;
    private final String pathToJsonFromWeb;
    private final File dataJsonFile;
    private File dataJsonWithIdFile;
    private Map<Long, Boolean> idsMap;
    private long chatId;
    private String lastDay;
    private Map<String, Map<String, ArrayList<DistrictsDTO>>> lastMap;
    private Map<String, String> subscriptionMap;// drugName/benefit
    private String userName;
    private Set<String> districtsSet;
    private String lastCommand;
    private LastUserChoose lastUserChoose;
    private boolean globalError = false;
    private final SendMessage sendMessage = new SendMessage();

    private static final Field[] DATA_WITH_ID_JSON_DECLARED_FIELDS = DataWithIdJson.class.getDeclaredFields();
    private static final Field[] DATA_JSON_DECLARED_FIELDS = DataJson.class.getDeclaredFields();

    private static final String START = "/start";
    private static final String STOP = "/stop";
    private static final String MY_SUBSCRIPTIONS = "/my_subscriptions";
    private static final String CHANGE_DISTRICTS = "/change_districts";
    private static final String FIND_DRUGS = "/find_drugs";
    private static final String PROCEED_USER_CHOOSE_FIND_DRUGS = "/proceed_user_choose_find_drugs";
    private static final String PROCEED_USER_CHOOSE_BENEFITS = "/proceed_user_choose_benefits";
    private static final String PROCEED_JSON_PARSER = "/proceed_json_parser";

    private static final String SPACE = StringUtils.SPACE;
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String FINDING_DRUG = "Поиск лекарства...";
    private static final String SUBSCRIBE = "Подписаться";
    private static final String EXIT_TO_MENU = "Выйти в меню";
    private static final String GET_DATA_FROM = "Get data from: {0}";
    private static final String BOT_S_STARTED = "Бот%s запущен!";
    private static final String BOT_S_STOPPED = "Бот%s остановлен!";
    private static final String ALREADY = SPACE + "уже";
    private static final String BOT_STARTED = String.format(BOT_S_STARTED, StringUtils.EMPTY);
    private static final String BOT_STOPPED = String.format(BOT_S_STOPPED, StringUtils.EMPTY);
    private static final String BOT_ALREADY_STARTED = String.format(BOT_STARTED, ALREADY);
    private static final String BOT_ALREADY_STOPPED = String.format(BOT_STOPPED, ALREADY);
    private static final String END_OF_CHOOSE = "Завершить выбор";
    private static final String CHOOSE_A_DRUG = "Выберите лекарство:";
    private static final String EMPTY_OR_YOU_ALREADY_HAVE_IT = "Пусто! Либо то, что вы ищите уже у вас в подписках";
    private static final String YOU_SUBSCRIBE_SUCCESSFULLY = "Вы успешно подписались!";
    private static final String YOU_UNSUBSCRIBE_SUCCESSFULLY = "Вы успешно отписались!";
    private static final String YOUR_DISTRICTS = "Ваши районы:";
    private static final String YOUR_SUBSCRIPTIONS = "Ваши подписки:";
    private static final String CHOOSE_CONVENIENT_DISTRICTS = "Выберите удобные для Вас районы:";
    private static final String YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM = "Вам необходимо выбрать как минимум 1 район!";
    private static final String CHOOSE_ALL_DISTRICTS = "Выбрать все";
    private static final String REMOVE_ALL_DISTRICTS = "Удалить все";
    private static final String EMPTY = "Пусто";
    private static final String DOT = ".";
    private static final String DOT_AND_SPACE = DOT + SPACE;
    private static final String INFO_WITH_LINE_SEPARATOR = "---------------ИНФО---------------" + LINE_SEPARATOR;
    private static final String MENU = "MENU";
    private static final String INFO_MESSAGE = INFO_WITH_LINE_SEPARATOR + "Нажмите " + MENU + " чтобы посмотреть все команды";
    private static final String INFO_START = INFO_WITH_LINE_SEPARATOR + String.format("Выберите в %s %s чтобы запустить бот", MENU, START);
    private static final String INFO_FIND_DRUGS = INFO_WITH_LINE_SEPARATOR + String.format("Выберите в %s %s для поиска лекарства", MENU, FIND_DRUGS);
    private static final String WRONG_COMMAND = "Неверная команда!" + LINE_SEPARATOR;
    private static final String ERROR_IN_CHOOSE = "Ошибка в выборе!";
    private static final String ERROR_IN_CHOOSE_WITH_CAUSE = ERROR_IN_CHOOSE + " Причина: ";
    private static final String ALREADY_EXIST = "Уже есть";
    private static final String CHOOSE_YOUR_BENEFITS = "Выберите вашу льготу:";
    private static final String WRITE_DRUG_NAME = "Напишите название лекарства для поиска: (Например: Юперио)";
    private static final String DRUG_FOUND = "Найдено лекарство:";
    private static final String THERE_ARE_NO_DRUGS = "В выбранных вами районах сейчас нет такого лекарства по такой льготе!";
    private static final String YOU_COULD_SUBSCRIBE = "Вы можете подписаться и получать уведомления о наличии:";
    private static final String DRUG_PRINT = "Лекарство: ";
    private static final String BENEFIT_PRINT = "Льгота: ";
    private static final String INFO = "Информация";
    private static final String WRONG_NAME = "Неправильное название";
    private static final String PHARMACIES_AND_AVAILABILITY = "Аптеки и наличие в ваших выбранных районах:";
    private static final String NOTHING_FOUND_SEND_ANOTHER_DRUG_NAME = "Ничего не найдено! Введите другое название:";
    private static final String ERROR_IN_CONNECTION_TRY_AGAIN_LATER = "Ошибка в подключении: Возможно сервер лекарст не отвечает. Попробуйте попозже";
    protected static final String INFO_BEFORE_VISIT = "На момент обращения в аптеку не гарантируется наличие лекарственного препарата к выдаче, в связи с ограничением количества препарата в аптеке. Информацию о наличии препарата необходимо уточнить по телефону";
    protected static final String UNSUCCESSFUL_RESULT = "Unsuccessful result";
    protected static final String ERROR_IN_FIND_DRUGS = "Error in find drugs";
    protected static final String RESPONSE_DTO_IS_NULL = "ResponseDTO is null";
    private static final String MY_BOT_ERROR = "MyBot Error {0}";
    private static final String DISTRICT = SPACE + "район";
    private static final List<String> districts = new ArrayList<>(List.of("Адмиралтейский" + DISTRICT, "Василеостровский" + DISTRICT, "Выборгский" + DISTRICT, "Калининский" + DISTRICT, "Кировский" + DISTRICT, "Колпинский" + DISTRICT, "Красногвардейский" + DISTRICT, "Красносельский" + DISTRICT, "Кронштадтcкий" + DISTRICT, "Курортный" + DISTRICT, "Московский" + DISTRICT, "Невский" + DISTRICT, "Петроградский" + DISTRICT, "Петродворцовый" + DISTRICT, "Приморский" + DISTRICT, "Пушкинский" + DISTRICT, "Фрунзенский" + DISTRICT, "Центральный" + DISTRICT));
    private static final String BENEFIT = SPACE + "льгота";
    private final List<String> benefits = new ArrayList<>(List.of("Федеральная" + BENEFIT, "Региональная" + BENEFIT, "Психиатрическая" + BENEFIT, "ВЗН" + BENEFIT));
    private static final String ROUND_BRACKET_OPEN = "(";
    private static final String ROUND_BRACKET_CLOSE = ")";
    private static final String BUTTON_ADD = ROUND_BRACKET_OPEN + "Добавить" + ROUND_BRACKET_CLOSE;
    private static final String BUTTON_DELETE = ROUND_BRACKET_OPEN + "Удалить" + ROUND_BRACKET_CLOSE;
    private static final List<List<String>> SUBSCRIBE_AND_EXIT_BUTTONS = List.of(List.of(SUBSCRIBE, EXIT_TO_MENU));

    private enum SubscriptionsEnum {
        ALL, SUBSCRIPTIONS_ONLY, DISTRICTS_ONLY;

        boolean isSubscriptionOnly(SubscriptionsEnum subscriptionsEnum) {
            return subscriptionsEnum.equals(SUBSCRIPTIONS_ONLY);
        }

        boolean isDistrictsOnly(SubscriptionsEnum subscriptionsEnum) {
            return subscriptionsEnum.equals(DISTRICTS_ONLY);
        }
    }

    public MyBot(String botToken, String botUsername, String pathToJsonFromWeb, File dataJsonFile, Logger logger) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.pathToJsonFromWeb = pathToJsonFromWeb;
        this.dataJsonFile = dataJsonFile;
        this.logger = logger;
        this.lastCommand = StringUtils.EMPTY;

        long startTime = System.nanoTime();

        if (!getDataFromJsonFile(false)) {
            logger.log(Level.WARNING, MY_BOT_ERROR, " 1: Error in create dataJson file!");
            globalError = true;
        }

        logger.log(Level.INFO, "Start duration, ms: {0}", (System.nanoTime() - startTime) / 1000000);
    }

    public void onUpdateReceived(Update update) {
        if (globalError) {
            logger.log(Level.WARNING, MY_BOT_ERROR, " 2: Global error!");
            return;
        }

        long startTime = System.nanoTime();

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (!message.hasText()) {
                logger.log(Level.WARNING, MY_BOT_ERROR, " 3: Message is EMPTY!");
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
            logger.log(Level.WARNING, MY_BOT_ERROR, " 4: Message is EMPTY!");
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
            editedText = text.substring(0, text.lastIndexOf(SPACE + ROUND_BRACKET_OPEN));
        }
        if (text.equals(START)) {
            proceedStartCommand();
            return;
        }
        if (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS)) {
            if (StringUtils.isNotEmpty(editedText) && districts.contains(editedText)) {
                proceedChangeDistrict(editedText);
                return;
            }
            if (districts.contains(text)) {
                proceedChangeDistrict(text);
                return;
            }
        }
        if (text.equals(MY_SUBSCRIPTIONS)) {
            sendSubscriptions(SubscriptionsEnum.ALL);
            lastCommand = MY_SUBSCRIPTIONS;
            return;
        }
        if (text.equals(CHANGE_DISTRICTS)) {
            sendSubscriptions(SubscriptionsEnum.DISTRICTS_ONLY);
            sendDistrictsToBot();
            lastCommand = CHANGE_DISTRICTS;
            return;
        }
        if (text.equals(FIND_DRUGS)) {
            if (isDistrictsSetEmpty()) {
                lastCommand = CHANGE_DISTRICTS;
                return;
            }

            sendMessageToBot(WRITE_DRUG_NAME);
            lastCommand = FIND_DRUGS;
            return;
        }
        if (text.equals(END_OF_CHOOSE)) {
            if (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS)) {
                if (isDistrictsSetEmpty()) {
                    return;
                }
                sendSubscriptions(SubscriptionsEnum.ALL);

                if (lastCommand.equals(START)) {
                    sendMessageToBot(INFO_FIND_DRUGS);
                }
                lastCommand = END_OF_CHOOSE;
                return;
            }
            if (lastCommand.equals(PROCEED_JSON_PARSER)) {
                sendSubscriptions(SubscriptionsEnum.ALL);
                lastCommand = END_OF_CHOOSE;
                return;
            }

            sendMessageToBot(INFO_MESSAGE);
            lastCommand = END_OF_CHOOSE;
            return;
        }
        if (text.equals(EXIT_TO_MENU)) {
            sendMessageToBot(INFO_MESSAGE);
            lastCommand = EXIT_TO_MENU;
            return;
        }
        if (text.equals(CHOOSE_ALL_DISTRICTS) && (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS))) {
            proceedChooseAllDistricts();
            return;
        }
        if (text.equals(REMOVE_ALL_DISTRICTS) && (lastCommand.equals(START) || lastCommand.equals(CHANGE_DISTRICTS))) {
            proceedRemoveAllDistricts();
            return;
        }
        if (lastCommand.equals(PROCEED_JSON_PARSER)) {
            proceedJsonParser(text);
            return;
        }
        if (lastCommand.equals(PROCEED_USER_CHOOSE_FIND_DRUGS)) {
            if (benefits.contains(text)) {
                proceedUserChooseBenefits(text);
            } else {
                sendMessageToBot(ERROR_IN_CHOOSE_WITH_CAUSE + WRONG_NAME);
                sendFoundedDrugsToBot();
            }
            return;
        }
        if (lastCommand.equals(PROCEED_USER_CHOOSE_BENEFITS)) {
            if (text.equals(SUBSCRIBE)) {
                subscribeToDrug();
                lastCommand = SUBSCRIBE;
                return;
            }

            sendMessageToBot(ERROR_IN_CHOOSE);
            sendFoundedDrugsToBot();
            return;
        }
        if (text.equals(STOP)) {
            proceedStopCommand();
            lastCommand = STOP;
            return;
        }

        lastCommand = proceedJsonParserCommand(text);
    }

    private boolean isDistrictsSetEmpty() {
        if (districtsSet.isEmpty()) {
            sendMessageToBot(YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM);
            sendDistrictsToBot();
            return true;
        }
        return false;
    }

    private void proceedStartCommand() {
        if (idsMap.containsKey(chatId) && idsMap.get(chatId)) {
            sendMessageToBot(BOT_ALREADY_STARTED);
        } else {
            sendMessageToBot(BOT_STARTED);
            if (!idsMap.containsKey(chatId)){
                sendMessageToBot("Вас приветствует неоффициальный бот по поиску лекарств от Горздрава!" + LINE_SEPARATOR +
                        LINE_SEPARATOR +
                        "Бот создан не только для поиска лекарств в наличии, а также для подписки на них, если лекарства не оказалось в наличии в аптеках Санкт-Петербурга. Для начала выберите удобный(е) для вас район(ы) в котором бот будет искать наличие лекарств для Вас. Для завершения выбора районов, используйте кнопку с сответствующим названием. Далее вы сможете искать лекарства и их наличие, а также подписаться на уведомления." + LINE_SEPARATOR +
                        LINE_SEPARATOR +
                        "Пожалуйста, пользуйтесь всплывающим блоком кнопок, которые сделаны для Вашего максимального удобства и быстрого управления" + LINE_SEPARATOR +
                        "Бот бесплатен!");
            }
            idsMap.put(chatId, true);
            writeDataToJsonWithIdFile();
            writeDataToFile();
        }

        if (districtsSet.isEmpty()) {
            sendDistrictsToBot();
        } else {
            sendSubscriptions(SubscriptionsEnum.ALL);
            sendMessageToBot(INFO_FIND_DRUGS);
        }
        lastCommand = START;
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
        sendSubscriptions(SubscriptionsEnum.DISTRICTS_ONLY);
        sendDistrictsToBot();
    }

    private void proceedChooseAllDistricts() {
        districtsSet.addAll(districts);
        sendSubscriptions(SubscriptionsEnum.ALL);
        writeDataToJsonWithIdFile();
        lastCommand = CHOOSE_ALL_DISTRICTS;
    }

    private void proceedRemoveAllDistricts() {
        districtsSet.removeAll(districts);
        sendSubscriptions(SubscriptionsEnum.DISTRICTS_ONLY);
        sendDistrictsToBot();
        writeDataToJsonWithIdFile();
        lastCommand = CHANGE_DISTRICTS;
    }

    private boolean subscribeToDrug() {
        subscriptionMap.put(lastUserChoose.getDrugName(), lastUserChoose.getBenefit());
        writeDataToJsonWithIdFile();

        sendMessageToBot(YOU_SUBSCRIBE_SUCCESSFULLY);
        sendSubscriptions(SubscriptionsEnum.SUBSCRIPTIONS_ONLY);
        return true;
    }

    private void proceedJsonParser(String text) {
        if (lastMap.containsKey(text)) {
            if (proceedUserChooseFindDrugs(text)) {
                lastCommand = PROCEED_USER_CHOOSE_FIND_DRUGS;
            }
        } else {
            sendMessageToBot(ERROR_IN_CHOOSE_WITH_CAUSE + WRONG_NAME);
            sendFoundedDrugsToBot();
        }
    }

    private boolean proceedUserChooseFindDrugs(String text) {
        if (subscriptionMap.containsKey(text)) {
            sendMessageToBot(ERROR_IN_CHOOSE_WITH_CAUSE + ALREADY_EXIST);
            sendFoundedDrugsToBot();
            return false;
        }

        lastUserChoose = new LastUserChoose(text);
        sendReplyKeyboardToBot(CHOOSE_YOUR_BENEFITS, swapOneRowToRows(benefits));
        return true;
    }

    private void proceedUserChooseBenefits(String benefit) {
        lastUserChoose.setBenefit(benefit);
        List<String> messagesList = getMessagesListOfDrugsMoreThanZero(lastMap.get(lastUserChoose.getDrugName()), benefit);
        if (messagesList.isEmpty()) {
            sendMessageToBot(DRUG_PRINT + lastUserChoose.getDrugName() + LINE_SEPARATOR
                    + BENEFIT_PRINT + benefit + LINE_SEPARATOR
                    + LINE_SEPARATOR
                    + PHARMACIES_AND_AVAILABILITY);
            sendMessageToBot(THERE_ARE_NO_DRUGS);
        } else {
            sendMessageToBot(DRUG_FOUND + LINE_SEPARATOR
                    + DRUG_PRINT + lastUserChoose.getDrugName() + LINE_SEPARATOR
                    + BENEFIT_PRINT + benefit + LINE_SEPARATOR
                    + LINE_SEPARATOR
                    + INFO + ": " + INFO_BEFORE_VISIT + "!" + LINE_SEPARATOR
                    + LINE_SEPARATOR
                    + PHARMACIES_AND_AVAILABILITY);
            messagesList.forEach(this::sendMessageToBot);
        }

        sendReplyKeyboardToBot(YOU_COULD_SUBSCRIBE, SUBSCRIBE_AND_EXIT_BUTTONS);
        lastCommand = PROCEED_USER_CHOOSE_BENEFITS;
    }

    private List<String> getMessagesListOfDrugsMoreThanZero(Map<String, ArrayList<DistrictsDTO>> districtsMapOfChoseDrug, String benefitName) {
        List<String> messagesList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<DistrictsDTO>> entry : districtsMapOfChoseDrug.entrySet()) {
            String district = entry.getKey();
            ArrayList<DistrictsDTO> districtsDTOS = entry.getValue();
            if (districtsSet.contains(district)) {
                for (DistrictsDTO districtsDTO : districtsDTOS) {
                    Benefit benefit = districtsDTO.getBenefit(benefitName);
                    if (benefit.getCount() > 0) {
                        messagesList.add(districtsDTO.createMessage(benefit));
                    }
                }
            }
        }
        return messagesList;
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

    private String proceedJsonParserCommand(String text) {
        lastMap = getLastMapByDrugInWeb(text);
        if (lastMap.isEmpty() || lastMap.containsKey(ERROR_IN_FIND_DRUGS) || lastMap.containsKey(RESPONSE_DTO_IS_NULL)) {
            if (lastCommand.equals(FIND_DRUGS)) {
                sendMessageToBot(NOTHING_FOUND_SEND_ANOTHER_DRUG_NAME);
                return lastCommand;
            }

            sendMessageToBot(WRONG_COMMAND + INFO_MESSAGE);
            return text;
        }
        if (lastMap.containsKey(UNSUCCESSFUL_RESULT)) {
            if (lastCommand.equals(FIND_DRUGS)) {
                sendMessageToBot(ERROR_IN_CONNECTION_TRY_AGAIN_LATER);
                return lastCommand;
            }

            sendMessageToBot(WRONG_COMMAND + INFO_MESSAGE);
            return text;
        }

        sendMessageToBot(FINDING_DRUG);
        if(sendFoundedDrugsToBot()) {
            return PROCEED_JSON_PARSER;
        }
        return text;
    }

    private Map<String, Map<String, ArrayList<DistrictsDTO>>> getLastMapByDrugInWeb(String drug) {
        return new JsonWebParser(logger).findDrugFromWeb(this, drug);
    }

    private boolean sendFoundedDrugsToBot() {
        List<String> drugsToSend = new ArrayList<>();
        drugsToSend.add(END_OF_CHOOSE);
        for (Map.Entry<String, Map<String, ArrayList<DistrictsDTO>>> entry : lastMap.entrySet()) {
            drugsToSend.add(entry.getKey());
        }

        if (drugsToSend.size() > 1) {
            sendSubscriptions(SubscriptionsEnum.SUBSCRIPTIONS_ONLY);
            sendReplyKeyboardToBot(CHOOSE_A_DRUG, swapOneRowToRows(drugsToSend));
        } else {
            sendMessageToBot(EMPTY_OR_YOU_ALREADY_HAVE_IT);
            sendSubscriptions(SubscriptionsEnum.SUBSCRIPTIONS_ONLY);
            lastCommand = EMPTY_OR_YOU_ALREADY_HAVE_IT;
            return false;
        }
        return true;
    }

    protected void sendMessageToBot(String text) {
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage(text);
    }

    protected void sendDistrictsToBot() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(END_OF_CHOOSE, REMOVE_ALL_DISTRICTS, CHOOSE_ALL_DISTRICTS));

        List<String> districtsButtonsDelete = new ArrayList<>();
        List<String> districtsButtonsAdd = new ArrayList<>();
        for (String districtName : districts) {
            if (districtsSet.contains(districtName)) {
                districtsButtonsDelete.add(districtName + SPACE + BUTTON_DELETE);
            } else {
                districtsButtonsAdd.add(districtName + SPACE + BUTTON_ADD);
            }
        }

        rows.addAll(swapOneRowToRows(districtsButtonsDelete));
        rows.addAll(swapOneRowToRows(districtsButtonsAdd));

        sendReplyKeyboardToBot(CHOOSE_CONVENIENT_DISTRICTS, rows);
    }

    private void sendSubscriptions(SubscriptionsEnum sEnum) {
        StringBuilder stringBuilder = new StringBuilder();
        addSetToStringBuilder(stringBuilder, YOUR_DISTRICTS + LINE_SEPARATOR, districtsSet);
        if (sEnum.isDistrictsOnly(sEnum)) {
            sendMessageToBot(stringBuilder.toString());
        } else {
            if (sEnum.isSubscriptionOnly(sEnum)) {
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(LINE_SEPARATOR);
            }
            addSetToStringBuilder(stringBuilder, YOUR_SUBSCRIPTIONS + LINE_SEPARATOR, subscriptionMap);

            if (!sEnum.isSubscriptionOnly(sEnum)) {
                stringBuilder.append(LINE_SEPARATOR).append(INFO_MESSAGE);
            }
            sendMessageToBot(stringBuilder.toString());
        }
    }

    private void addSetToStringBuilder(StringBuilder stringBuilder, String str, Object object) {
        stringBuilder.append(str);
        if ((object instanceof Set && ((Set<?>) object).isEmpty()) || (object instanceof Map && ((Map<?, ?>) object).isEmpty())) {
            stringBuilder.append(EMPTY).append(LINE_SEPARATOR);
        } else {
            int i = 1;
            if (object instanceof Set) {
                for (String value : (Set<String>) object) {
                    stringBuilder.append(i++).append(DOT_AND_SPACE).append(value).append(LINE_SEPARATOR);
                }
            }
            if (object instanceof Map) {
                for (String value : ((Map<String, String>) object).keySet()) {
                    stringBuilder.append(i++).append(DOT_AND_SPACE).append(value).append(ROUND_BRACKET_OPEN).append(((Map<String, String>) object).get(value)).append(ROUND_BRACKET_CLOSE).append(LINE_SEPARATOR);
                }
            }
        }
    }

    private List<List<String>> swapOneRowToRows(List<String> buttonNamesList) {
        List<List<String>> rows = new ArrayList<>();

        for (String buttonName : buttonNamesList){
            rows.add(List.of(buttonName));
        }

        return rows;
    }

    private void sendReplyKeyboardToBot(String text, List<List<String>> buttonNamesList) {
        List<KeyboardRow> rows = new ArrayList<>();

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        for (List<String> buttonNames : buttonNamesList) {
            KeyboardRow row = new KeyboardRow();
            for (String buttonName : buttonNames) {
                row.add(new KeyboardButton(buttonName));
            }
            rows.add(row);
        }

        replyKeyboardMarkup.setKeyboard(rows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(text);
    }

    private void sendInlineKeyboardToBot(String text, List<List<String>> buttonNamesList) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        for (List<String> buttonNames : buttonNamesList) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String buttonName : buttonNames) {
                InlineKeyboardButton button = new InlineKeyboardButton(buttonName);
                button.setCallbackData(buttonName); // This is the data sent when the button is clicked
                row.add(button);
            }
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(text);
    }

    private void sendMessage(String text) {
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.log(Level.WARNING, MY_BOT_ERROR, " 6: " + e.getMessage());
        }
    }

    private boolean createDataJsonWithIdFile() {
        sendMessage.setChatId(chatId);

        File file;
        String fileName = String.format(DATA_JSON, chatId);
        try {
            file = findOrCreateFile(fileName);
        } catch (IOException e) {
            logger.log(Level.WARNING, MY_BOT_ERROR, String.format(" 5: Error reading %s: %s", fileName, e.getMessage()));
            return false;
        }
        if (!file.exists()) {
            return false;
        }

        this.dataJsonWithIdFile = file;
        this.districtsSet = new TreeSet<>();
        this.subscriptionMap = new HashMap<>();

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
                logger.log(Level.WARNING, MY_BOT_ERROR, " 7: JsonObject dataJson null");
                return false;
            }

            if (isWithId) {
                getDataFromJsonWithIdFile(dataJson);
            } else {
                getDataFromJsonFile(dataJson);
            }

            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, MY_BOT_ERROR, " 8: " + e.getMessage());
            return false;
        }
    }

    private void getDataFromJsonWithIdFile(JSONObject dataJsonWithId) throws JsonProcessingException {
        DataWithIdJson data = new ObjectMapper().readValue(dataJsonWithId.toString(), DataWithIdJson.class);

        Set<String> set = new TreeSet<>();
        if (data.hasDistrictsSet()) {
            set.addAll(data.getDistrictsSet());
        }
        Map<String, String> map = new HashMap<>();
        if (data.hasSubscriptionMap()) {
            map.putAll(data.getSubscriptionMap());
        }

        this.lastDay = data.getLastDay();
        this.userName = data.getUserName();
        this.districtsSet = set;
        this.subscriptionMap = map;
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
                    logger.log(Level.WARNING, MY_BOT_ERROR, " 9: Json file still empty: " + file);
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
        int i = 0;
        writeDataToFile(new JSONObject()
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[i++].getName(), LocalDate.now())
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[i++].getName(), userName)
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[i++].getName(), districtsSet)
                        .put(DATA_WITH_ID_JSON_DECLARED_FIELDS[i++].getName(), subscriptionMap)
                , dataJsonWithIdFile);
    }

    private void writeDataToFile(JSONObject jsonObject, File dataJsonFile) {
        try (PrintWriter out = new PrintWriter(dataJsonFile)) {
            out.write(jsonObject.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, MY_BOT_ERROR, " 10: " + e.getMessage());
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

    public List<String> getBenefits() {
        return benefits;
    }

    public String getPathToJsonFromWeb() {
        return pathToJsonFromWeb;
    }
}
