package ru.ifmo.rain.naumov.crawler.book;

import ru.ifmo.rain.naumov.crawler.crawler.ConcurrentPageCollector;
import ru.ifmo.rain.naumov.crawler.crawler.InfoExtractor;
import ru.ifmo.rain.naumov.crawler.WebCrawler;
import ru.ifmo.rain.naumov.crawler.downloader.CachingPageDownloader;
import ru.ifmo.rain.naumov.crawler.downloader.PageDownloader;
import ru.ifmo.rain.naumov.crawler.downloader.WebDownloaderImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class WebCrawlerBooks {
    private static final String RESULT_FILE_NAME = "results.txt";
    private static final String PATH_TO_TEMP_DIR = "/tmp/CachingDownloaderBook";

    private static final String START_PAGE = "https://e.lanbook.com/books/917#matematika_0_header";
    private static final int DEPTH = 40;

    private static final String[] TOPIC_SUBSTRINGS = {
            "matematika_0\">Математика</a>",
            "fizika_0\">Физика</a>",
            "informatika_0\">Информатика</a>"
    };
    private static final int START_YEAR = 2013;
    private static final int END_YEAR = 2018;


    public static void main(String[] args) throws IOException {
        Path pathToTemp = Paths.get(PATH_TO_TEMP_DIR);
        Files.createDirectories(pathToTemp);

        WebCheckerBook webCheckerBook = new WebCheckerBook(START_YEAR, END_YEAR + 1, TOPIC_SUBSTRINGS);
        InfoExtractor<String> extractor = new InfoExtractorBook();
        ConcurrentPageCollector<String> pageCollector = new ConcurrentPageCollector<>(webCheckerBook, extractor);
        PageDownloader pageDownloader = new CachingPageDownloader(pathToTemp);
        WebLinksProcessingBook linksProcessing = new WebLinksProcessingBook();
        WebDownloaderImpl<String> webDownloader = new WebDownloaderImpl<>(pageDownloader, pageCollector, linksProcessing);

        try (WebCrawler crawler = new WebCrawler(webDownloader)) {
            crawler.download(START_PAGE, DEPTH);
        }

        try (FileWriter writer = new FileWriter(RESULT_FILE_NAME, false)) {
            Collection<String> books = pageCollector.get();
            writer.write("Total count: " + Integer.toString(books.size()) + "\n\n");
            for (String elem : books) {
                writer.write(elem + "\n\n\n");
            }
        }
    }
}
