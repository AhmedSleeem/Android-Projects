package com.example.startactivity.Model;

import android.widget.Button;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Chat {
    public String sender,receiver,message;
    public String isseen;

    public Chat(String sender, String receiver, String message, String isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen=isseen;
    }

    public Chat() {
    }
    @Exclude
    public String getSender() {
        return sender;
    }

    @Exclude
    public void setSender(String sender) {
        this.sender = sender;
    }
    @Exclude
    public String getReceiver() {
        return receiver;
    }
    @Exclude
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    @Exclude
    public String getMessage() {
        return message;
    }
    @Exclude
    public void setMessage(String message) {
        this.message = message;
    }

    public String getIsseen() {
        return isseen;
    }

    public void setIsseen(String isseen) {
        this.isseen = isseen;
    }
}
