package com.example.afbu.parkking;

public class Notification {
    private String id;
    private String title,message,date,module,request_id;
    private String is_read;
    private String timestamp;
    private String clickable;

    public Notification(String id, String title, String message, String date, String module, String is_read, String timestamp,String clickable, String request_id) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
        this.module = module;
        this.request_id = request_id;
        this.is_read = is_read;
        this.timestamp = timestamp;
        this.clickable = clickable;
    }
    public Notification(String id, String title, String message, String date, String module, String is_read, String timestamp,String clickable) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
        this.module = module;
        this.is_read = is_read;
        this.timestamp = timestamp;
        this.clickable = clickable;
    }

    public String getClickable() {
        return clickable;
    }

    public void setClickable(String clickable) {
        this.clickable = clickable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getIs_read() {
        return is_read;
    }

    public void setIs_read(String is_read) {
        this.is_read = is_read;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
