package com.vutran.expensetracker.modules.transaction.entity;

import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter // Dùng Getter/Setter thay vì @Data cho Entity để tránh lỗi vòng lặp bộ nhớ (StackOverflow) với @ManyToOne
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // QUAN TRỌNG: Luôn dùng BigDecimal cho tiền bạc để tránh sai số thập phân của double/float
    @Column(nullable = false)
    private BigDecimal amount; 

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate; // Ngày thực hiện giao dịch (chỉ lấy ngày, không lấy giờ)

    // Quan hệ N-1: Nhiều Giao dịch thuộc về 1 Người dùng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ N-1: Nhiều Giao dịch thuộc về 1 Danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}