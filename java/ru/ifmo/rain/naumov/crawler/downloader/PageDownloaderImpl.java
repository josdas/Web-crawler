package ru.ifmo.rain.naumov.crawler.downloader;

import info.kgeorgiy.java.advanced.crawler.URLUtils;
import ru.ifmo.rain.naumov.crawler.crawler.Page;

import java.io.*;
import java.net.URI;
import java.util.stream.Collectors;

public class PageDownloaderImpl implements PageDownloader {
    public Page getPage(final String url) throws IOException {
        final URI uri = URLUtils.getURI(url);
        String page;
        System.out.println("Downloading " + url);
        try (final InputStream is = uri.toURL().openStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            page = br.lines().collect(Collectors.joining());
        }
        System.out.println("Downloaded " + uri);

        return () -> {
            return page;
        };
    }
}
