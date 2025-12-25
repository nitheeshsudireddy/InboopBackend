package com.inboop.backend.instagram.repository;

import com.inboop.backend.instagram.entity.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    Page<WebhookLog> findAllByOrderByReceivedAtDesc(Pageable pageable);

    List<WebhookLog> findTop50ByOrderByReceivedAtDesc();
}
