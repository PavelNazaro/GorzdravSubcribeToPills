package my.projects.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MyTimer {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static final int ONE_SECOND_PERIOD = 1000;
    public static final int ONE_MINUTE_PERIOD = 60 * ONE_SECOND_PERIOD;
    public static final int ONE_HOUR_PERIOD = 60 * ONE_MINUTE_PERIOD;
    public static final int FIFTEEN_MINUTES_PERIOD = 15 * ONE_MINUTE_PERIOD;

    public MyTimer(MyBot bot) {
        Timer timer = new Timer();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.add(Calendar.MINUTE, 1);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Calendar currentTime = Calendar.getInstance();

                if (isAt00Each5Minutes(currentTime)){
                    LOGGER.debug("Task executed!");
                    bot.getSubscriptions();
                }
            }
        };

        timer.scheduleAtFixedRate(task, startTime.getTime(), ONE_MINUTE_PERIOD);
    }

    private static boolean isAt00MinuteEveryHour(Calendar currentTime) {
        return currentTime.get(Calendar.MINUTE) == 0 &&
                currentTime.get(Calendar.SECOND) == 0;
    }

    private static boolean isAt00Each15Minutes(Calendar currentTime) {
        return currentTime.get(Calendar.MINUTE) % 15 == 0 &&
                currentTime.get(Calendar.SECOND) == 0;
    }

    private static boolean isAt00Each5Minutes(Calendar currentTime) {
        return currentTime.get(Calendar.MINUTE) % 5 == 0 &&
                currentTime.get(Calendar.SECOND) == 0;
    }

    private static boolean isAt00Each1Minutes(Calendar currentTime) {
        return currentTime.get(Calendar.SECOND) == 0;
    }
}