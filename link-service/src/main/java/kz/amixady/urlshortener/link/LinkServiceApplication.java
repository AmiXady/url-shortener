package kz.amixady.urlshortener.link;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class LinkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkServiceApplication.class, args);
    }
}
