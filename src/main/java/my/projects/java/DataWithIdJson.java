package my.projects.java;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class DataWithIdJson {
    private String lastDay;
    private String userName;
    private Set<String> districtsSet;
    private Map<String, String> subscriptionMap;

    public DataWithIdJson() {
    }

    public DataWithIdJson(String lastDay, String userName, Set<String> districtsSet, Map<String, String> subscriptionMap) {
        this.lastDay = lastDay;
        this.userName = userName;
        this.districtsSet = districtsSet;
        this.subscriptionMap = subscriptionMap;
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

    public Map<String, String> getSubscriptionMap() {
        return subscriptionMap;
    }

    public void setSubscriptionMap(Map<String, String> subscriptionMap) {
        this.subscriptionMap = subscriptionMap;
    }

    public boolean hasDistrictsSet() {
        return districtsSet != null;
    }

    public boolean hasSubscriptionMap() {
        return subscriptionMap != null;
    }
}
