package com.haheskja.mtgpointtracker;

import java.util.List;

public class League {
    private String id;
    private String name;
    private List<String> participants;
    private List<String> participantsid;
    private List<Integer> totalscore;

    public League() {
    }

    public League(String name, List<String> participants, List<String> participantsid, List<Integer> totalscore, String id) {
        this.name = name;
        this.participants = participants;
        this.participantsid = participantsid;
        this.totalscore = totalscore;
        this.id = id;
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
