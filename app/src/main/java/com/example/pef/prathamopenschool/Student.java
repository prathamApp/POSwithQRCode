package com.example.pef.prathamopenschool;

// change student id  int to String
// change grp id int to String
public class Student {
    public String StudentID;
    public String FirstName;
    public String MiddleName;
    public int Age;
    public int Class;
    public String UpdatedDate;
    public String LastName;
    public String Gender;
    public String GroupID;
    public String CreatedBy;
    Boolean newStudent;
    public String StudentUID;
    Boolean IsSelected;
    public String studentPhotoPath;
    public String sharedBy;
    public String SharedAtDateTime;
    public String appVersion;
    public String appName;
    public String CreatedOn;

    public Student(String id, String name, String groupID) {
        this.StudentID = id;
        this.FirstName = name;
        this.GroupID = groupID;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String studentID) {
        StudentID = studentID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getGroupID() {
        return GroupID;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public Boolean getNewStudent() {
        return newStudent;
    }

    public void setNewStudent(Boolean newStudent) {
        this.newStudent = newStudent;
    }

    public String getStudentUID() {
        return StudentUID;
    }

    public void setStudentUID(String studentUID) {
        StudentUID = studentUID;
    }

    public Boolean getSelected() {
        return IsSelected;
    }

    public void setSelected(Boolean selected) {
        IsSelected = selected;
    }

    public String getStudentPhotoPath() {
        return studentPhotoPath;
    }

    public void setStudentPhotoPath(String studentPhotoPath) {
        this.studentPhotoPath = studentPhotoPath;
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

    Student() {
    }

    Student(String Firstname, String studentImgPath) {
        this.FirstName = Firstname;
        this.studentPhotoPath = studentImgPath;
    }

}