package ru.ifmo.rain.naumov.crawler.crawler;

import java.util.Collection;

public interface PageCollector<T> {
    void add(String url, String page);
    Collection<T> get();
}
