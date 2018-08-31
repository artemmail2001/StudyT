package com.example.artik.studyt;

public class Chats {

    public boolean seen;
    public long timestamp;

    public Chats(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Chats(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
