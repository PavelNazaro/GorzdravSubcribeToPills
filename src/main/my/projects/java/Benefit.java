package main.my.projects.java;

public class Benefit {
    private String benefitName;
    private int count;

    public Benefit() {
    }

    public Benefit(String benefitName, int count) {
        this.benefitName = benefitName;
        this.count = count;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public void setBenefitName(String benefitName) {
        this.benefitName = benefitName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
