package com.haheskja.mtgpointtracker;

import java.util.List;

public class Game {
    private List<Integer> score;
    private List<String> participants;
    private String id;
    private String winner;
    private String gamename;

    public Game(List<Integer> score, List<String> participants, String id, String winner, String gamename) {
        this.score = score;
        this.participants = participants;
        this.id = id;
        this.winner = winner;
        this.gamename = gamename;
    }

    public Game() {
    }

    public List<Integer> getScore() {
        return score;
    }
    public List<String> getParticipants() {
        return participants;
    }

    public String getId(){
        return id;
    }
    public String getGamename(){
        return gamename;
    }
    public String getWinner(){
        return winner;
    }

    public void setId(String id){
        this.id = id;
    }
    public void setGamename(String gamename){
        this.gamename = gamename;
    }
    public void setWinner(String winner){
        this.winner = winner;
    }

    public void setScore(List<Integer> score){
        this.score = score;
    }
    public void setParticipants(List<String> participants){
        this.participants = participants;
    }
}
