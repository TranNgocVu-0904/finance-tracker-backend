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
    
    // Tìm tất cả giao dịch của 1 user (thông qua email) và sắp xếp ngày giảm dần (mới nhất lên đầu)
    List<Transaction> findByUserEmailOrderByTransactionDateDesc(String email);
    // COALESCE(..., 0) giúp trả về số 0 nếu bạn chưa có giao dịch nào (tránh lỗi NullPointerException)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.category c WHERE t.user.email = :email AND c.type = :type")
    BigDecimal sumAmountByUserEmailAndCategoryType(@Param("email") String email, @Param("type") String type);
    // THÊM HÀM NÀY VÀO DƯỚI CÙNG TRONG TRANSACTION REPOSITORY
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.category c " +
           "WHERE t.user.email = :email AND c.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByDateRange(
            @Param("email") String email, 
            @Param("type") String type, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}