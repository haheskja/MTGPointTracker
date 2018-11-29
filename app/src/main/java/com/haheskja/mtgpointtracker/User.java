package com.haheskja.mtgpointtracker;

import android.support.annotation.NonNull;

public class User implements Comparable{
    public int score;
    public String username;

    public User(int score, String username) {
        this.score = score;
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public String getUsername() {
        return username;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int compareTo(@NonNull Object comparestu) {
        int compareage=((User)comparestu).getScore();
        return compareage-this.score;
    }
}
