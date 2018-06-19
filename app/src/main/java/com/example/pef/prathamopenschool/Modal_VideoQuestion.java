package com.example.pef.prathamopenschool;

import com.google.gson.annotations.SerializedName;

public class Modal_VideoQuestion {
    @SerializedName("nodeId")
    String nodeId;
    @SerializedName("nodeType")
    String nodeType;
    @SerializedName("nodeTitle")
    String nodeTitle;
    @SerializedName("nodeTime")
    String nodeTime;
    @SerializedName("QueId")
    String QueId;
    @SerializedName("Question")
    String question;
    @SerializedName("QuestionType")
    String QuestionType;
    @SerializedName("Option1")
    String Option1;
    @SerializedName("Option2")
    String Option2;
    @SerializedName("Option3")
    String Option3;
    @SerializedName("Option4")
    String Option4;
    @SerializedName("Answer")
    String Answer;
    @SerializedName("difficultyLevel")
    String difficultyLevel;
    @SerializedName("resourceName")
    String resourceName;
    @SerializedName("resourceId")
    String resourceId;
    @SerializedName("resourcePath")
    String resourcePath;
    @SerializedName("programLanguage")
    String programLanguage;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    public String getNodeTime() {
        return nodeTime;
    }

    public void setNodeTime(String nodeTime) {
        this.nodeTime = nodeTime;
    }

    public String getQueId() {
        return QueId;
    }

    public void setQueId(String queId) {
        QueId = queId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionType() {
        return QuestionType;
    }

    public void setQuestionType(String questionType) {
        QuestionType = questionType;
    }

    public String getOption1() {
        return Option1;
    }

    public void setOption1(String option1) {
        Option1 = option1;
    }

    public String getOption2() {
        return Option2;
    }

    public void setOption2(String option2) {
        Option2 = option2;
    }

    public String getOption3() {
        return Option3;
    }

    public void setOption3(String option3) {
        Option3 = option3;
    }

    public String getOption4() {
        return Option4;
    }

    public void setOption4(String option4) {
        Option4 = option4;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getProgramLanguage() {
        return programLanguage;
    }

    public void setProgramLanguage(String programLanguage) {
        this.programLanguage = programLanguage;
    }
}
