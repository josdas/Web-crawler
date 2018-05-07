package ru.ifmo.rain.naumov.crawler.downloader;
import ru.ifmo.rain.naumov.crawler.crawler.Page;


import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class CachingPageDownloader implements PageDownloader {
    private static final byte[] OK_MARKER = {'+'};
    private static final byte[] FAIL_MARKER = {'-'};

    private final Path directory;

    public CachingPageDownloader() throws IOException {
        this(Files.createTempDirectory(CachingPageDownloader.class.getName()));
    }

    public CachingPageDownloader(final Path directory) throws IOException {
        System.out.println(directory.toString());
        this.directory = directory;
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException(directory + " is not a directory");
        }
    }

    private Path cachingDownload(final String url, boolean printLog) throws IOException {
        final URI uri = URLUtils.getURI(url);
        final Path file = directory.resolve(URLEncoder.encode(uri.toString(), "UTF-8"));

        if (Files.notExists(file)) {
            if (printLog) {
                System.out.println("Downloading " + url);
            }
            try {
                try (final InputStream is = uri.toURL().openStream()) {
                    Files.copy(new SequenceInputStream(new ByteArrayInputStream(OK_MARKER), is), file);
                }
            } catch (final IOException e) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(FAIL_MARKER);
                try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                    oos.writeObject(e);
                }
                Files.copy(new ByteArrayInputStream(out.toByteArray()), file);
                throw e;
            }
            if (printLog) {
                System.out.println("Downloaded " + uri);
            }
        } else {
            if (printLog) {
                System.out.println("OK " + uri);
            }
            try (final InputStream is = Files.newInputStream(file)) {
                if (is.read() == FAIL_MARKER[0]) {
                    try (ObjectInputStream ois = new ObjectInputStream(is)) {
                        throw (IOException) ois.readObject();
                    } catch (final ClassNotFoundException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        return file;
    }

    public Page getPage(final String url) throws IOException {
        final Path file = cachingDownload(url, true);
        return () -> {
            try (final BufferedReader is = Files.newBufferedReader(file)) {
                return is.read() == FAIL_MARKER[0] ? "" : is.lines().collect(Collectors.joining());
            }
        };
    }
}
