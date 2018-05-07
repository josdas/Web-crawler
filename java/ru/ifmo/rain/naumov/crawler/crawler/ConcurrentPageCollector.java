package ru.ifmo.rain.naumov.crawler.crawler;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentPageCollector<T> implements PageCollector<T> {
    private WebChecker checker;
    private InfoExtractor<T> extractor;
    private Set<T> results;

    public ConcurrentPageCollector(WebChecker checker, InfoExtractor<T> extractor) {
        this.checker = checker;
        this.extractor = extractor;
        this.results = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void add(String url, String page) {
        if (checker.check(page)) {
            results.add(extractor.extract(url, page));
        }
    }

    @Override
    public Collection<T> get() {
        return results;
    }
}
