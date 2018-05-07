package ru.ifmo.rain.naumov.crawler.downloader;

import java.io.IOException;

public interface Downloader {
    Document download(final String url) throws IOException;
}
