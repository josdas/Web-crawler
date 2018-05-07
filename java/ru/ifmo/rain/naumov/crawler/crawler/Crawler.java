package ru.ifmo.rain.naumov.crawler.crawler;


public interface Crawler extends AutoCloseable {
    Result download(String url, int depth);

    void close();
}
