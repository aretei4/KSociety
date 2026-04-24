package com.bccommittee.model;

public class Payment {
    private long id;
    private long memberId;
    private String memberName;
    private String memberAvatar;
    private String type; // "Interest", "Contribution", "Principal", etc.
    private long amount;
    private long principal;
    private long interest;
    private long penalty;
    private long memberFee;
    private String date;
    private String status; // "paid", "pending", "no_loan"
    private long fundId;
    private long createdAt;
    private String note;

    public Payment() {
        this.createdAt = System.currentTimeMillis();
        this.status = "paid";
    }

    public Payment(long id, long memberId, String memberName, String memberAvatar,
                   String type, long amount, long principal, long interest,
                   long penalty, long memberFee, String date, String status,
                   long fundId, long createdAt, String note) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberAvatar = memberAvatar;
        this.type = type;
        this.amount = amount;
        this.principal = principal;
        this.interest = interest;
        this.penalty = penalty;
        this.memberFee = memberFee;
        this.date = date;
        this.status = status;
        this.fundId = fundId;
        this.createdAt = createdAt;
        this.note = note;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberAvatar() { return memberAvatar; }
    public void setMemberAvatar(String memberAvatar) { this.memberAvatar = memberAvatar; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public long getPrincipal() { return principal; }
    public void setPrincipal(long principal) { this.principal = principal; }

    public long getInterest() { return interest; }
    public void setInterest(long interest) { this.interest = interest; }

    public long getPenalty() { return penalty; }
    public void setPenalty(long penalty) { this.penalty = penalty; }

    public long getMemberFee() { return memberFee; }
    public void setMemberFee(long memberFee) { this.memberFee = memberFee; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getFundId() { return fundId; }
    public void setFundId(long fundId) { this.fundId = fundId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isPaid() { return "paid".equals(status); }
    public boolean isPending() { return "pending".equals(status); }
}
