package com.bccommittee.model;

import java.util.List;

public class BackupData {
    private String appVersion;
    private long backupTimestamp;
    private String deviceId;
    private List<Fund> funds;
    private List<Member> members;
    private List<Payment> payments;
    private List<MonthlyReport> reports;

    public BackupData() {
        this.backupTimestamp = System.currentTimeMillis();
        this.appVersion = "1.0.0";
    }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public long getBackupTimestamp() { return backupTimestamp; }
    public void setBackupTimestamp(long backupTimestamp) { this.backupTimestamp = backupTimestamp; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public List<Fund> getFunds() { return funds; }
    public void setFunds(List<Fund> funds) { this.funds = funds; }

    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> members) { this.members = members; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public List<MonthlyReport> getReports() { return reports; }
    public void setReports(List<MonthlyReport> reports) { this.reports = reports; }
}
