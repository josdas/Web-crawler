package ru.ifmo.rain.naumov.crawler.downloader;

import java.io.IOException;
import java.util.List;

public interface Document {
    List<String> extractLinks() throws IOException;
}
