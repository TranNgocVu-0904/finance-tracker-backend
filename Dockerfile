# ==========================================
# BƯỚC 1: MÔI TRƯỜNG BUILD (Dùng Maven + Java 21)
# ==========================================
# Sử dụng image Maven chính thức đi kèm Java 21 (Eclipse Temurin)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# TỐI ƯU HÓA: Copy pom.xml vào trước và tải thư viện (Dependencies)
# Việc này giúp Docker "nhớ" (cache) các thư viện. Lần sau bạn sửa code Java, 
# Docker sẽ không phải tải lại mớ thư viện này từ đầu, tiết kiệm rất nhiều thời gian.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ thư mục mã nguồn (src) vào và bắt đầu đóng gói
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# BƯỚC 2: MÔI TRƯỜNG CHẠY (Dùng JRE 21 siêu nhẹ)
# ==========================================
# Chỉ dùng JRE (Java Runtime Environment) bản Alpine (siêu nhẹ, chỉ khoảng ~50MB) 
# thay vì dùng nguyên bộ JDK nặng nề để tiết kiệm RAM cho máy chủ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy đích danh file JAR vừa được tạo ra từ Bước 1 sang Bước 2
COPY --from=build /app/target/expensetracker-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080 cho Backend
EXPOSE 8080

# Câu lệnh khởi động ứng dụng khi Container chạy
ENTRYPOINT ["java", "-jar", "app.jar"]