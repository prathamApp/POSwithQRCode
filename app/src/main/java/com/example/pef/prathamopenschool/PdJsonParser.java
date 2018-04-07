package com.example.pef.prathamopenschool;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Abc on 07-Jun-17.
 */

public class PdJsonParser {

    String newNodeList, nodeId,nodeType,nodeTitle,nodeImage,nodePhase,nodeAge,nodeDesc,nodeKeywords,sameCode,resourceId,resourceType,resourcePath,nodelist;
    JSONArray contentNavigate;
    String[] myTitleNameArray;
    private CardAdapter adapter;
    private List<Card> cardList;
    ArrayList<String> arrJson;



    public HashMap<String,String> ReadMyFile (String newNodeList) {

        HashMap<String, String> parsedData = null;
        try {
            if (newNodeList == null) {

//            File myJsonFile= new File(MainActivity.fpath+"Json/funNavigate.json");
                File myJsonFile = new File(MainActivity.fpath + "Json/NewKetanLearn.json");
                FileInputStream stream = new FileInputStream(myJsonFile);
                String jsonStr = null;
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    jsonStr = Charset.defaultCharset().decode(bb).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting data JSON Array nodes
//            JSONArray contentNavigate  = jsonObj.getJSONArray("funNavigate");
                contentNavigate = jsonObj.getJSONArray("nodelist");
//            JSONObject contentNavigate2 = jsonObj.getJSONObject("nodelist");

            } else {

                JSONArray jsnarray = new JSONArray(newNodeList);
                contentNavigate = jsnarray;
                // contentNavigate = findElementsChildren(contentNavigate.getJSONObject(""),newNodeId);
            }


            // looping through All nodes
            parsedData = null;
            for (int i = 0; i < contentNavigate.length(); i++) {

                String notetitle;
                JSONObject c = contentNavigate.getJSONObject(i);

                nodeId = c.optString("nodeId");
                if (nodeId.equals(null))
                    nodeId = "";
                nodeType = c.optString("nodeType");
                if (nodeType.equals(null))
                    nodeType = "";
                nodeTitle = c.optString("nodeTitle");
                if (nodeType.equals(null))
                    nodeType = "";
                nodeImage = c.optString("nodeImage");
                if (nodeImage.equals(null))
                    nodeImage = "";
                nodePhase = c.optString("nodePhase");
                if (nodePhase.equals(null))
                    nodePhase = "";
                nodeAge = c.optString("nodeAge");
                if (nodeAge.equals(null))
                    nodeAge = "";
                nodeDesc = c.optString("nodeDesc");
                if (nodeDesc.equals(null))
                    nodeDesc = "";
                nodeKeywords = c.optString("nodeKeywords");
                if (nodeKeywords.equals(null))
                    nodeKeywords = "";
                sameCode = c.optString("sameCode");
                if (sameCode.equals(null))
                    sameCode = "";
                resourceId = c.optString("resourceId");
                if (resourceId.equals(null))
                    resourceId = "";
                resourceType = c.optString("resourceType");
                if (resourceType.equals(null))
                    resourceType = "";
                resourcePath = c.optString("resourcePath");
                if (resourcePath.equals(null))
                    resourcePath = "";
                nodelist = c.optString("nodelist");
                if (nodelist.equals(null))
                    nodelist = "";

/*
                arrJson.add(nodeId);
                arrJson.add(nodeType);
                arrJson.add(nodeTitle);
                arrJson.add(nodeImage);
                arrJson.add(nodePhase);
                arrJson.add(nodeAge);
                arrJson.add(nodeDesc);
                arrJson.add(nodeKeywords);
                arrJson.add(sameCode);
                arrJson.add(resourceId);
                arrJson.add(resourceType);
                arrJson.add(resourcePath);
                arrJson.add(nodelist);
*/
//nodeId,nodeType,nodeTitle,nodeImage,nodePhase,nodeAge,nodeDesc,nodeKeywords,sameCode,resourceId,resourceType,resourcePath,nodelist

                // tmp hashmap for single node
                parsedData = new HashMap<String, String>();
                // adding each child node to HashMap key => value
                parsedData.put("nodeId", nodeId);
                parsedData.put("nodeType", nodeType);
                parsedData.put("nodeTitle", nodeTitle);
                parsedData.put("nodeImage", nodeImage);
                parsedData.put("nodePhase", nodePhase);
                parsedData.put("nodeAge", nodeAge);
                parsedData.put("nodeDesc", nodeDesc);
                parsedData.put("nodeKeywords", nodeKeywords);
                parsedData.put("sameCode", sameCode);
                parsedData.put("resourceId", resourceId);
                parsedData.put("resourceType", resourceType);
                parsedData.put("resourcePath", resourcePath);
                parsedData.put("nodelist", nodelist);

                // do what do you want on your interface
            }
            //adapter.notifyDataSetChanged();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedData;
    }

}
