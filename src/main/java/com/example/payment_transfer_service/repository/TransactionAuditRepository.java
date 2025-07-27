package com.example.payment_transfer_service.repository;

import com.example.payment_transfer_service.entity.TransactionAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, String> {

    List<TransactionAudit> findByTransactionIdOrderByCreatedAtDesc(String transactionId);

    Page<TransactionAudit> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT ta FROM TransactionAudit ta WHERE ta.sourceAccountId = :accountId OR ta.destinationAccountId = :accountId ORDER BY ta.createdAt DESC")
    List<TransactionAudit> findByAccountId(@Param("accountId") String accountId);

    @Query("SELECT ta FROM TransactionAudit ta WHERE ta.createdAt >= :startDate AND ta.createdAt <= :endDate ORDER BY ta.createdAt DESC")
    List<TransactionAudit> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<TransactionAudit> findBySuccessOrderByCreatedAtDesc(boolean success);
}
