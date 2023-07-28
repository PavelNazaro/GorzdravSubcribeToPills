package main.my.projects.java;

import java.time.LocalDateTime;

public class DistrictsDTO {
    private String storeName;
    private String storeDistrict;
    private String storeAddress;
    private String drugName;
    private String mnnName;
    private String actualDate;
    private Benefit federalCount;
    private Benefit regionalCount;
    private Benefit psychiatryCount;
    private Benefit vznCount;
    private int totalCount;

    private static final String LINE_SEPARATOR = System.lineSeparator();

    public DistrictsDTO() {
    }

    public DistrictsDTO(String storeName, String storeDistrict, String storeAddress, String drugName, String mnnName, String actualDate, Benefit federalCount, Benefit regionalCount, Benefit psychiatryCount, Benefit vznCount, int totalCount) {
        this.storeName = storeName;
        this.storeDistrict = storeDistrict;
        this.storeAddress = storeAddress;
        this.drugName = drugName;
        this.mnnName = mnnName;
        this.actualDate = actualDate;
        this.federalCount = federalCount;
        this.regionalCount = regionalCount;
        this.psychiatryCount = psychiatryCount;
        this.vznCount = vznCount;
        this.totalCount = totalCount;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreDistrict() {
        return storeDistrict;
    }

    public void setStoreDistrict(String storeDistrict) {
        this.storeDistrict = storeDistrict;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getMnnName() {
        return mnnName;
    }

    public void setMnnName(String mnnName) {
        this.mnnName = mnnName;
    }

    public String getActualDate() {
        return actualDate;
    }

    public void setActualDate(String actualDate) {
        this.actualDate = actualDate;
    }

    public Benefit getFederalCount() {
        return federalCount;
    }

    public void setFederalCount(Benefit federalCount) {
        this.federalCount = federalCount;
    }

    public Benefit getRegionalCount() {
        return regionalCount;
    }

    public void setRegionalCount(Benefit regionalCount) {
        this.regionalCount = regionalCount;
    }

    public Benefit getPsychiatryCount() {
        return psychiatryCount;
    }

    public void setPsychiatryCount(Benefit psychiatryCount) {
        this.psychiatryCount = psychiatryCount;
    }

    public Benefit getVznCount() {
        return vznCount;
    }

    public void setVznCount(Benefit vznCount) {
        this.vznCount = vznCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Benefit getBenefit(String benefit) {
        if (federalCount.getBenefitName().equals(benefit)) {
            return federalCount;
        }
        if (regionalCount.getBenefitName().equals(benefit)) {
            return regionalCount;
        }
        if (psychiatryCount.getBenefitName().equals(benefit)) {
            return psychiatryCount;
        }
        return vznCount;
    }

    public String createMessage(Benefit benefit) {
        return "Остаток: " + benefit.getCount() + " шт" + LINE_SEPARATOR +
                storeDistrict + " " + storeName + LINE_SEPARATOR +
                "Адрес: " + storeAddress + LINE_SEPARATOR +
                "Название: " + drugName + LINE_SEPARATOR +
                "Международное наименование: " + mnnName + LINE_SEPARATOR +
                benefit.getBenefitName() + ", остаток: " + benefit.getCount() + " шт" + LINE_SEPARATOR +
                "Дата обновления данных: " + LocalDateTime.parse(actualDate).toLocalDate();
    }
}