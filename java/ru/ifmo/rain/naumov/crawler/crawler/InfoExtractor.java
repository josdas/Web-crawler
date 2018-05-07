package ru.ifmo.rain.naumov.crawler.crawler;

import java.io.IOException;

public interface InfoExtractor<T> {
    T extract(String url, String page);
}
