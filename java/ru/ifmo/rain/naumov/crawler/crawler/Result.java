package ru.ifmo.rain.naumov.crawler.crawler;

import java.io.IOException;
import java.util.*;

public class Result {
    private final List<String> downloaded;
    private final Map<String, IOException> errors;

    public Result(final List<String> downloaded, final Map<String, IOException> errors) {
        this.downloaded = Collections.unmodifiableList(new ArrayList<>(downloaded));
        this.errors = Collections.unmodifiableMap(new HashMap<>(errors));
    }

    public List<String> getDownloaded() {
        return downloaded;
    }

    public Map<String, IOException> getErrors() {
        return errors;
    }
}
