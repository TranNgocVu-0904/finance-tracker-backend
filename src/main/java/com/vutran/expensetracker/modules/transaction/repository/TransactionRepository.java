package com.vutran.expensetracker.modules.transaction.repository;

import com.vutran.expensetracker.modules.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    // Retrieves all transactions associated with a specific user email, ordered by transaction date in descending order (most recent first)
    List<Transaction> findByUserEmailOrderByTransactionDateDesc(String email);

    // Utilizes the COALESCE function to return zero in the absence of transaction records, effectively preventing NullPointerException during aggregation
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.category c WHERE t.user.email = :email AND c.type = :type")
    BigDecimal sumAmountByUserEmailAndCategoryType(@Param("email") String email, @Param("type") String type);

    // Calculates the total transaction amount for a specific user and category type within a defined chronological date range
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.category c " +
           "WHERE t.user.email = :email AND c.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByDateRange(
            @Param("email") String email, 
            @Param("type") String type, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}