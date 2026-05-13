package com.swarm.dashboard.domain.staging;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "staging_payload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagingPayload {

    @EmbeddedId
    private StagingPayloadId id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;  // raw JSON 문자열로 저장

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;
}
