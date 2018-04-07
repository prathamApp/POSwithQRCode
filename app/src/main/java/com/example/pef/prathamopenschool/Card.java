package com.example.pef.prathamopenschool;

/**
 * Created by Lincoln on 18/05/16.
 */
public class Card {
    public String name;
    public String numOfSongs;
    public String thumbnail;
    public String nodeType, nodeTitle, nodeImage, nodePhase, nodeAge, nodeKeyword, resourceId;
    public String nodeId, resourceLevel, resourceType, resourcePath, sameCode;
    public String nodeDesc,nodeKeywords,nodeList,cardLang;

    public Card() {
    }

    public Card(String name, String nodePhase, String thumbnail) {
        this.name = name;
        this.nodePhase = nodePhase;
        this.thumbnail = thumbnail;
    }

}