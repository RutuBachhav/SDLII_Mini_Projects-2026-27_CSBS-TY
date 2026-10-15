package com.entity;

import java.util.Arrays;
import java.util.List;

/**
 * A suggested pairing between a lost report and a found report.
 *
 * {@code counterpart} is whichever side of the pair the viewer is not looking
 * at, so a card can show "your lost wallet looks like this found wallet".
 * A suggestion is never evidence of ownership; it only points two people at
 * each other.
 */
public class MatchSuggestion {

    private int id;
    private int lostItemId;
    private int foundItemId;
    private int score;
    private String reasons;
    private Item counterpart;
    private Item mine;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLostItemId() { return lostItemId; }
    public void setLostItemId(int lostItemId) { this.lostItemId = lostItemId; }

    public int getFoundItemId() { return foundItemId; }
    public void setFoundItemId(int foundItemId) { this.foundItemId = foundItemId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getReasons() { return reasons; }
    public void setReasons(String reasons) { this.reasons = reasons; }

    public Item getCounterpart() { return counterpart; }
    public void setCounterpart(Item counterpart) { this.counterpart = counterpart; }

    public Item getMine() { return mine; }
    public void setMine(Item mine) { this.mine = mine; }

    /** Reasons are stored as one string so the explanation travels with the row. */
    public List<String> getReasonList() {
        if (reasons == null || reasons.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(reasons.split("\\s*\\|\\s*"));
    }

    /** Used for the strength label on the card. */
    public String getStrength() {
        if (score >= 75) {
            return "Strong";
        }
        if (score >= 55) {
            return "Possible";
        }
        return "Weak";
    }
}
