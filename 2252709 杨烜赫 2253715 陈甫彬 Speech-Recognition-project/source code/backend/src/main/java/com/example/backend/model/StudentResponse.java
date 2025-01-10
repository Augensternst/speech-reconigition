package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
public class StudentResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_id")
    private Task task;

    @Lob
    @Column(name = "response_text")
    private String responseText;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "submission_date")
    private Instant submissionDate;

    @NotNull
    @Column(name = "ai_feedback", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> aiFeedback;

    @PrePersist
    public void prePersist() {
        if (this.submissionDate == null) {
            this.submissionDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant();
        }
    }
}