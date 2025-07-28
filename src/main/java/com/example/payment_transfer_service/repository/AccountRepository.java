package com.example.payment_transfer_service.repository;

import com.example.payment_transfer_service.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") String id);

    List<Account> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUserId(@Param("userId") String userId);

    @Query("SELECT a FROM Account a WHERE a.id = :accountId AND a.userId = :userId")
    Optional<Account> findByIdAndUserId(@Param("accountId") String accountId, @Param("userId") String userId);

}
