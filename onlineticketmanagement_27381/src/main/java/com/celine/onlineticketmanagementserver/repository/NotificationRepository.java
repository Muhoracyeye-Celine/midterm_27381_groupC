package com.celine.onlineticketmanagementserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByPersonIdOrderByCreatedAtDesc(Long personId, Pageable pageable);

    Page<Notification> findByPersonIdAndIsReadOrderByCreatedAtDesc(Long personId, Boolean isRead, Pageable pageable);

    Long countByPersonIdAndIsRead(Long personId, Boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.person.id = :personId AND n.isRead = false")
    void markAllAsReadByPersonId(@Param("personId") Long personId);

    void deleteByPersonId(Long personId);
}
