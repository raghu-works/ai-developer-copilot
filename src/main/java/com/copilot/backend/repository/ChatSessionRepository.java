package com.copilot.backend.repository;




	import java.util.List;

	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.stereotype.Repository;

	import com.copilot.backend.entity.ChatSession;
	import com.copilot.backend.entity.User;

	@Repository
	public interface ChatSessionRepository
	        extends JpaRepository<ChatSession, Long> {

	    List<ChatSession> findByUser(User user);
	}





