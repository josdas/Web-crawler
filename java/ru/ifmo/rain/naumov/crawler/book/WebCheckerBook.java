package ru.ifmo.rain.naumov.crawler.book;

import ru.ifmo.rain.naumov.crawler.crawler.WebChecker;

import java.util.Arrays;

public class WebCheckerBook implements WebChecker {
    private final String[] topicSubstrings;
    private final String[] yearsSubstrings;

    WebCheckerBook(int lowerYear, int upperYear, String[] topicSubstrings) {
        this.topicSubstrings = topicSubstrings;
        yearsSubstrings = new String[upperYear - lowerYear];
        for (int year = lowerYear; year < upperYear; year++) {
            yearsSubstrings[year - lowerYear] = "<dt>Год:</dt><dd>" + year + "</dd>";
        }
    }

    private static String removeWhitespaces(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                result.append(text.charAt(i));
            }
        }
        return result.toString();

    }

    private static boolean checkAnyContains(String text, String[] words) {
        return Arrays.stream(words).anyMatch(text::contains);
    }

    private boolean checkYear(String page) {
        return checkAnyContains(page, yearsSubstrings);
    }

    private boolean checkTopics(String page) {
        return checkAnyContains(page, topicSubstrings);
    }

    public boolean check(String page) {
        String pageWithoutSpaces = removeWhitespaces(page);
        return checkYear(pageWithoutSpaces) && checkTopics(pageWithoutSpaces);
    }
}
