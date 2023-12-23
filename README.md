# GorzdravSubcribeToPills
*Горздрав подписка на лекарства
<h2>About:</h2>

Scanning for preferential drugs in pharmacies in St. Petersburg. And also subscribe to their appearance.<br><br>Non-official bot!

<h2>Описание:</h2>
Поиск льготных лекарственных препаратов в аптеках СПб. А также подписка на их появление.<br><br>Неоффициальный бот!

<br>
<br>
<br>


ToDo tasks:
1) <s>Create bot draft with config file;</s>
2) <s>Create a common file to store user ids and status (Bot is running or stopped);</s>
3) <s>Create a file for each user to store subscriptions, the date of the last request from the user and the username;</s>
4) <s>Processing simple commands from user;</s>
5) <s>Add districts selection for user at start and change them after;</s>
6) <s>Implementation of the search for drugs, obtaining availability in pharmacies for a certain benefit and district;</s>
7) <s>Implementation of a drug subscription with saving to a user file;</s>
8) <s>Send a start message at first time user start bot;</s>
9) <s>Suggest search at start bot;</s>
10) <s>When entering a drug without a command, still make a search request;</s>
11) <s>Implementation of removal of subscriptions;</s>
12) <s>Suggest to unsubscribe when a pill is found;</s>
13) <s>Use a database instead of writing to files;</s>
14) <s>Constant (once every 5 minutes) monitoring of the availability of a drug from user subscriptions and sending a notification when it appears daily;</s>
15) <s>Not to search in the background if 4 minutes have not passed since the last user action;</s>
16) <s>Use log4j2;</s>
17) <s>User could subscribe to drug even if it not found</s>
17) Once a pill is found in the background suggest to user not to send any more notifications today;
18) Save old searches (drug names) and offer them with the button;
19) Add searches (names of drugs) to the general list with the number of users looking for them and suggest these drugs when searching;
20) Add an additional question when unsubscribing "Are you sure? Yes / No";
21) Settings. Change language;

Чек-лист:
1) <s>Создать каркас бота с файлом конфигурации;</s>
2) <s>Создать общий файл для хранения id пользователей и статуса (Бот запущен или остановлен);</s>
3) <s>Создавать каждому пользователю свой файл для хранения подписок, даты последнего запроса от пользователя и юзернейма;</s>
4) <s>Обработка простых запросов от пользователя;</s>
5) <s>Выбор пользователем районов для подписки при старте бота и возможность смены в дальнейшем;</s>
6) <s>Реализация поиска лекарства, получение наличия в аптеках по определенной льготе и району;</s>
7) <s>Реализация подписки на лекарство с сохранением в пользовательский файл;</s>
8) <s>При старте в первый раз выводить приветствие;</s>
9) <s>При старте предлагать сразу поиск;</s>
10) <s>При вводе лекарства без команды, все равно делать запрос на поиск;</s>
11) <s>Реализация удаления подписок;</s>
12) <s>Предлагать отписаться, когда лекарство найдено;</s>
13) <s>Использовать базу данных вместо записи в файл;</s>
14) <s>Постоянный (раз в 5 минут) мониторинг наличия лекарства из подписок пользователей и отправки уведомления при появлении;</s>
15) <s>Не искать фоном, если с последнего действия пользователя не прошло 4 минуты;</s>
16) <s>Использовать log4j2;</s>
17) <s>Пользователь может подписаться на лекарство даже если оно не было найдено</s>
17) После того как лекарство найдено в фоновом режиме предложить пользователю больше не присылать уведомления сегодня;
18) Сохранять старые поиски (названия лекарств) и предлагать их кнопкой;
19) Добавлять поиски (названия лекарств) в общий список с количеством пользователей ищущих их и предлагать эти лекарства при поиске;
20) Добавить дополнительный вопрос при отписке "Вы уверены? Да/Нет";
21) Настройки. Смена языка;
