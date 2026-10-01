package com.copilot.backend.entity;




import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "code_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String operation;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}







