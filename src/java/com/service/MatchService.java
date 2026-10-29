package com.service;

import com.dao.ItemDao;
import com.dao.MatchDao;
import com.dao.NotificationDao;
import com.entity.Item;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The matching algorithm: deterministic, weighted, and explainable.
 *
 * Five signals, 100 points in total:
 *
 *   category    30   the same category, or nothing
 *   words       25   overlapping words in the title and description
 *   colour      15   the same colour word
 *   place       15   overlapping words in the location
 *   date        15   how close the two dates are
 *
 * Anything below {@link #THRESHOLD} is not stored, because a suggestion nobody
 * would act on is worse than no suggestion. A stored suggestion always carries
 * the reasons that produced it, so the card can say why the two reports were
 * put next to each other.
 *
 * A score is a hint for a human. It never approves a claim and it is never
 * treated as proof of ownership.
 */
public class MatchService {

    /** Below this, a pair is not worth showing to anybody. */
    public static final int THRESHOLD = 45;

    /** At or above this, the two reporters are told about it once. */
    private static final int NOTIFY_AT = 60;

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "and", "the", "my", "in", "on", "at", "it", "is", "was", "of", "to",
            "for", "with", "near", "from", "i", "me", "mine", "this", "that", "there", "has",
            "have", "had", "been", "but", "or", "if", "its", "be", "am", "are", "some", "any",
            "lost", "found", "please", "somebody", "someone", "item", "items", "left", "kept",
            "about", "after", "before", "while", "during", "very", "just", "also", "will"));

    private final ItemDao items = new ItemDao();
    private final MatchDao matches = new MatchDao();
    private final NotificationDao notifications = new NotificationDao();

    /** One scored pair, before it is written anywhere. */
    public static class Scored {
        public final int score;
        public final String reasons;

        Scored(int score, String reasons) {
            this.score = score;
            this.reasons = reasons;
        }
    }

    /**
     * Recomputes suggestions for one report against every open report on the
     * other side of the board. Called after a report is created or edited, so
     * the suggestions follow the data instead of going stale.
     */
    public int recomputeFor(Item item) throws SQLException {
        if (item == null || !item.isOpen()) {
            return 0;
        }
        String otherType = item.isLost() ? "FOUND" : "LOST";
        List<Item> candidates = items.openByType(otherType);
        int stored = 0;
        for (Item other : candidates) {
            if (other.getId() == item.getId()) {
                continue; // a report is never matched against itself
            }
            Item lost = item.isLost() ? item : other;
            Item found = item.isLost() ? other : item;
            Scored s = score(lost, found);
            if (s.score >= THRESHOLD) {
                matches.save(lost.getId(), found.getId(), s.score, s.reasons);
                stored++;
                if (s.score >= NOTIFY_AT) {
                    notifyBoth(lost, found, s.score);
                }
            } else {
                // The pair may have been above the line before an edit.
                matches.deleteBelow(lost.getId(), found.getId());
            }
        }
        return stored;
    }

    /** Recomputes the whole board. Used by the admin dashboard button. */
    public int recomputeAll() throws SQLException {
        int stored = 0;
        for (Item lost : items.openByType("LOST")) {
            stored += recomputeFor(lost);
        }
        return stored;
    }

    /**
     * Scores one lost/found pair. Missing attributes simply score zero for that
     * signal; nothing throws because somebody left the colour blank.
     */
    public Scored score(Item lost, Item found) {
        int score = 0;
        List<String> why = new ArrayList<>();

        if (lost.getCategoryId() == found.getCategoryId()) {
            score += 30;
            why.add("Same category: " + found.getCategoryName());
        }

        Set<String> lostWords = words(lost.getTitle() + " " + lost.getDescription());
        Set<String> foundWords = words(found.getTitle() + " " + found.getDescription());
        List<String> shared = overlap(lostWords, foundWords);
        if (!shared.isEmpty()) {
            int smaller = Math.max(1, Math.min(lostWords.size(), foundWords.size()));
            double ratio = Math.min(1.0, shared.size() / (double) Math.min(smaller, 8));
            int wordPoints = (int) Math.round(25 * ratio);
            score += wordPoints;
            why.add("Description words in common: " + preview(shared));
        }

        if (same(lost.getColor(), found.getColor())) {
            score += 15;
            why.add("Both reported as " + found.getColor().toLowerCase());
        }

        int placePoints = placeScore(lost.getLocation(), found.getLocation());
        if (placePoints > 0) {
            score += placePoints;
            why.add(placePoints == 15
                    ? "Same place: " + found.getLocation()
                    : "Nearby places: " + lost.getLocation() + " and " + found.getLocation());
        }

        if (lost.getItemDate() != null && found.getItemDate() != null) {
            long days = Math.abs(ChronoUnit.DAYS.between(lost.getItemDate(), found.getItemDate()));
            boolean foundTooEarly =
                    found.getItemDate().isBefore(lost.getItemDate().minusDays(2));
            if (!foundTooEarly) {
                if (days <= 1) {
                    score += 15;
                    why.add(days == 0 ? "Reported for the same day" : "Dates are one day apart");
                } else if (days <= 3) {
                    score += 12;
                    why.add("Dates are " + days + " days apart");
                } else if (days <= 7) {
                    score += 8;
                    why.add("Dates are within a week");
                } else if (days <= 21) {
                    score += 4;
                    why.add("Dates are within three weeks");
                }
            }
        }

        score = Math.max(0, Math.min(100, score));
        return new Scored(score, String.join(" | ", why));
    }

    // ------------------------------------------------------------------ helpers

    private void notifyBoth(Item lost, Item found, int score) throws SQLException {
        String pair = lost.getId() + "-" + found.getId();
        notifications.add(lost.getReporterId(), "MATCH",
                "A found report looks like your lost \"" + lost.getTitle() + "\" ("
                + score + "% match).",
                "/items/view?id=" + lost.getId(),
                "match-" + pair + "-u" + lost.getReporterId());
        notifications.add(found.getReporterId(), "MATCH",
                "Someone's lost report looks like the \"" + found.getTitle()
                + "\" you found (" + score + "% match).",
                "/items/view?id=" + found.getId(),
                "match-" + pair + "-u" + found.getReporterId());
    }

    private static Set<String> words(String text) {
        Set<String> out = new HashSet<>();
        if (text == null) {
            return out;
        }
        for (String raw : text.toLowerCase().split("[^a-z0-9]+")) {
            if (raw.length() >= 3 && !STOPWORDS.contains(raw)) {
                out.add(stem(raw));
            }
        }
        return out;
    }

    /** Crude plural trim, so "keys" and "key" count as the same word. */
    private static String stem(String word) {
        if (word.length() > 4 && word.endsWith("es")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.length() > 3 && word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static List<String> overlap(Set<String> a, Set<String> b) {
        List<String> shared = new ArrayList<>();
        for (String word : a) {
            if (b.contains(word)) {
                shared.add(word);
            }
        }
        shared.sort((x, y) -> y.length() - x.length());
        return shared;
    }

    private static String preview(List<String> shared) {
        return String.join(", ", shared.subList(0, Math.min(3, shared.size())));
    }

    private static boolean same(String a, String b) {
        return a != null && b != null && !a.isBlank()
                && a.trim().equalsIgnoreCase(b.trim());
    }

    private static int placeScore(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }
        if (a.trim().equalsIgnoreCase(b.trim())) {
            return 15;
        }
        Set<String> wa = words(a);
        Set<String> wb = words(b);
        return overlap(wa, wb).isEmpty() ? 0 : 10;
    }
}
