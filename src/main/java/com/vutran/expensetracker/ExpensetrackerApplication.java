package com.vutran.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ExpensetrackerApplication {

	

	public static void main(String[] args) {
    try {
        // 1. Nạp file .env
        Dotenv dotenv = Dotenv.configure()
                .directory("./") // Chỉ định tìm ở thư mục gốc
                .ignoreIfMissing() // Tránh crash nếu lỡ tay xóa file
                .load();

        // 2. ÉP các biến từ .env vào System Properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            // Dòng này để Vũ debug xem nó có nạp được gì không
            if (entry.getKey().contains("MAIL")) {
                System.out.println("Loaded from .env: " + entry.getKey());
            }
        });
    } catch (Exception e) {
        System.out.println("⚠️ Cảnh báo: Không tìm thấy file .env, sẽ dùng cấu hình mặc định.");
    }

    // 3. Chạy Spring Boot
    SpringApplication.run(ExpensetrackerApplication.class, args);
}

}
