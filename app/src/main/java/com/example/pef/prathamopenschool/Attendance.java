package com.example.pef.prathamopenschool;

public class Attendance {
    public String SessionID, PresentStudentIds;
    public String GroupID;

    public Attendance(String sessionID) {

    }

    public Attendance() {

    }

    @Override
    public String toString() {
        return "Attendance{" +
                "SessionID='" + SessionID + '\'' +
                ", PresentStudentIds='" + PresentStudentIds + '\'' +
                ", GroupID='" + GroupID + '\'' +
                '}';
    }

    public String getSessionID() {
        return SessionID;
    }

    public void setSessionID(String sessionID) {
        SessionID = sessionID;
    }

    public String getPresentStudentIds() {
        return PresentStudentIds;
    }

    public void setPresentStudentIds(String presentStudentIds) {
        PresentStudentIds = presentStudentIds;
    }

    public String getGroupID() {
        return GroupID;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }
}
