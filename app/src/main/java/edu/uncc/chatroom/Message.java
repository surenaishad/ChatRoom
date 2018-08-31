package edu.uncc.chatroom;

public class Message {
    private String message;
    private String uid;
    private String time;
    private String parentID;

    public Message(String message, String uid, String time, String parentID) {
        this.message = message;
        this.uid = uid;
        this.time = time;
        this.parentID = parentID;
    }

    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }
}
