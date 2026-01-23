package com.andreyk.practiceproject;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StartupCheck {

    private final S3Client s3Client;

    @PostConstruct
    public void check() {
        s3Client.listBuckets().buckets()
                .forEach(b -> log.info("S3 bucket: {}", b.name()));
    }
}
