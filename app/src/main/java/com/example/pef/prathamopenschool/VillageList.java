package com.example.pef.prathamopenschool;

/**
 * Created by PEF-2 on 27/10/2015.
 */
public class VillageList {
    public int villageId;
    public String villageName;

    VillageList(int id,String name){
        villageId=id;
        villageName=name;
    }

    VillageList(int id){
        villageId=id;
    }



    @Override
    public String toString(){
        return this.villageName;
    }

    public int getVillageId() {
        return villageId;
    }

    public String getVillageName() {
        return villageName;
    }
}
