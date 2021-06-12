package com.example.ezpath;

import org.bson.types.ObjectId;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SavedPath extends RealmObject {
    @PrimaryKey String pathName;
    RealmList<ErrandResults> errResultsList;

    public SavedPath(){}


    public RealmList<ErrandResults> getErrResultsList() {
        return errResultsList;
    }

    public String getPathName() {
        return pathName;
    }


    public void setErrResultsList(RealmList<ErrandResults> errResultsList) {
        this.errResultsList = errResultsList;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }
}
