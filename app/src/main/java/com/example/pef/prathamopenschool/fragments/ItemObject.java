package com.example.pef.prathamopenschool.fragments;

/**
 * Created by Ameya on 6/9/2017.
 */

public class ItemObject {

    private int screenShot;
    private String musicName;
    private String musicAuthor;
    public String name;
    public String numOfSongs;
    private String tabThumbnail;
    public String nodeType, nodeTitle, nodeImage, nodePhase, nodeAge, nodeDescription, nodeKeyword, resourceId;
    public String nodeId, resourceLevel, resourceType, resourcePath, sameCode;
    public String nodeDesc,nodeKeywords,nodeList;

    public ItemObject(String tabThumbnail, String nodeTitle, String nodePhase, String nodeList) {
        this.tabThumbnail = tabThumbnail;
        this.nodeTitle = nodeTitle;
        this.nodePhase = nodePhase;
        this.nodeList = nodeList;
    }

    public String getTabThumbnail() {
        return tabThumbnail;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public String getNodePhase() {
        return nodePhase;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getNodeAge() {
        return nodeAge;
    }

    public String getNodeDesc() {
        return nodeDesc;
    }

    public String getNodeKeywords() {
        return nodeKeywords;
    }

    public String getSameCode() {
        return sameCode;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getNodeList() {
        return nodeList;
    }

}