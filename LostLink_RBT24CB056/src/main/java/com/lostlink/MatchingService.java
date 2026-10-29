package com.lostlink;

import com.lostlink.Models.*;
import java.text.Normalizer;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Transparent weighted similarity; never a probability. Missing optional data earns zero. */
public final class MatchingService {
  static Set<String> tokens(String text) {
    if (text == null || text.isBlank()) return Set.of();
    String clean =
        Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFKD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^\\p{L}\\p{N}]+", " ")
            .trim();
    return clean.isEmpty() ? Set.of() : new HashSet<>(Arrays.asList(clean.split("\\s+")));
  }

  public static double similarity(String a, String b) {
    Set<String> x = tokens(a), y = tokens(b);
    if (x.isEmpty() || y.isEmpty()) return 0;
    Set<String> union = new HashSet<>(x);
    union.addAll(y);
    Set<String> intersection = new HashSet<>(x);
    intersection.retainAll(y);
    return (double) intersection.size() / union.size();
  }

  public Optional<Match> compare(Report lost, Report found) {
    if (lost.type() != Type.LOST
        || found.type() != Type.FOUND
        || lost.id() == found.id()
        || lost.status() != Status.ACTIVE
        || found.status() != Status.ACTIVE
        || !lost.category().equals(found.category())
        || found.date().isBefore(lost.date())) return Optional.empty();
    double name = similarity(lost.name(), found.name());
    double colour = similarity(lost.colour(), found.colour());
    double location = similarity(lost.location(), found.location());
    double description = similarity(lost.description(), found.description());
    long days = ChronoUnit.DAYS.between(lost.date(), found.date());
    double date = Math.max(0, 1 - days / 30.0);
    double score = 25 + 25 * name + 15 * colour + 15 * location + 10 * date + 10 * description;
    List<String> reasons = new ArrayList<>();
    reasons.add("Same category");
    if (colour == 1) reasons.add("same colour");
    else if (colour > 0) reasons.add("overlapping colour words");
    if (name == 1) reasons.add("same name");
    else if (name > 0) reasons.add("similar name");
    if (location == 1) reasons.add("same location");
    else if (location > 0) reasons.add("shared location words");
    reasons.add(days == 0 ? "same incident date" : days + " day(s) apart");
    if (description > 0) reasons.add("shared description words");
    return Optional.of(new Match(lost, found, score, String.join("; ", reasons) + "."));
  }

  public List<Match> suggest(List<Report> reports) {
    List<Match> matches = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (Report lost : reports)
      for (Report found : reports)
        compare(lost, found)
            .ifPresent(
                m -> {
                  if (seen.add(lost.id() + ":" + found.id())) matches.add(m);
                });
    matches.sort(Comparator.comparingDouble(Match::score).reversed());
    return matches;
  }
}
