package com.example.payment_transfer_service.repository;

import com.example.payment_transfer_service.entity.Transaction;
import com.example.payment_transfer_service.entity.TransactionStatus;
import com.example.payment_transfer_service.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Transaction> findByUserIdAndStatus(String userId, TransactionStatus status);

    List<Transaction> findByUserIdAndTransactionType(String userId, TransactionType transactionType);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.createdAt >= :startDate AND t.createdAt <= :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findUserTransactionsBetweenDates(@Param("userId") String userId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    // Account-specific transaction queries
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId = :accountId OR t.destinationAccountId = :accountId) AND t.userId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findAccountTransactionsByUserId(@Param("accountId") String accountId, @Param("userId") String userId);

    // Legacy method (for backward compatibility)
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
            String sourceAccountId, String destinationAccountId);

    // Statistics and reporting
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED' AND t.createdAt >= :startDate")
    long countCompletedTransactionsByUserSince(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED' AND t.transactionType = :type AND t.createdAt >= :startDate")
    BigDecimal sumTransactionAmountByUserAndTypeSince(@Param("userId") String userId,
                                                      @Param("type") TransactionType type,
                                                      @Param("startDate") LocalDateTime startDate);

    @Query("SELECT t FROM Transaction t WHERE t.reference = :reference AND t.userId = :userId")
    List<Transaction> findByReferenceAndUserId(@Param("reference") String reference, @Param("userId") String userId);

    // Check if reference exists for user (prevent duplicate references per user)
    boolean existsByReferenceAndUserId(String reference, String userId);

}
