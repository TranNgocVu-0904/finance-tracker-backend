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
@Setter // Use @Getter and @Setter instead of @Data to prevent StackOverflowError caused by circular references in @ManyToOne associations
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Use BigDecimal for monetary values to prevent precision loss and rounding errors associated with floating-point types
    @Column(nullable = false)
    private BigDecimal amount; 

    @Column(length = 500)
    private String description;

    // The date the transaction occurred (date only, excludes time components)
    @Column(nullable = false)
    private LocalDate transactionDate; 

    // Many-to-One association: Multiple transactions are linked to a single authenticated user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many-to-One association: Multiple transactions are classified under a single category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}