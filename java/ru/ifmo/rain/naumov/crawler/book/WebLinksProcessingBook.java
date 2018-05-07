package ru.ifmo.rain.naumov.crawler.book;

import ru.ifmo.rain.naumov.crawler.crawler.WebLinksProcessing;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebLinksProcessingBook implements WebLinksProcessing {
    private static final String FLAGS_SPLIT_SYMBOL = "&";
    private static final String PATH_FLAGS_SPLIT_SYMBOL = "?";

    private static final String BOOK_LINK_PREFIX = "https://e.lanbook.com/book/";

    private static String[] ALLOWED_PARAMS_PREFIXES = {
            "page=",
            /*"author=",
            "foundRows=",
            "category_pk=",
            "limit=10",
            "extra="*/
    };

    private static final String[] ALLOWED_LINK_PREFIXES = {
            BOOK_LINK_PREFIX,
            "https://e.lanbook.com/books/917",
            "https://e.lanbook.com/books/918",
            "https://e.lanbook.com/books/1537",
    };

    private static boolean checkPrefix(String str, String[] prefixes) {
        return Arrays.stream(prefixes).anyMatch(str::startsWith);
    }

    private static boolean checkParam(String param) {
        return checkPrefix(param, ALLOWED_PARAMS_PREFIXES);
    }

    @Override
    public boolean checkLink(String link) {
        return checkPrefix(link, ALLOWED_LINK_PREFIXES);
    }

    private String getNormalizedLinkBook(String link) {
        if (link.contains(PATH_FLAGS_SPLIT_SYMBOL)) {
            String[] temp = link.split(Pattern.quote(PATH_FLAGS_SPLIT_SYMBOL), 2);
            return temp[0];
        }
        return link;
    }

    private String getNormalizedLinkPage(String link) {
        if (link.contains(PATH_FLAGS_SPLIT_SYMBOL)) {
            String[] temp = link.split(Pattern.quote(PATH_FLAGS_SPLIT_SYMBOL), 2);
            String path = temp[0];
            String params = temp[1];
            String goodFlags = Arrays.stream(params.split(FLAGS_SPLIT_SYMBOL))
                    .filter(WebLinksProcessingBook::checkParam)
                    .collect(Collectors.joining(FLAGS_SPLIT_SYMBOL));
            return path + (goodFlags.isEmpty() ? "" : PATH_FLAGS_SPLIT_SYMBOL) + goodFlags;
        }
        return link;
    }

    @Override
    public String getNormalizedLink(String link) {
        if (link.startsWith(BOOK_LINK_PREFIX)) {
            return getNormalizedLinkBook(link);
        }
        return getNormalizedLinkPage(link);
    }
}
