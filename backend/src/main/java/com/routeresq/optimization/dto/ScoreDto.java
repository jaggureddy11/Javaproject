package com.routeresq.optimization.dto;

public class ScoreDto {

    private int hard;
    private int soft;

    public ScoreDto() {
    }

    public ScoreDto(int hard, int soft) {
        this.hard = hard;
        this.soft = soft;
    }

    public int getHard() {
        return hard;
    }

    public void setHard(int hard) {
        this.hard = hard;
    }

    public int getSoft() {
        return soft;
    }

    public void setSoft(int soft) {
        this.soft = soft;
    }
}
