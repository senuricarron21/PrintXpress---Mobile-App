package com.example.Printxpress_hdcse_131_10;

public class Promotion {
    private int promId;
    private String promTitle;
    private String description;
    private double disRate;
    private String startDate;
    private String endDate;
    private boolean isActive;

    public Promotion(int promId, String promTitle, String description, double disRate, String startDate, String endDate, boolean isActive) {
        this.promId = promId;
        this.promTitle = promTitle;
        this.description = description;
        this.disRate = disRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public int getPromId() {
        return promId;
    }

    public void setPromId(int promId) {
        this.promId = promId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getDisRate() {
        return disRate;
    }

    public void setDisRate(double disRate) {
        this.disRate = disRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromTitle() {
        return promTitle;
    }

    public void setPromTitle(String promTitle) {
        this.promTitle = promTitle;
    }
}
