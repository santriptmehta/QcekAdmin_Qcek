package com.blankspace.adminQcek;

public class TestModel {

    private String testID;
    private int time;

    public TestModel(String testID, int time) {
        this.testID = testID;
        this.time = time;
      //  this.time = time;
    }


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTestID() {
        return testID;
    }

    public void setTestID(String testID) {
        this.testID = testID;
    }
}
