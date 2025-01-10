package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "UserTaskStages")
public class UserTaskStage {
    @EmbeddedId
    private UserTaskStageId id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false) // 外键 user_id
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", insertable = false, updatable = false) // 外键 task_id
    private Task task;

    @NotNull
    @Column(name = "stage", nullable = false)
    private Integer stage;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;

    public UserTaskStage() {
        this.id = new UserTaskStageId();
    }

    public UserTaskStage(Integer userId, Integer taskId) {
        this.id = new UserTaskStageId();
        this.id.setUserId(userId);
        this.id.setTaskId(taskId);
    }

}