package com.swarm.dashboard.util;

import com.swarm.dashboard.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FetchService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String fetchJson(String key) {
        log.debug("S3 JSON fetch: bucket={}, key={}", s3Properties.bucket(), key);
        ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .build()
        );
        return bytes.asString(StandardCharsets.UTF_8);
    }
}
