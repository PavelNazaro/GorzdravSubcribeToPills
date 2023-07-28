package main.my.projects.java;

public class LastUserChoose {
    private String drugName;
    private String benefit;

    public LastUserChoose() {
    }

    public LastUserChoose(String drugName) {
        this.drugName = drugName;
    }

    public LastUserChoose(String drugName, String benefit) {
        this.drugName = drugName;
        this.benefit = benefit;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getBenefit() {
        return benefit;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }
}
