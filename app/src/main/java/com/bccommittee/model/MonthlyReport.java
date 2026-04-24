package com.bccommittee.model;

public class MonthlyReport {
    private long id;
    private String month;
    private long collected;
    private long interestIn;
    private long penalties;
    private long fees;
    private long totalFund;
    private int defaults;
    private long fundId;

    public MonthlyReport() {}

    public MonthlyReport(long id, String month, long collected, long interestIn,
                         long penalties, long fees, long totalFund, int defaults, long fundId) {
        this.id = id;
        this.month = month;
        this.collected = collected;
        this.interestIn = interestIn;
        this.penalties = penalties;
        this.fees = fees;
        this.totalFund = totalFund;
        this.defaults = defaults;
        this.fundId = fundId;
    }

    public long getTotalRevenue() {
        return collected + interestIn + fees + penalties;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public long getCollected() { return collected; }
    public void setCollected(long collected) { this.collected = collected; }

    public long getInterestIn() { return interestIn; }
    public void setInterestIn(long interestIn) { this.interestIn = interestIn; }

    public long getPenalties() { return penalties; }
    public void setPenalties(long penalties) { this.penalties = penalties; }

    public long getFees() { return fees; }
    public void setFees(long fees) { this.fees = fees; }

    public long getTotalFund() { return totalFund; }
    public void setTotalFund(long totalFund) { this.totalFund = totalFund; }

    public int getDefaults() { return defaults; }
    public void setDefaults(int defaults) { this.defaults = defaults; }

    public long getFundId() { return fundId; }
    public void setFundId(long fundId) { this.fundId = fundId; }
}
