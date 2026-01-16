package com.project.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CsvCache {

    private final Map<String, String> cache = new HashMap<>();

    public CsvCache(String csvFileName) throws Exception {


        InputStream is = CsvCache.class.getClassLoader()
                .getResourceAsStream(csvFileName);

        if (is == null) {
            throw new FileNotFoundException("CSV NOT FOUND in resources: " + csvFileName);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",", 3);
            if (parts.length < 3) continue;

            cache.put(parts[1].trim(), parts[0].trim() + "," + parts[2].trim());
        }

        br.close();
    }


    public String getValue(String key) {
        return cache.get(key);
    }


    public List<String> getAllRows() {
        return cache.entrySet().stream()
                .map(e -> e.getKey() + "," + e.getValue())
                .toList();
    }
}
