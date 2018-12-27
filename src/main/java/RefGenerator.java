import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

public class RefGenerator {

    public static String[] generate(int count) {
        String[] urls = new String[count];
        for (int i = 0; i < count; i++) {
            urls[i] = "https://prnt.sc/" + RandomStringUtils.random(6, true, true).toLowerCase();
        }
        return urls;
    }

    public static void main(String[] args) {
        String[] urls = generate(10);
        Random random = new Random();
        long start = System.nanoTime();
        Arrays.stream(urls).forEach(url -> {
            System.out.println(url);
            try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println("Working time: " + (end - start)/1e+9 + " sec");
    }
}
