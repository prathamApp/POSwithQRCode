package com.example.pef.prathamopenschool;

// add created by id

public class Group {
    public String GroupID;
    public String GroupName; // AKA Unit Code
    public String UnitNumber;
    public String DeviceID;
    public String Responsible;
    public String ResponsibleMobile;
    public int VillageID;
    public String GroupCode;
    public int ProgramID;
    public String CreatedBy;
    Boolean newGroup;

    public String sharedBy;
    public String SharedAtDateTime;
    public String appVersion;
    public String appName;
    public String CreatedOn;

    public String getUnitNumber() {
        return UnitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        UnitNumber = unitNumber;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public String getResponsible() {
        return Responsible;
    }

    public void setResponsible(String responsible) {
        Responsible = responsible;
    }

    public String getResponsibleMobile() {
        return ResponsibleMobile;
    }

    public void setResponsibleMobile(String responsibleMobile) {
        ResponsibleMobile = responsibleMobile;
    }

    public String getGroupCode() {
        return GroupCode;
    }

    public void setGroupCode(String groupCode) {
        GroupCode = groupCode;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public Boolean getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(Boolean newGroup) {
        this.newGroup = newGroup;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public String getSharedAtDateTime() {
        return SharedAtDateTime;
    }

    public void setSharedAtDateTime(String sharedAtDateTime) {
        SharedAtDateTime = sharedAtDateTime;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCreatedOn() {
        return CreatedOn;
    }

    public void setCreatedOn(String createdOn) {
        CreatedOn = createdOn;
    }

    public String getVillageName() {
        return VillageName;
    }

    public void setVillageName(String villageName) {
        VillageName = villageName;
    }

    public String getSchoolName() {
        return SchoolName;
    }

    public void setSchoolName(String schoolName) {
        SchoolName = schoolName;
    }

    public String VillageName;
    public String SchoolName;

    public String getGroupID() {
        return GroupID;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }


    public String getGroupName() {
        return GroupName;
    }

    @Override
    public String toString() {
        return this.GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public int getVillageID() {
        return VillageID;
    }

    public void setVillageID(int villageID) {
        VillageID = villageID;
    }

    public int getProgramID() {
        return ProgramID;
    }

    public void setProgramID(int programID) {
        ProgramID = programID;
    }
}