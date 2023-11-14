package org.example;

public class PostMessage {
    private String content;
    private String nonce;
    private boolean tts;
    private int flags;

    public PostMessage(String content, String nonce, boolean tts, int flags) {
        this.content = content;
        this.nonce = nonce;
        this.tts = tts;
        this.flags = flags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
