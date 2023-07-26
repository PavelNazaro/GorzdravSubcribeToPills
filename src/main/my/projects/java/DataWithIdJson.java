package main.my.projects.java;

public class DataWithIdJson {
    private String lastDay;
    private String userName;

    public DataWithIdJson() {
    }

    public DataWithIdJson(String lastDay, String userName) {
        this.lastDay = lastDay;
        this.userName = userName;
    }

    public String getLastDay() {
        return lastDay;
    }

    public void setLastDay(String lastDay) {
        this.lastDay = lastDay;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
