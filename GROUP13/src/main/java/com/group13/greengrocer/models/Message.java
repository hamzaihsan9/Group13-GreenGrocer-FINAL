package com.group13.greengrocer.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int senderId;
    private int receiverId;
    private User sender;
    private User receiver;
    private String content;
    private LocalDateTime sentTime;
    private boolean isRead;
    private Integer replyToId;

    public Message() {
        this.sentTime = LocalDateTime.now();
        this.isRead = false;
    }

    public Message(int senderId, int receiverId, String content) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public User getSender() { return sender; }
    public void setSender(User sender) { 
        this.sender = sender;
        if (sender != null) this.senderId = sender.getId();
    }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { 
        this.receiver = receiver;
        if (receiver != null) this.receiverId = receiver.getId();
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Integer getReplyToId() { return replyToId; }
    public void setReplyToId(Integer replyToId) { this.replyToId = replyToId; }

    public void setReplyTo(Message replyTo) {
        if (replyTo != null) {
            this.replyToId = replyTo.getId();
        }
    }

    public String getFormattedTime() {
        return sentTime != null ? sentTime.format(FORMATTER) : "";
    }

    @Override
    public String toString() {
        return getFormattedTime() + " - " + content;
    }
}
