package com.bccommittee.model;

public class Fund {
    private long id;
    private String name;
    private int totalMembers;
    private long monthlyAmount;
    private long memberFee;
    private float interestRate;
    private String status; // "allpaid", "overdue", "closed"
    private int overdueCount;
    private String closedDate;
    private String dotColor;
    private long createdAt;

    public Fund() {
        this.createdAt = System.currentTimeMillis();
        this.interestRate = 2.0f;
        this.status = "allpaid";
        this.overdueCount = 0;
    }

    // Full constructor
    public Fund(long id, String name, int totalMembers, long monthlyAmount,
                long memberFee, float interestRate, String status,
                int overdueCount, String closedDate, String dotColor, long createdAt) {
        this.id = id;
        this.name = name;
        this.totalMembers = totalMembers;
        this.monthlyAmount = monthlyAmount;
        this.memberFee = memberFee;
        this.interestRate = interestRate;
        this.status = status;
        this.overdueCount = overdueCount;
        this.closedDate = closedDate;
        this.dotColor = dotColor;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public long getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(long monthlyAmount) { this.monthlyAmount = monthlyAmount; }

    public long getMemberFee() { return memberFee; }
    public void setMemberFee(long memberFee) { this.memberFee = memberFee; }

    public float getInterestRate() { return interestRate; }
    public void setInterestRate(float interestRate) { this.interestRate = interestRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    public String getClosedDate() { return closedDate; }
    public void setClosedDate(String closedDate) { this.closedDate = closedDate; }

    public String getDotColor() { return dotColor; }
    public void setDotColor(String dotColor) { this.dotColor = dotColor; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isClosed() { return "closed".equals(status); }
    public boolean isOverdue() { return "overdue".equals(status); }
    public boolean isAllPaid() { return "allpaid".equals(status); }
}
