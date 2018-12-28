package com.imageParser;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RefGenerator {

    public static String[] generateUrls(int count) {
        String[] urls = new String[count];
        for (int i = 0; i < count; i++) {
            urls[i] = "https://prnt.sc/" + RandomStringUtils.random(6, true, true).toLowerCase();
        }
        return urls;
    }

    public static void generateParallel(int count) {
        String[] urls = generateUrls(count);
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = new ArrayList<>();
        for (String url : urls) {
            Future<?> future = executorService.submit(() -> {
                try {
                    parseImageToFile(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }
        executorService.shutdown();

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseImageToFile(String url) throws IOException {
        Random random = new Random();
        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .ignoreHttpErrors(true)
                .execute();
        Map<String, String> cookies = response.cookies();
        Document document = response.parse();
        String imgUrl = document.getElementsByClass("screenshot-image").attr("src");
        System.out.println(imgUrl);
        if (!imgUrl.startsWith("//") && !imgUrl.isEmpty()) {
            System.out.println("valid!!!");
            Connection.Response imageResponse = Jsoup.connect(imgUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .cookies(cookies)
                    .ignoreContentType(true)
                    .execute();
            try (FileOutputStream fileOutputStream = new FileOutputStream("image" + random.nextInt() + ".png")) {
                fileOutputStream.write(imageResponse.bodyAsBytes());
            }
        }
    }


    public static void main(String[] args) {
        System.out.print("Enter urls count: ");
        Scanner scanner = new Scanner(System.in);
        int count = scanner.nextInt();
        long start = System.nanoTime();
        generateParallel(count);
        long end = System.nanoTime();
        System.out.println("Working time: " + (end - start)/1e+9 + " sec");
    }
}
