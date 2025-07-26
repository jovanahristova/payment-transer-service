package com.example.payment_transfer_service.repository;

import com.example.payment_transfer_service.entity.Account;
import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.status = 'ACTIVE'")
    Optional<Account> findActiveAccountById(@Param("id") String id);

    // User-specific account queries
    List<Account> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Account> findByUserIdAndStatus(String userId, AccountStatus status);

    List<Account> findByUserIdAndAccountType(String userId, AccountType accountType);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUserId(@Param("userId") String userId);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE' AND a.currency = :currency")
    BigDecimal getTotalBalanceByUserAndCurrency(@Param("userId") String userId, @Param("currency") String currency);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId")
    long countAccountsByUserId(@Param("userId") String userId);

    // Validate account ownership
    @Query("SELECT a FROM Account a WHERE a.id = :accountId AND a.userId = :userId")
    Optional<Account> findByIdAndUserId(@Param("accountId") String accountId, @Param("userId") String userId);

    // Check if user owns both accounts (for internal transfers)
    @Query("SELECT COUNT(a) FROM Account a WHERE a.id IN (:accountId1, :accountId2) AND a.userId = :userId")
    long countAccountsByIdsAndUserId(@Param("accountId1") String accountId1,
                                     @Param("accountId2") String accountId2,
                                     @Param("userId") String userId);
}
