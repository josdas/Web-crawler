package ru.ifmo.rain.naumov.crawler.book;

import ru.ifmo.rain.naumov.crawler.crawler.InfoExtractor;

public class InfoExtractorBook implements InfoExtractor<String> {
    private final static String BEGIN = "<div id=\"bibliographic_record\">";
    private final static String END = "</div>";
    private final static int MIN_LEN = 70;

    @Override
    public String extract(String url, String page) {
        int l = page.indexOf(BEGIN) + BEGIN.length();
        int r = page.indexOf(END, l);
        String info = page.substring(l, r).trim();
        StringBuilder builder = new StringBuilder();
        int last = 0;
        for (int i = 0; i < info.length(); i++) {
            char cur = info.charAt(i);
            if (last > MIN_LEN && Character.isWhitespace(cur)) {
                builder.append('\n');
                last = 0;
            }
            else {
                builder.append(cur);
                last++;
            }
        }
        return url + "\n" + builder.toString();
    }
}
