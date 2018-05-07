package ru.ifmo.rain.naumov.crawler.crawler;

public interface WebLinksProcessing {
    boolean checkLink(String url);

    String getNormalizedLink(String url);
}
