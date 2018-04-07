package com.example.pef.prathamopenschool;

/**
 * Created by PEF-2 on 27/10/2015.
 */
public class StudentList {
    String StudentID;
    String studentName;

    StudentList(String id,String name){
        StudentID = id;
        studentName = name;
    }

    @Override
    public String toString(){
        return this.studentName;
    }

    public String getStudentID() {
        return StudentID;
    }

    public String getStudentName() {
        return studentName;
    }
}
