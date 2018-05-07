package ru.ifmo.rain.naumov.crawler.downloader;

import ru.ifmo.rain.naumov.crawler.crawler.PageCollector;
import ru.ifmo.rain.naumov.crawler.crawler.WebLinksProcessing;
import ru.ifmo.rain.naumov.crawler.downloader.Downloader;
import ru.ifmo.rain.naumov.crawler.downloader.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class WebDownloaderImpl<T> implements Downloader {
    private PageDownloader downloader;
    private PageCollector<T> collector;
    private WebLinksProcessing linksProcessing;

    public WebDownloaderImpl(PageDownloader downloader, PageCollector<T> collector, WebLinksProcessing linksProcessing) {
        this.downloader = downloader;
        this.collector = collector;
        this.linksProcessing = linksProcessing;
    }

    private List<String> extractLinks(String url, String page) throws IOException {
        InputStream stream = new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
        final URI uri = URLUtils.getURI(url);
        return URLUtils.extractLinks(uri, stream);
    }

    @Override
    public Document download(String url) {
        return () -> {
            String page = downloader.getPage(url).extractPage();
            List<String> links = extractLinks(url, page).stream()
                    .filter(linksProcessing::checkLink)
                    .map(linksProcessing::getNormalizedLink)
                    .distinct()
                    .collect(Collectors.toList());
            collector.add(url, page);
            System.out.println("Processed " + url);
            return links;
        };
    }
}
