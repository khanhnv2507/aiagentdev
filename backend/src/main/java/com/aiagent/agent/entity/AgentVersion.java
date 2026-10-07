package com.aiagent.agent.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "agent_versions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_agent_version",
            columnNames = {"agent_id", "version"}
        )
    }
)
public class AgentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "system_instruction", nullable = false, columnDefinition = "TEXT")
    private String systemInstruction;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "model_provider", length = 50)
    private String modelProvider;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "temperature", precision = 3, scale = 2)
    private BigDecimal temperature;

    @Column(name = "max_tokens")
    private Integer maxTokens;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}