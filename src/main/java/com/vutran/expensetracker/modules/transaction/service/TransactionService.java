package com.vutran.expensetracker.modules.transaction.service;

import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.transaction.dto.TransactionRequest;
import com.vutran.expensetracker.modules.transaction.dto.TransactionResponse;
import com.vutran.expensetracker.modules.transaction.entity.Transaction;
import com.vutran.expensetracker.modules.transaction.repository.TransactionRepository;
import com.vutran.expensetracker.modules.user.entity.User;
import com.vutran.expensetracker.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // 1. TẠO GIAO DỊCH MỚI
    public TransactionResponse createTransaction(TransactionRequest request) {
        // Lấy email người dùng đang đăng nhập từ Token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm User trong DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));

        // Tìm Category dựa trên ID mà client gửi lên
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Category!"));

        // BẢO MẬT: Kiểm tra xem Category này có ĐÚNG LÀ CỦA USER ĐANG ĐĂNG NHẬP KHÔNG?
        // Nếu ID của user tạo category KHÁC VỚI ID của user đang đăng nhập -> Chặn ngay!
        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền sử dụng Category của người khác!");
        }

        // Nếu qua được bước bảo mật, tiến hành tạo Entity Transaction
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .user(user)
                .category(category)
                .build();

        // Lưu xuống Database
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Map sang DTO để trả về cho Client
        return mapToResponse(savedTransaction);
    }

    // 2. LẤY DANH SÁCH GIAO DỊCH (SẮP XẾP MỚI NHẤT LÊN ĐẦU)
    public List<TransactionResponse> getAllTransactionsByUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return transactionRepository.findByUserEmailOrderByTransactionDateDesc(email)
                .stream()
                .map(this::mapToResponse) // Gọi hàm phụ ở dưới cho gọn code
                .collect(Collectors.toList());
    }
    
    // 3. XÓA GIAO DỊCH
    public void deleteTransaction(UUID id) {
        // Lấy email người dùng đang đăng nhập
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Tìm giao dịch xem có tồn tại không
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại!"));

        // 2. BẢO MẬT: Kiểm tra xem giao dịch này có thuộc về người dùng đang đăng nhập không
        if (!transaction.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền xóa giao dịch của người khác!");
        }

        // 3. Nếu mọi thứ ổn, tiến hành xóa
        transactionRepository.delete(transaction);
    }

    // Hàm phụ: Chuyển Entity thành Response DTO
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryName(transaction.getCategory().getName()) // Lấy tên Category
                .categoryType(transaction.getCategory().getType()) // Lấy loại (INCOME/EXPENSE)
                .build();
    }
}