package com.copilot.backend.entity;



	import jakarta.persistence.*;
	import lombok.*;

	import java.time.LocalDateTime;
	import java.util.List;

	@Entity
	@Table(name = "chat_sessions")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public class ChatSession {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false)
	    private String title;

	    private LocalDateTime createdAt;

	    @ManyToOne
	    @JoinColumn(name = "user_id")
	    private User user;

	    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
	    private List<CodeRequestEntity> codeRequests;

	    @PrePersist
	    public void onCreate() {
	        createdAt = LocalDateTime.now();
	    }
	}




