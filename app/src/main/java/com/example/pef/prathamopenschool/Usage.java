package com.example.pef.prathamopenschool;

public class Usage{

    public String grpName;
    public long usageTime;
    public String usageTimeInDays;

    public Usage(String groupName, long usageTime,String usageTimeInDays) {
        this.grpName = groupName;
        this.usageTime = usageTime;
        this.usageTimeInDays = usageTimeInDays;
    }

    public String getGrpName() {
        return grpName;
    }

    public void setGrpName(String grpName) {
        this.grpName = grpName;
    }

    public long getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(long usageTime) {
        this.usageTime = usageTime;
    }

    public String getUsageTimeInDays() {
        return usageTimeInDays;
    }

    public void setUsageTimeInDays(String usageTimeInDays) {
        this.usageTimeInDays = usageTimeInDays;
    }
}
