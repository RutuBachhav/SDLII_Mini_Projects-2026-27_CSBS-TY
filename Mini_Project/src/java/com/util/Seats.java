package com.util;

import java.util.ArrayList;
import java.util.List;

public final class Seats {

    public static final int MAX_PER_BOOKING = 8;

    private Seats() {
    }

    public static String label(int rowIndex, int seatNumber) {
        return String.valueOf((char) ('A' + rowIndex)) + seatNumber;
    }

    public static boolean valid(String label, int rows, int seatsPerRow) {
        if (label == null || label.length() < 2 || rows < 1 || seatsPerRow < 1) {
            return false;
        }
        char row = label.charAt(0);
        if (row < 'A' || row >= 'A' + rows) {
            return false;
        }
        try {
            int number = Integer.parseInt(label.substring(1));
            return number >= 1 && number <= seatsPerRow;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<String> normalize(String[] raw, int rows, int seatsPerRow) {
        List<String> seats = new ArrayList<>();
        if (raw == null) {
            return seats;
        }
        for (String value : raw) {
            if (value == null) {
                continue;
            }
            String label = value.trim().toUpperCase();
            if (valid(label, rows, seatsPerRow) && !seats.contains(label)) {
                seats.add(label);
            }
        }
        return seats;
    }
}
