package com.example.pef.prathamopenschool;

import java.util.List;

public class Usage {

    public String grpName;
    public long usageTime;
    public String usageTimeInDays;
    public List<String> students;
    public List<Usage> usageStudents;

    public Usage(String groupName, long usageTime, String usageTimeInDays) {
        this.grpName = groupName;
        this.usageTime = usageTime;
        this.usageTimeInDays = usageTimeInDays;
    }

    public Usage(String groupName, long usageTime, String usageTimeInDays, List<String> students) {
        this.grpName = groupName;
        this.usageTime = usageTime;
        this.usageTimeInDays = usageTimeInDays;
        this.students = students;
    }

    public Usage(String groupName, String usageTimeInDays,long usageTime, List<Usage> usageStudents) {
        this.grpName = groupName;
        this.usageTimeInDays = usageTimeInDays;
        this.usageTime = usageTime;
        this.usageStudents = usageStudents;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
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
