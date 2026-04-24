package com.bccommittee.model;

public class Member {
    private long id;
    private String name;
    private String phone;
    private String avatar; // 2-char initials
    private long contribution;
    private long amtBorrowed;
    private long fees;
    private String joinDate;
    private long fundId;
    private long createdAt;

    public Member() {
        this.createdAt = System.currentTimeMillis();
    }

    public Member(long id, String name, String phone, String avatar,
                  long contribution, long amtBorrowed, long fees,
                  String joinDate, long fundId, long createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
        this.contribution = contribution;
        this.amtBorrowed = amtBorrowed;
        this.fees = fees;
        this.joinDate = joinDate;
        this.fundId = fundId;
        this.createdAt = createdAt;
    }

    public static String generateAvatar(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.toString().isEmpty() ? "?" : sb.toString();
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public long getContribution() { return contribution; }
    public void setContribution(long contribution) { this.contribution = contribution; }

    public long getAmtBorrowed() { return amtBorrowed; }
    public void setAmtBorrowed(long amtBorrowed) { this.amtBorrowed = amtBorrowed; }

    public long getFees() { return fees; }
    public void setFees(long fees) { this.fees = fees; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public long getFundId() { return fundId; }
    public void setFundId(long fundId) { this.fundId = fundId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getMonthlyInterest() {
        if (amtBorrowed <= 0) return 0;
        return Math.round(amtBorrowed * 0.02);
    }
}
