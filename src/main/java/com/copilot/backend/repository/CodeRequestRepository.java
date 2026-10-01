package com.copilot.backend.repository;





	import java.util.List;

	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.stereotype.Repository;

	import com.copilot.backend.entity.ChatSession;
	import com.copilot.backend.entity.CodeRequestEntity;

	@Repository
	public interface CodeRequestRepository
	        extends JpaRepository<CodeRequestEntity, Long> {

	    List<CodeRequestEntity> findBySession(ChatSession session);
	}




