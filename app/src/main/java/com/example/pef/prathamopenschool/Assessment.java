package com.example.pef.prathamopenschool;

/**
 * Created by Abc on 10-Jul-17.
 */

public class Assessment {

    public String name, ScoredMarks, TotalMarks,nosOfAssessments,TotalPercentage;
    public String thumbnail;
    public String nodeType, nodeTitle, nodeImage, nodePhase, nodeAge, nodeKeyword, resourceId;
    public String nodeId, resourceLevel, resourceType, resourcePath, sameCode;
    public String nodeDesc,nodeKeywords,nodeList,imgPath;
    public  float rating;

    public Assessment(){}


    public Assessment(Float rating, String name) {
        this.rating= rating;
        this.name = name;
    }

    public Assessment(String resourcePath, String resourceId){
        this.resourcePath = resourcePath;
        this.resourceId = resourceId;
    }

    public Assessment(String name, String nosOfAssessments, String ScoredMarks, String TotalMarks, String TotalPercentage) {
        this.name = name;
        this.nosOfAssessments = nosOfAssessments;
        this.ScoredMarks      = ScoredMarks;
        this.TotalMarks       = TotalMarks;
        this.TotalPercentage  = TotalPercentage;
    }

}
