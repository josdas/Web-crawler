package ru.ifmo.rain.naumov.crawler.crawler;

import java.io.IOException;

public interface Page {
    String extractPage() throws IOException;
}
