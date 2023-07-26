package main.my.projects.java;

import java.util.Set;

public class DataWithIdJson {
    private String lastDay;
    private String userName;
    private Set<String> districtsSet;

    public DataWithIdJson() {
    }

    public DataWithIdJson(String lastDay, String userName, Set<String> districtsSet) {
        this.lastDay = lastDay;
        this.userName = userName;
        this.districtsSet = districtsSet;
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

    public Set<String> getDistrictsSet() {
        return districtsSet;
    }

    public void setDistrictsSet(Set<String> districtsSet) {
        this.districtsSet = districtsSet;
    }

    public boolean hasDistrictsSet() {
        return districtsSet != null;
    }
}
