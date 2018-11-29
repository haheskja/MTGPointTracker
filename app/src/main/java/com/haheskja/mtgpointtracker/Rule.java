package com.haheskja.mtgpointtracker;

public class Rule{
    private String text;
    private int pointsGiven;
    private boolean removeFromAll;
    private boolean removeFromSelf;

    public Rule(String text, int pointsGiven, boolean removeFromAll, boolean removeFromSelf) {
        this.text = text;
        this.pointsGiven = pointsGiven;
        this.removeFromAll = removeFromAll;
        this.removeFromSelf = removeFromSelf;
    }

    public Rule() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPointsGiven() {
        return pointsGiven;
    }

    public void setPointsGiven(int pointsGiven) {
        this.pointsGiven = pointsGiven;
    }

    public boolean isRemoveFromAll() {
        return removeFromAll;
    }

    public void setRemoveFromAll(boolean removeFromAll) {
        this.removeFromAll = removeFromAll;
    }

    public boolean isRemoveFromSelf() {
        return removeFromSelf;
    }

    public void setRemoveFromSelf(boolean removeFromSelf) {
        this.removeFromSelf = removeFromSelf;
    }
}
