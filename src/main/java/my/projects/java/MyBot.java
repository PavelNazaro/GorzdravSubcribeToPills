package my.projects.java;

import org.apache.commons.lang3.StringUtils;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static my.projects.java.Main.printToLog;
import static my.projects.java.Main.shutdownJar;

public class MyBot extends TelegramLongPollingBot {
    private final PropertiesDTO propertiesDTO;
    private final ConnectionsToDB connectionsToDB;
    private long userId;
    private Map<String, Map<String, ArrayList<DistrictsDTO>>> lastMap;
    private Map<Integer, String> benefitsMap;
    private Map<Integer, String> districtsMap;
    private String userName;
    private String lastCommand;
    private LastUserChoose lastUserChoose;
    private final SendMessage sendMessage = new SendMessage();
    private Map<Long, Boolean> usersTableFromDB;
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER_FOR_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String START = "/start";
    private static final String STOP = "/stop";
    private static final String STOP_BOT = "/stop_bot";
    private static final String MY_SUBSCRIPTIONS = "/my_subscriptions";
    private static final String CHANGE_DISTRICTS = "/change_districts";
    private static final String FIND_DRUGS = "/find_drugs";
    private static final String UNSUBSCRIBE = "/unsubscribe";
    private static final String UNSUBSCRIBE_FROM_ALL = "Отписаться от всего";
    private static final String SEND_MESSAGE_TO_ALL_USERS = "/smtau";
    private static final String SEND_MESSAGE_TO_ALL_USERS_BOT_UPDATED = "/smtaubu";
    private static final String PROCEED_USER_CHOOSE_FIND_DRUGS = "/proceed_user_choose_find_drugs";
    private static final String PROCEED_USER_CHOOSE_BENEFITS = "/proceed_user_choose_benefits";
    private static final String PROCEED_JSON_PARSER = "/proceed_json_parser";

    private static final String SPACE = StringUtils.SPACE;
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String FINDING_DRUG = "Поиск лекарства...";
    private static final String SUBSCRIBE = "Подписаться";
    private static final String EXIT_TO_MENU = "Выйти в меню";
    private static final String BOT_S_STARTED = "Бот%s запущен!";
    private static final String BOT_S_STOPPED = "Бот%s остановлен!";
    private static final String ALREADY = SPACE + "уже";
    private static final String BOT_STARTED = String.format(BOT_S_STARTED, StringUtils.EMPTY);
    private static final String BOT_STOPPED = String.format(BOT_S_STOPPED, StringUtils.EMPTY);
    private static final String BOT_ALREADY_STARTED = String.format(BOT_S_STARTED, ALREADY);
    private static final String BOT_ALREADY_STOPPED = String.format(BOT_S_STOPPED, ALREADY);
    private static final String END_OF_CHOOSE = "Завершить выбор";
    private static final String CHOOSE_A_DRUG = "Выберите лекарство:";
    private static final String EMPTY_OR_YOU_ALREADY_HAVE_IT = "Пусто! Либо то, что вы ищите уже у вас в подписках";
    private static final String YOU_SUBSCRIBE_SUCCESSFULLY = "Вы успешно подписались!";
    private static final String YOU_SUCCESSFULLY_ADDED_DISTRICT = "Вы успешно добавили район!";
    private static final String YOU_UNSUBSCRIBE_SUCCESSFULLY = "Вы успешно отписались!";
    private static final String YOU_SUCCESSFULLY_REMOVED_DISTRICT = "Вы успешно удалили район!";
    private static final String SEND_MESSAGE_THAT_WILL_SEND_TO_ALL_USERS = "Введите сообщение, которое отправится всем пользователям:";
    private static final String YOU_ARE_NOT_HAVE_SUBSCRIPTIONS = "У вас нет подписок!";
    private static final String CHOOSE_WHAT_YOU_WANT_TO_UNSUBSCRIBE = "Выберите от чего Вы хотите отписаться:";
    private static final String YOUR_DISTRICTS = "Ваши районы:";
    private static final String YOUR_SUBSCRIPTIONS = "Ваши подписки:";
    private static final String CHOOSE_CONVENIENT_DISTRICTS = "Выберите удобные для Вас районы:";
    private static final String YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM = "Вам необходимо выбрать как минимум 1 район!";
    private static final String CHOOSE_ALL_DISTRICTS = "Выбрать все";
    private static final String REMOVE_ALL_DISTRICTS = "Удалить все";
    private static final String EMPTY = "Пусто";
    private static final String DOT = ".";
    private static final String DOT_AND_SPACE = DOT + SPACE;
    private static final String TEXT_HIGHLIGHTING = "---------- %s ----------";
    private static final String INFO_WITH_LINE_SEPARATOR = String.format(TEXT_HIGHLIGHTING, "ИНФО") + LINE_SEPARATOR;
    private static final String MENU = "MENU";
    private static final String INFO_MESSAGE = INFO_WITH_LINE_SEPARATOR + "Нажмите " + MENU + " чтобы посмотреть все команды";
    private static final String INFO_START = INFO_WITH_LINE_SEPARATOR + String.format("Выберите в %s %s чтобы запустить бот", MENU, START);
    private static final String INFO_FIND_DRUGS = INFO_WITH_LINE_SEPARATOR + String.format("Выберите в %s %s для поиска лекарства", MENU, FIND_DRUGS);
    private static final String WRONG_COMMAND = "Неверная команда!" + LINE_SEPARATOR;
    private static final String ERROR_IN_CHOOSE = "Ошибка в выборе!";
    private static final String ERROR_IN_CHOOSE_WITH_CAUSE = ERROR_IN_CHOOSE + " Причина: ";
    private static final String ERROR_TRY_AGAIN_LATER = "Ошибка! Попробуйте позже";
    private static final String ALREADY_EXIST = "Уже есть";
    private static final String CHOOSE_YOUR_BENEFITS = "Выберите вашу льготу:";
    private static final String WRITE_DRUG_NAME = "Напишите название лекарства для поиска: (Например: Юперио)";
    private static final String DRUG_FOUND = String.format(TEXT_HIGHLIGHTING, "Найдено лекарство:");
    private static final String THERE_ARE_NO_DRUGS = "В выбранных вами районах сейчас нет такого лекарства по такой льготе!";
    private static final String YOU_COULD_SUBSCRIBE = "Вы можете подписаться и получать уведомления о наличии:";
    private static final String DRUG_PRINT = "Лекарство: ";
    private static final String INTERNATIONAL_NAME = "Международное наименование: ";
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
    private static final String MY_BOT_ERROR = "MyBot Error";
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

    public MyBot(ConnectionsToDB connectionsToDB, PropertiesDTO propertiesDTO, long startTime) {
        this.propertiesDTO = propertiesDTO;
        this.lastCommand = StringUtils.EMPTY;
        this.connectionsToDB = connectionsToDB;
        this.benefitsMap = connectionsToDB.getBenefitsTable();
        this.districtsMap = connectionsToDB.getDistrictsTable();

        printToLog(String.format("Start duration, ms: %s", (System.nanoTime() - startTime) / 1000000));
    }

    public void onUpdateReceived(Update update) {
        long startTime = System.nanoTime();

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (!message.hasText()) {
                printToLog(MY_BOT_ERROR + " 3: Message is EMPTY!");
                return;
            }

            try {
                messageProcessing(message);
            } catch (Exception e) {
                printToLog("Global error: " + e.getMessage());
            }
        }

        printToLog(String.format("Duration, ms: %s", (System.nanoTime() - startTime) / 1000000));
    }

    private void messageProcessing(Message message) {
        userId = message.getChatId();
        userName = message.getChat().getUserName();
        sendMessage.setChatId(userId);
        String text = message.getText();

        if (text.isEmpty()) {
            printToLog(MY_BOT_ERROR + " 4: Message is EMPTY!");
            return;
        }

        printToLog(String.format("User id: %s sent: %s", userId, text));

        if (!connectionsToDB.isConnectionActive() && !connectionsToDB.createConnection()) {
            sendMessageToBot(ERROR_TRY_AGAIN_LATER);
            printToLog("Error: Can not create connection!");
            return;
        }

        usersTableFromDB = connectionsToDB.getIdAndIsAvailableFromUsersTableFromDB();
        if (usersTableFromDB == null) {
            printToLog(MY_BOT_ERROR + " 4: usersTableFromDB is null!");
            return;
        }

        boolean isUsersTableContainsKey = usersTableFromDB.containsKey(userId);
        if (!isUsersTableContainsKey || (isUsersTableContainsKey && !usersTableFromDB.get(userId))) {
            if (!isUsersTableContainsKey) {
                printToLog("Chat id not contains in usersTableFromDB");
            } else {
                printToLog("Bot status: stopped");
            }
            if (!text.equals(START)) {
                text = STOP;
            }
        }

        if (isUsersTableContainsKey && !connectionsToDB.updateLastActionTimeInUsersTableByUserId(userId, getLocalDateTimeNow())) { //to write current date and time to json
            sendMessageToBot(ERROR_TRY_AGAIN_LATER);
            return;
        }

        checkTextIsCommandOrUserChoose(text);

        connectionsToDB.closeConnection();
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
            Set<String> districtsSet = new TreeSet<>(districtsMap.values());
            if (StringUtils.isNotEmpty(editedText) && districtsSet.contains(editedText)) {
                proceedChangeDistrict(editedText);
                return;
            }
            if (districtsSet.contains(text)) {
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

            if (isAdminId()) {
                sendReplyKeyboardToBot(WRITE_DRUG_NAME, List.of(List.of("Юперио", "Салтиказон")));
            } else {
                sendMessageToBot(WRITE_DRUG_NAME);
            }
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
            if (connectionsToDB.getBenefitNamesFromBenefitsTable().contains(text)) {
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
        if (text.equals(STOP_BOT) && isAdminId()) {
            printToLog("Stop command received. Exiting...");
            shutdownJar();
        }
        if (text.equals(STOP)) {
            proceedStopCommand();
            return;
        }
        if (text.equals(UNSUBSCRIBE)) {
            proceedUnsubscribe();
            lastCommand = UNSUBSCRIBE;
            return;
        }
        if (lastCommand.equals(UNSUBSCRIBE) && text.equals(UNSUBSCRIBE_FROM_ALL)) {
            proceedUserChooseUnsubscribe(text, true);
            return;
        }
        if (lastCommand.equals(UNSUBSCRIBE) && connectionsToDB.getSubscriptionsMapByUserId(userId).containsKey(text)) {
            proceedUserChooseUnsubscribe(text, false);
            return;
        }
        if (text.equals(SEND_MESSAGE_TO_ALL_USERS_BOT_UPDATED) && isAdminId()) {
            sendMessageToAllUsers("Бот обновлен! Попробуйте новый функционал");
            lastCommand = SEND_MESSAGE_TO_ALL_USERS_BOT_UPDATED;
            return;
        }
        if (text.equals(SEND_MESSAGE_TO_ALL_USERS) && isAdminId()) {
            sendMessageToBot(SEND_MESSAGE_THAT_WILL_SEND_TO_ALL_USERS);
            lastCommand = SEND_MESSAGE_TO_ALL_USERS;
            return;
        }
        if (lastCommand.equals(SEND_MESSAGE_TO_ALL_USERS) && isAdminId()) {
            sendMessageToAllUsers(text);
            return;
        }

        lastCommand = proceedJsonParserCommand(text);
    }

    private void proceedUnsubscribe() {
        Map<String, String> userSubscriptionsMap = connectionsToDB.getSubscriptionsMapByUserId(userId);
        if (userSubscriptionsMap.isEmpty()) {
            sendMessageToBot(YOU_ARE_NOT_HAVE_SUBSCRIPTIONS);
            return;
        }

        ArrayList<List<String>> unsubscribeList = new ArrayList<>();
        for (String key : userSubscriptionsMap.keySet()) {
            unsubscribeList.add(Collections.singletonList(key));
        }
        if (unsubscribeList.size() >= 2) {
            unsubscribeList.add(Collections.singletonList(UNSUBSCRIBE_FROM_ALL));
        }
        sendReplyKeyboardToBot(CHOOSE_WHAT_YOU_WANT_TO_UNSUBSCRIBE, unsubscribeList);
    }

    private void proceedUserChooseUnsubscribe(String text, boolean isUnsubscribeFromAll) {
        if (isUnsubscribeFromAll) {
            if (!connectionsToDB.removeAllFromUserSubscriptionsTableByUserId(userId)) {
                sendMessageToBot(ERROR_TRY_AGAIN_LATER);
                return;
            }
        } else {
            if (!connectionsToDB.removeFromUserSubscriptionsTableByUserId(userId, text)) {
                sendMessageToBot(ERROR_TRY_AGAIN_LATER);
                return;
            }
        }

        sendMessageToBot(YOU_UNSUBSCRIBE_SUCCESSFULLY);
        sendSubscriptions(SubscriptionsEnum.SUBSCRIPTIONS_ONLY);
    }

    private void proceedStartCommand() {
        boolean isUsersTableContainsKey = usersTableFromDB.containsKey(userId);
        if (isUsersTableContainsKey && Boolean.TRUE.equals(usersTableFromDB.get(userId))) {
            sendMessageToBot(BOT_ALREADY_STARTED);
        } else {
            sendMessageToBot(BOT_STARTED);
            if (!isUsersTableContainsKey) {
                sendMessageToBot("Вас приветствует неоффициальный бот по поиску лекарств от Горздрава!" + LINE_SEPARATOR +
                        LINE_SEPARATOR +
                        "Бот создан не только для поиска лекарств в наличии, а также для подписки на них, если лекарства не оказалось в наличии в аптеках Санкт-Петербурга. Для начала выберите удобный(е) для вас район(ы) в котором бот будет искать наличие лекарств для Вас. Для завершения выбора районов, используйте кнопку с сответствующим названием. Далее вы сможете искать лекарства и их наличие, а также подписаться на уведомления." + LINE_SEPARATOR +
                        LINE_SEPARATOR +
                        "Пожалуйста, пользуйтесь всплывающим блоком кнопок, которые сделаны для Вашего максимального удобства и быстрого управления" + LINE_SEPARATOR +
                        "Бот бесплатен!");
                sendMessageToAdmin(String.format("User %s just joined to %s", userName, propertiesDTO.getBotUsername()));
            }

            connectionsToDB.updateUsersTable(userId, true, getLocalDateTimeNow(), userName);
        }

        if (!isDistrictsSetEmpty()) {
            sendSubscriptions(SubscriptionsEnum.ALL);
            sendMessageToBot(INFO_FIND_DRUGS);
        }
        lastCommand = START;
    }

    private static String getLocalDateTimeNow() {
        return LocalDateTime.now().format(LOCAL_DATE_TIME_FORMATTER_FOR_SQL);
    }

    private void proceedChangeDistrict(String text) {
        if (connectionsToDB.getDistrictsByUserId(userId).contains(text)) {
            if (connectionsToDB.removeDistrictIdFromUserDistrictsTable(userId, text)) {
                sendMessageToBot(YOU_SUCCESSFULLY_REMOVED_DISTRICT);
            } else {
                sendMessageToBot(ERROR_TRY_AGAIN_LATER);
                return;
            }
        } else {
            if (connectionsToDB.addNewToUserDistrictsTable(userId, text)) {
                sendMessageToBot(YOU_SUCCESSFULLY_ADDED_DISTRICT);
            } else {
                sendMessageToBot(ERROR_TRY_AGAIN_LATER);
                return;
            }
        }

        sendSubscriptions(SubscriptionsEnum.DISTRICTS_ONLY);
        sendDistrictsToBot();
    }

    private void proceedChooseAllDistricts() {
        Set<Integer> districtIdsFromDistrictsTable = districtsMap.keySet();
        if (!districtIdsFromDistrictsTable.isEmpty() &&
                connectionsToDB.addAllDistrictsIdFromUserDistrictsTable(districtIdsFromDistrictsTable, userId)) {
            sendSubscriptions(SubscriptionsEnum.ALL);
        } else {
            sendMessageToBot(ERROR_TRY_AGAIN_LATER);
        }

        lastCommand = CHOOSE_ALL_DISTRICTS;
    }

    private void proceedRemoveAllDistricts() {
        if (connectionsToDB.removeAllDistrictsIdFromUserDistrictsTable(userId)) {
            sendSubscriptions(SubscriptionsEnum.DISTRICTS_ONLY);
            sendDistrictsToBot();
        } else {
            sendMessageToBot(ERROR_TRY_AGAIN_LATER);
        }

        lastCommand = CHANGE_DISTRICTS;
    }

    private void subscribeToDrug() {
        if (connectionsToDB.addNewToUserSubscriptionsTable(userId, lastUserChoose.getDrugName(), lastUserChoose.getBenefit())) {
            sendMessageToBot(YOU_SUBSCRIBE_SUCCESSFULLY);
            sendSubscriptions(SubscriptionsEnum.SUBSCRIPTIONS_ONLY);
            return;
        }

        sendMessageToBot(ERROR_TRY_AGAIN_LATER);
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
        if (connectionsToDB.getSubscriptionsMapByUserId(userId).containsKey(text)) {
            sendMessageToBot(ERROR_IN_CHOOSE_WITH_CAUSE + ALREADY_EXIST);
            sendFoundedDrugsToBot();
            return false;
        }

        lastUserChoose = new LastUserChoose(text);
        sendReplyKeyboardToBot(CHOOSE_YOUR_BENEFITS, swapOneRowToRows(List.copyOf(connectionsToDB.getBenefitNamesFromBenefitsTable())));
        return true;
    }

    private void proceedUserChooseBenefits(String benefit) {
        lastUserChoose.setBenefit(benefit);
        Map<String, ArrayList<DistrictsDTO>> districtsMapOfChoseDrug = lastMap.get(lastUserChoose.getDrugName());
        Set<String> districtsByUserId = connectionsToDB.getDistrictsByUserId(userId);
        String internationalName = getInternationalName(districtsByUserId, districtsMapOfChoseDrug);
        List<String> messagesList = getMessagesListOfDrugsMoreThanZero(districtsByUserId, districtsMapOfChoseDrug, benefit);

        sendFoundedDrugsToBot(lastUserChoose.getDrugName(), benefit, internationalName, messagesList, messagesList.isEmpty());

        sendReplyKeyboardToBot(YOU_COULD_SUBSCRIBE, SUBSCRIBE_AND_EXIT_BUTTONS);
        lastCommand = PROCEED_USER_CHOOSE_BENEFITS;
    }

    private List<String> getMessagesListOfDrugsMoreThanZero(Set<String> districtsByUserId, Map<String, ArrayList<DistrictsDTO>> districtsMapOfChoseDrug, String benefitName) {
        List<String> messagesList = new ArrayList<>();
        if (districtsByUserId.isEmpty()) {
            return messagesList;
        }

        for (Map.Entry<String, ArrayList<DistrictsDTO>> entry : districtsMapOfChoseDrug.entrySet()) {
            if (districtsByUserId.contains(entry.getKey())) {
                for (DistrictsDTO districtsDTO : entry.getValue()) {
                    Benefit benefit = districtsDTO.getBenefit(benefitName);
                    if (benefit.getCount() > 0) {
                        messagesList.add(districtsDTO.createMessage(benefit));
                    }
                }
            }
        }
        return messagesList;
    }

    private String getInternationalName(Set<String> districtsByUserId, Map<String, ArrayList<DistrictsDTO>> districtsMapOfChoseDrug) {
        for (Map.Entry<String, ArrayList<DistrictsDTO>> entry : districtsMapOfChoseDrug.entrySet()) {
            if (districtsByUserId.contains(entry.getKey())) {
                for (DistrictsDTO districtsDTO : entry.getValue()) {
                    String mnnName = districtsDTO.getMnnName();
                    if (!mnnName.isEmpty()) {
                        return mnnName;
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private void proceedStopCommand() {
        if (usersTableFromDB.containsKey(userId)) {
            if (Boolean.TRUE.equals(usersTableFromDB.get(userId))) {
                sendMessageToBot(BOT_STOPPED);
            } else {
                sendMessageToBot(BOT_ALREADY_STOPPED);
            }
            sendMessageToBot(INFO_START);
            lastCommand = STOP;
        } else {
            proceedStartCommand();
            return;
        }

        connectionsToDB.updateUsersTable(userId, false, getLocalDateTimeNow(), userName);
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
        if (sendFoundedDrugsToBot()) {
            return PROCEED_JSON_PARSER;
        }
        return text;
    }

    private Map<String, Map<String, ArrayList<DistrictsDTO>>> getLastMapByDrugInWeb(String drug) {
        return new JsonWebParser().findDrugFromWeb(this, drug);
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

    protected void sendDistrictsToBot() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(END_OF_CHOOSE, REMOVE_ALL_DISTRICTS, CHOOSE_ALL_DISTRICTS));

        List<String> districtsButtonsDelete = new ArrayList<>();
        List<String> districtsButtonsAdd = new ArrayList<>();
        Set<String> districtsByUserId = connectionsToDB.getDistrictsByUserId(userId);
        for (String districtName : new TreeSet<>(districtsMap.values())) {
            if (districtsByUserId.contains(districtName)) {
                districtsButtonsDelete.add(districtName + SPACE + BUTTON_DELETE);
            } else {
                districtsButtonsAdd.add(districtName + SPACE + BUTTON_ADD);
            }
        }

        rows.addAll(swapOneRowToRows(districtsButtonsDelete));
        rows.addAll(swapOneRowToRows(districtsButtonsAdd));

        sendReplyKeyboardToBot(CHOOSE_CONVENIENT_DISTRICTS, rows);
    }

    private boolean isDistrictsSetEmpty() {
        if (connectionsToDB.getDistrictsByUserId(userId).isEmpty()) {
            sendMessageToBot(YOU_MUST_CHOOSE_ONE_DISTRICT_MINIMUM);
            sendDistrictsToBot();
            return true;
        }
        return false;
    }

    private void sendSubscriptions(SubscriptionsEnum sEnum) {
        StringBuilder stringBuilder = new StringBuilder();
        if (sEnum.isDistrictsOnly(sEnum)) {
            addSetToStringBuilder(stringBuilder, YOUR_DISTRICTS + LINE_SEPARATOR, connectionsToDB.getDistrictsByUserId(userId));
            sendMessageToBot(stringBuilder.toString());
        } else {
            if (!sEnum.isSubscriptionOnly(sEnum)) {
                addSetToStringBuilder(stringBuilder, YOUR_DISTRICTS + LINE_SEPARATOR, connectionsToDB.getDistrictsByUserId(userId));
                stringBuilder.append(LINE_SEPARATOR);
            }
            addSetToStringBuilder(stringBuilder, YOUR_SUBSCRIPTIONS + LINE_SEPARATOR, connectionsToDB.getSubscriptionsMapByUserId(userId));

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

    protected void getSubscriptions() {
        printToLog("getSubscriptions");

        if (!connectionsToDB.isConnectionActive() && !connectionsToDB.createConnection()) {
            printToLog("Error: Can not create connection!");
            return;
        }

        Map<Long, Boolean> usersTableFromDBLocal = connectionsToDB.getIdAndIsAvailableFromUsersTableFromDB();
        for (Long id : usersTableFromDBLocal.keySet()) {
            if (Boolean.FALSE.equals(usersTableFromDBLocal.get(id))) {
                continue;
            }

            String lastActionTimeFromDB = connectionsToDB.getLastActionTimeInUsersTableByUserId(id);
            if (lastActionTimeFromDB.equals(StringUtils.EMPTY)) {
                printToLog("Error: lastActionTimeFromDB empty!");
                continue;
            }

            TemporalAccessor parsedLastActionTime;
            try {
                parsedLastActionTime = LOCAL_DATE_TIME_FORMATTER_FOR_SQL.parse(lastActionTimeFromDB);
            } catch (DateTimeParseException e) {
                printToLog("Error: in parse lastActionTimeFromDB!");
                continue;
            }

            LocalDateTime dateAndTime;
            try {
                dateAndTime = LocalDateTime.from(parsedLastActionTime);
            } catch (DateTimeParseException e) {
                printToLog("LocalDateTime error: " + e.getMessage());
                try {
                    dateAndTime = LocalDate.from(parsedLastActionTime).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    printToLog("LocalDate error: " + e2.getMessage());
                    continue;
                }
            }

            if (LocalDateTime.now().minusMinutes(4).isBefore(dateAndTime)) {
                printToLog("4 minutes have not passed yet!");
                continue;
            }

            Set<String> districtsByUserId = connectionsToDB.getDistrictsByUserId(id);
            if (districtsByUserId.isEmpty()) {
                printToLog("districtsByUserId empty!");
                continue;
            }

            Map<String, String> userSubscriptionsMap = connectionsToDB.getSubscriptionsMapByUserId(id);
            if (userSubscriptionsMap.isEmpty()) {
                printToLog("subscriptionMap empty!");
                continue;
            }

            int countFounded = 0;
            for (Map.Entry<String, String> entry : userSubscriptionsMap.entrySet()) {
                String drugName = entry.getKey();
                String benefit = entry.getValue();
                Map<String, Map<String, ArrayList<DistrictsDTO>>> lastMapByDrugInWeb = getLastMapByDrugInWeb(drugName);
                if (lastMapByDrugInWeb.isEmpty()) {
                    printToLog("Drug not found in web: " + drugName);
                    continue;
                }
                if (lastMapByDrugInWeb.containsKey(RESPONSE_DTO_IS_NULL) ||
                        lastMapByDrugInWeb.containsKey(ERROR_IN_FIND_DRUGS) ||
                        lastMapByDrugInWeb.containsKey(UNSUCCESSFUL_RESULT)) {
                    printToLog("Smth problem, drug not found in web: " + drugName);
                    return;
                }
                Map<String, ArrayList<DistrictsDTO>> districtsMapFromWeb = lastMapByDrugInWeb.get(drugName);
                String internationalName = getInternationalName(districtsByUserId, districtsMapFromWeb);
                List<String> messagesList = getMessagesListOfDrugsMoreThanZero(districtsByUserId, districtsMapFromWeb, benefit);
                if (!messagesList.isEmpty()) {
                    sendFoundedDrugsToBot(drugName, benefit, internationalName, messagesList, false);
                    printToLog("Drug exists: " + drugName);
                    countFounded++;
                } else {
                    printToLog("Drug not exists: " + drugName);
                }
            }

            if (countFounded > 0) {
                sendMessageToBot("Поиск лекарст из ваших подписок происходит каждые 5 минут." +
                        LINE_SEPARATOR + "Чтобы больше не получать уведомления, вы можете отписаться: " + UNSUBSCRIBE);
            }
        }

        connectionsToDB.closeConnection();
    }

    private void sendFoundedDrugsToBot(String drugName, String benefit, String internationalName, List<String> messagesList, boolean isEmpty) {
        sendMessageToBot((isEmpty ? "" : DRUG_FOUND + LINE_SEPARATOR)
                + DRUG_PRINT + drugName + LINE_SEPARATOR
                + INTERNATIONAL_NAME + internationalName + LINE_SEPARATOR
                + BENEFIT_PRINT + benefit + LINE_SEPARATOR
                + LINE_SEPARATOR
                + INFO + ": " + INFO_BEFORE_VISIT + "!" + LINE_SEPARATOR
                + LINE_SEPARATOR
                + PHARMACIES_AND_AVAILABILITY);
        if (isEmpty) {
            sendMessageToBot(THERE_ARE_NO_DRUGS);
        } else {
            messagesList.forEach(this::sendMessageToBot);
        }
    }

    private List<List<String>> swapOneRowToRows(List<String> buttonNamesList) {
        List<List<String>> rows = new ArrayList<>();

        for (String buttonName : buttonNamesList) {
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
        sendMessage(text, sendMessage);
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
        sendMessage(text, sendMessage);
    }

    private void sendMessageToAllUsers(String text) {
        for (Map.Entry<Long, Boolean> entry : usersTableFromDB.entrySet()) {
            sendMessageToCertainUser(text, entry.getKey());
        }
    }

    private void sendMessageToAdmin(String text) {
        sendMessageToCertainUser(text, propertiesDTO.getAdminId());
    }

    private void sendMessageToCertainUser(String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(new ReplyKeyboardRemove(true));
        message.setChatId(chatId);
        sendMessage(text, message);
    }

    protected void sendMessageToBot(String text) {
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage(text, sendMessage);
    }

    private void sendMessage(String text, SendMessage message) {
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            printToLog(MY_BOT_ERROR + " 6: " + e.getMessage());
        }
    }

    public Map<Integer, String> getBenefitsMap() {
        return benefitsMap;
    }

    public String getBotUsername() {
        return propertiesDTO.getBotUsername();
    }

    public String getBotToken() {
        return propertiesDTO.getBotToken();
    }

    public String getPathToJsonFromWeb() {
        return propertiesDTO.getPathToJsonFromWeb();
    }

    private boolean isAdminId() {
        return userId == propertiesDTO.getAdminId();
    }
}
