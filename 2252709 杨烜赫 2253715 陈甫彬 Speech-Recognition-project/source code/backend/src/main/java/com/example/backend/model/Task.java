package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "task_prompt", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> taskPrompt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creation_date")
    private Instant creationDate;

    @OneToMany(mappedBy = "taskID", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("imageOrder")
    private List<Image> images = new ArrayList<>();

    @Size(max = 255)
    @NotNull
    @Column(name = "image_description", nullable = false)
    private String imageDescription;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'看图说话'")
    @Column(name = "task_name", nullable = false, length = 50)
    private String taskName;

    public Task(Map<String, Object> taskPrompt, Timestamp creationDate) {
        this.taskPrompt = taskPrompt;
        this.creationDate = creationDate.toInstant();
    }

    public Task() {

    }
}