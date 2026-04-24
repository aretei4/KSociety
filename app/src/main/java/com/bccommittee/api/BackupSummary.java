package com.bccommittee.api;

public class BackupSummary {
    private String backupId;
    private String deviceId;
    private long timestamp;
    private int fundCount;
    private int memberCount;
    private int paymentCount;
    private String appVersion;

    public BackupSummary() {}

    public String getBackupId() { return backupId; }
    public void setBackupId(String backupId) { this.backupId = backupId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getFundCount() { return fundCount; }
    public void setFundCount(int fundCount) { this.fundCount = fundCount; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getPaymentCount() { return paymentCount; }
    public void setPaymentCount(int paymentCount) { this.paymentCount = paymentCount; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
}
