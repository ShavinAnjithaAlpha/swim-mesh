package org.shavin.swim.messages;

public class CustomUserData {

    private final byte[] data;
    private int disseminationCount = 0;


    public CustomUserData(byte[] data) {
        this.data = data;
    }

    public int disseminationCount() {
        return disseminationCount;
    }

    public void incrementDissemationCount() {
        disseminationCount++;
    }

    public byte[] getData() {
        return data;
    }

}
