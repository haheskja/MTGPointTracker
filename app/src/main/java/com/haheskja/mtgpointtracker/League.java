package com.haheskja.mtgpointtracker;

import java.util.List;

public class League {
    private String id;
    private String name;
    private boolean ongoing;
    private int numgames;
    private List<String> participants;
    private List<String> participantsid;
    private List<Integer> totalscore;

    public League() {
    }

    public League(String id, String name, boolean ongoing, int numgames, List<String> participants, List<String> participantsid, List<Integer> totalscore) {
        this.id = id;
        this.name = name;
        this.ongoing = ongoing;
        this.numgames = numgames;
        this.participants = participants;
        this.participantsid = participantsid;
        this.totalscore = totalscore;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public int getNumgames() {
        return numgames;
    }

    public String getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public List<Integer> getTotalscore(){
        return totalscore;
    }
    public List<String> getParticipants() {
        return participants;
    }
    public List<String> getParticipantsid() {
        return participantsid;
    }
}
