package ru.ifmo.rain.naumov.crawler;
import ru.ifmo.rain.naumov.crawler.crawler.Result;
import ru.ifmo.rain.naumov.crawler.downloader.CachingDownloader;
import ru.ifmo.rain.naumov.crawler.downloader.Document;
import ru.ifmo.rain.naumov.crawler.downloader.Downloader;
import ru.ifmo.rain.naumov.crawler.downloader.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;


public class WebCrawler implements ru.ifmo.rain.naumov.crawler.crawler.Crawler {
    private final static int BASE_DOWNLOADER_COUNT = 32;
    private final static int BASE_EXTRACTOR_COUNT = 32;
    private final static int BASE_LIMIT_PER_HOST = 32;

    private final Downloader downloader;
    private final int limitPerHost;
    private final ExecutorService downloaderPool;
    private final ExecutorService extractorPool;
    private final Map<String, HostHandler> hosts;


    public WebCrawler(Downloader downloader, int downloaderCount, int extractorsCount, int limitPerHost) {
        this.downloader = downloader;
        this.limitPerHost = limitPerHost;
        downloaderPool = Executors.newFixedThreadPool(downloaderCount);
        extractorPool = Executors.newFixedThreadPool(extractorsCount);
        hosts = new ConcurrentHashMap<>();
    }

    public WebCrawler(Downloader downloader) {
        this(downloader, BASE_DOWNLOADER_COUNT, BASE_EXTRACTOR_COUNT, BASE_LIMIT_PER_HOST);
    }

    public WebCrawler(int downloaderCount, int extractorsCount, int limitPerHost) throws IOException {
        this(new CachingDownloader(), downloaderCount, extractorsCount, limitPerHost);
    }

    public WebCrawler(int downloaderCount, int extractors) throws IOException {
        this(downloaderCount, extractors, BASE_LIMIT_PER_HOST);
    }

    public WebCrawler(int downloaderCount) throws IOException {
        this(downloaderCount, BASE_EXTRACTOR_COUNT);
    }

    public WebCrawler() throws IOException {
        this(BASE_DOWNLOADER_COUNT);
    }

    private class HostHandler {
        private final Queue<Runnable> queue = new ArrayDeque<>();
        private int count;
        private final Phaser sync;

        HostHandler(Phaser sync) {
            this.sync = sync;
            this.count = 0;
        }

        private void submit(Runnable task) {
            sync.register();
            downloaderPool.submit(task);
        }

        synchronized void add(Runnable task) {
            if (count < limitPerHost) {
                submit(task);
                count++;
            } else {
                queue.add(task);
            }
        }

        synchronized void done() {
            if (!queue.isEmpty()) {
                submit(queue.poll());
            } else {
                count--;
            }
        }
    }

    private HostHandler getHost(String url, Phaser sync) throws MalformedURLException {
        String hostUrl = URLUtils.getHost(url);
        return hosts.computeIfAbsent(hostUrl, t -> new HostHandler(sync));
    }

    private Runnable createExtractTask(Document page, int depth, Set<String> resultPages,
                                       Map<String, IOException> errors, Set<String> used, Phaser sync) {
        return () -> {
            try {
                for (String url : page.extractLinks()) {
                    if (used.add(url)) {
                        try {
                            HostHandler host = getHost(url, sync);
                            Runnable task = createDownloadTask(url, host, depth, resultPages, errors, used, sync);
                            host.add(task);
                        } catch (MalformedURLException e) {
                            errors.put(url, e);
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                sync.arrive();
            }
        };
    }

    private Runnable createDownloadTask(String url, HostHandler host, int depth, Set<String> resultPages,
                                        Map<String, IOException> errors, Set<String> used, Phaser sync) {
        return () -> {
            try {
                final Document page = downloader.download(url);
                resultPages.add(url);
                if (depth > 1) {
                    sync.register();
                    Runnable task = createExtractTask(page, depth - 1, resultPages, errors, used, sync);
                    extractorPool.submit(task);
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                host.done();
                sync.arrive();
            }
        };
    }

    @Override
    public ru.ifmo.rain.naumov.crawler.crawler.Result download(String url, int depth) {
        final Set<String> resultPages = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> used = ConcurrentHashMap.newKeySet();
        final Phaser sync = new Phaser(1);
        used.add(url);
        try {
            HostHandler host = getHost(url, sync);
            Runnable task = createDownloadTask(url, host, depth, resultPages, errors, used, sync);
            host.add(task);
            sync.arriveAndAwaitAdvance();
        } catch (MalformedURLException e) {
            errors.put(url, e);
        }

        return new Result(new ArrayList<>(resultPages), errors);
    }

    @Override
    public void close() {
        downloaderPool.shutdownNow();
        extractorPool.shutdownNow();
    }

    private final static String[] ARGS_NAME = {"depth", "downloaderCount", "extractorsCount", "limitPerHost"};

    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 5) {
            System.out.println("From two to five arguments are required");
            return;
        }
        for (final String arg : args) {
            if (arg == null) {
                System.out.println("non-null arguments expected");
            }
        }

        Map<String, Integer> parsed_args = new HashMap<>();
        try {
            for (int i = 1; i < args.length; i++) {
                int parsed_arg = Integer.parseInt(args[i]);
                parsed_args.put(ARGS_NAME[i - 1], parsed_arg);
            }
        } catch (NumberFormatException e) {
            System.out.println("The integer numbers expected: " + e.getMessage());
            return;
        }

        try (WebCrawler crawler = createCrawler(parsed_args)) {
            crawler.download(args[0], parsed_args.get("depth"));
        } catch (IOException e) {
            System.out.println("Can not create instance of WebCrawler: " + e.getMessage());
        }
    }

    private static WebCrawler createCrawler(Map<String, Integer> parsed_args) throws IOException {
        switch (parsed_args.size()) {
            case 4:
                return new WebCrawler(parsed_args.get("downloaderCount"),
                        parsed_args.get("extractorsCount"), parsed_args.get("limitPerHost"));
            case 3:
                return new WebCrawler(parsed_args.get("downloaderCount"), parsed_args.get("extractorsCount"));
            case 2:
                return new WebCrawler(parsed_args.get("downloaderCount"));
            default:
                return new WebCrawler();
        }
    }
}
