package com.swarm.dashboard.util;

import com.swarm.dashboard.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

    // s3:// URI에서 key 추출 후 fetchJson
    public String fetchJsonFromS3Uri(String s3Uri) {
        String key = s3UriToKey(s3Uri);
        return fetchJson(key);
    }

    // prefix 아래 모든 object key 목록 반환
    public List<String> listKeys(String prefix) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(s3Properties.bucket())
                .prefix(prefix)
                .build())
            .contents()
            .stream()
            .map(o -> o.key())
            .filter(k -> k.endsWith(".json"))
            .toList();
    }

    // "s3://bucket/key" → "key"
    public String s3UriToKey(String s3Uri) {
        if (s3Uri == null) return null;
        if (s3Uri.startsWith("s3://")) {
            String withoutScheme = s3Uri.substring(5);
            int slashIdx = withoutScheme.indexOf('/');
            return slashIdx >= 0 ? withoutScheme.substring(slashIdx + 1) : withoutScheme;
        }
        return s3Uri;
    }
}
