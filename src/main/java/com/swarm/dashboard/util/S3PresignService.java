package com.swarm.dashboard.util;

import com.swarm.dashboard.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3PresignService {

    private final S3Presigner presigner;
    private final S3Properties s3Properties;

    /**
     * Lambda가 보낸 presigned URL에서 S3 object key를 추출한다.
     * 예: https://bucket.s3.region.amazonaws.com/raw/logs/.../screenshots/enc.png?X-Amz-...
     *   → raw/logs/.../screenshots/enc.png
     *
     * presigned URL이 아닌 경우(이미 key 형태) 그대로 반환한다.
     */
    public String extractKey(String presignedUrl) {
        if (presignedUrl == null || presignedUrl.isBlank()) return presignedUrl;
        if (!presignedUrl.startsWith("http")) return presignedUrl;

        try {
            String path = URI.create(presignedUrl).getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            return presignedUrl;
        }
    }

    /**
     * S3 object key로 fresh presigned URL을 생성한다 (유효시간: application.yaml 설정값).
     * key가 null/blank이거나 http로 시작하는 경우(이미 URL) 그대로 반환한다.
     */
    public String presign(String key) {
        if (key == null || key.isBlank()) return key;
        if (key.startsWith("http")) return key;

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(s3Properties.presignedUrlDuration()))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .build())
            .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
