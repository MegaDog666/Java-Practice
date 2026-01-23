package com.andreyk.practiceproject;

import com.andreyk.practiceproject.core.config.DotenvPropertyInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PracticeProjectApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PracticeProjectApplication.class);
        app.addInitializers(new DotenvPropertyInitializer());
        app.run(args);
    }

}
