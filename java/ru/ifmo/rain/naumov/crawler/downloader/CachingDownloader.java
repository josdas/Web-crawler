package ru.ifmo.rain.naumov.crawler.downloader;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class CachingDownloader implements Downloader {
    private static final byte[] OK_MARKER = {'+'};
    private static final byte[] FAIL_MARKER = {'-'};

    private final Path directory;

    public CachingDownloader() throws IOException {
        this(Files.createTempDirectory(CachingDownloader.class.getName()));
    }

    public CachingDownloader(final Path directory) throws IOException {
        this.directory = directory;
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException(directory + " is not a directory");
        }
    }

    public Document download(final String url) throws IOException {
        final URI uri = URLUtils.getURI(url);
        final Path file = directory.resolve(URLEncoder.encode(uri.toString(), "UTF-8"));
        if (Files.notExists(file)) {
            System.out.println("Downloading " + url);
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
            System.out.println("Downloaded " + uri);
        } else {
            System.out.println("Already downloaded " + url);
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
        return () -> {
            try (final InputStream is = Files.newInputStream(file)) {
                return is.read() == FAIL_MARKER[0] ? Collections.emptyList() : URLUtils.extractLinks(uri, is);
            }
        };
    }
}
