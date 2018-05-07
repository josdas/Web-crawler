package ru.ifmo.rain.naumov.crawler.downloader;

import ru.ifmo.rain.naumov.crawler.crawler.Page;

import java.io.IOException;

public interface PageDownloader {
    Page getPage(String url)  throws IOException;
}
