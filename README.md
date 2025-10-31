# 🍚 AloTraBong - Hệ thống đặt cơm trực tuyến đa chi nhánh

<div align="center">

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf"/>
<img src="https://img.shields.io/badge/SQL_Server-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white" alt="SQL Server"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"/>
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white" alt="JWT"/>
<img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white" alt="WebSocket"/>

**Một quán cơm bình dân vibe chill nhất phố** 🍚✨

[Features](#-tính-năng-chính) • [Tech Stack](#-công-nghệ-sử-dụng) • [Installation](#-cài-đặt) • [API Endpoints](#-api-endpoints)

</div>

---

## 📖 Giới thiệu

**AloTraBong** là hệ thống website đặt cơm trực tuyến đa chi nhánh được xây dựng bằng **Spring Boot**, hỗ trợ quản lý toàn diện từ việc đặt hàng, thanh toán, đến vận hành chi nhánh và giao hàng.

### ✨ Đặc điểm nổi bật

- 🏪 **Đa chi nhánh**: Quản lý nhiều chi nhánh độc lập, mỗi chi nhánh có thực đơn và giá riêng
- 🔐 **Bảo mật cao**: Spring Security + JWT + OTP email + BCrypt encryption
- 💬 **Chat realtime**: WebSocket hỗ trợ tư vấn khách hàng trực tuyến
- 🤖 **AI Chatbot**: Tích hợp Gemini AI để hỗ trợ khách hàng tự động
- 💳 **Thanh toán đa dạng**: COD, VNPay, MOMO
- 📊 **Phân quyền RBAC**: 5 vai trò (Admin, Branch Manager, Shipper, User, Guest)
- 📦 **Quản lý đơn hàng**: Theo dõi đơn từ khi đặt đến khi giao
- 🎫 **Khuyến mãi**: Coupon, voucher, giảm giá động

---

## 🎯 Tính năng chính

### 👤 **Guest** (Khách vãng lai)
- Xem menu, thông tin chi nhánh
- Đăng ký tài khoản với OTP email
- Responsive trên mọi thiết bị

### 👨‍👩‍👧‍👦 **User** (Khách hàng)
- Đặt món, quản lý giỏ hàng
- Quản lý nhiều địa chỉ giao hàng
- Thanh toán: COD, VNPay, MOMO
- Theo dõi lịch sử đơn hàng
- Đánh giá sản phẩm (text + hình ảnh)
- Quản lý sản phẩm yêu thích
- Chat realtime với chi nhánh
- Sử dụng mã giảm giá
- Tích hợp AI Chatbot

### 🏪 **Branch Manager** (Quản lý chi nhánh)
- Dashboard thống kê doanh thu
- Quản lý thực đơn: CRUD món ăn, giá, tồn kho
- Quản lý đơn hàng: Cập nhật trạng thái, phân công shipper
- Quản lý shipper: Thêm, sửa, xem lịch sử giao hàng
- Quản lý khuyến mãi chi nhánh
- Thống kê báo cáo: Ngày/tuần/tháng
- Chat với khách hàng

### 🚚 **Shipper** (Nhân viên giao hàng)
- Xem danh sách đơn được phân công
- Cập nhật trạng thái giao hàng
- Thống kê cá nhân: Số đơn, tỷ lệ thành công
- Lịch sử giao hàng chi tiết

### 🛠️ **Admin** (Quản trị hệ thống)
- Dashboard tổng quan hệ thống
- Quản lý user: Khóa/mở, phân quyền
- Quản lý chi nhánh: CRUD, kích hoạt/tạm ngưng
- Quản lý danh mục sản phẩm
- Quản lý khuyến mãi hệ thống
- Quản lý vận chuyển: Phí ship, shipper
- Thống kê doanh thu toàn hệ thống
- Thiết lập chiết khấu chi nhánh

---

## 🛠 Công nghệ sử dụng

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Security**: Spring Security + JWT + OTP
- **ORM**: Hibernate/JPA
- **Template Engine**: Thymeleaf
- **Validation**: Bean Validation

### Database & Cache
- **Database**: SQL Server 2022
- **Cache**: Redis 7
- **Connection**: HikariCP

### Messaging & Realtime
- **WebSocket**: Spring WebSocket (STOMP)
- **Email**: JavaMailSender (SMTP)

### Payment Integration
- **VNPay**: Thanh toán trực tuyến
- **MOMO**: Thanh toán điện tử

### AI & Storage
- **AI Chatbot**: Google Gemini API (gemini-2.5-flash)
- **Image Storage**: Cloudinary

### DevOps
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven
- **Monitoring**: Actuator

### Development Tools
- **Lombok**: Giảm boilerplate code
- **DevTools**: Hot reload
- **Swagger/OpenAPI**: API documentation
- **SpringDoc**: Auto-generated API docs

---

## 📋 Yêu cầu hệ thống

- **Java**: JDK 17+
- **Maven**: 3.6+
- **Docker** & **Docker Compose**: Latest
- **IDE**: IntelliJ IDEA / VS Code / Eclipse

---

## 🚀 Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/yourusername/AloTraBong.git
cd AloTraBong
```

### 2. Chạy với Docker (Khuyên dùng)

```bash
# Khởi động tất cả services (SQL Server + Redis + App)
docker-compose up -d --build

# Xem logs
docker-compose logs -f app

# Dừng services
docker-compose down

# Rebuild khi có thay đổi code
docker-compose down
docker-compose up -d --build
```

**Lưu ý**: 
- Lần đầu chạy nên dùng `--build` để build lại Docker image
- Ứng dụng sử dụng profile `docker` khi chạy trong Docker
- Database và Redis sẽ tự động được tạo nếu chưa có

### 3. Chạy thủ công

#### Bước 1: Chuẩn bị database

```bash
# Chạy SQL Server
docker-compose up -d sqlserver

# Chạy Redis
docker-compose up -d redis
```

#### Bước 2: Cấu hình application.properties

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=Alotrabong
spring.datasource.username=sa
spring.datasource.password=123456@Abc

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Email (Gmail)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Gemini AI
gemini.api.key=your-gemini-api-key

# Cloudinary
cloudinary.cloud_name=your-cloud-name
cloudinary.api_key=your-api-key
cloudinary.api_secret=your-api-secret
```

#### Bước 3: Build và chạy

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### 4. Truy cập ứng dụng

- **Web App**: http://localhost:8080
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

---

## 📂 Cấu trúc dự án

```
AloTraBong/
├── src/main/
│   ├── java/com/example/Alotrabong/
│   │   ├── config/          # Cấu hình (Security, JWT, Redis, ...)
│   │   ├── controller/      # REST Controllers (27 controllers)
│   │   ├── dto/             # Data Transfer Objects (45+ DTOs)
│   │   ├── entity/          # JPA Entities (49 entities)
│   │   ├── repository/      # JPA Repositories (30 repositories)
│   │   ├── service/         # Business Logic (60+ services)
│   │   ├── exception/       # Custom Exceptions
│   │   ├── scheduler/       # Scheduled Tasks
│   │   └── AlotrabongApplication.java
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       │   ├── admin/       # Admin pages
│       │   ├── user/        # User pages
│       │   ├── branch-manager/  # Branch Manager pages
│       │   └── fragments/   # Reusable fragments
│       ├── static/          # CSS, JS, Images
│       ├── application.properties         # Local config
│       └── application-docker.properties  # Docker config
├── docker-compose.yml       # Docker orchestration
├── Dockerfile              # Docker image
├── .dockerignore           # Docker ignore rules
├── pom.xml                # Maven dependencies
└── README.md              # Tài liệu
```

---

## 🔐 Phân quyền (RBAC)

| Role | Quyền truy cập |
|------|----------------|
| **Admin** | Toàn quyền hệ thống |
| **Branch Manager** | Quản lý chi nhánh được phân công |
| **Shipper** | Xem và cập nhật đơn được phân công |
| **User** | Đặt món, thanh toán, xem đơn của mình |
| **Guest** | Xem menu, đăng ký |

### Authentication Flow

1. **Đăng ký**: Email + OTP verification
2. **Đăng nhập**: Email + Password → JWT token
3. **Reset Password**: Email + OTP → Set new password
4. **Session**: HTTP Session cho web, JWT cho API

---

## 📡 API Endpoints

### Authentication
- `POST /auth/register` - Đăng ký
- `POST /auth/login` - Đăng nhập
- `POST /auth/verify-otp` - Xác thực OTP
- `POST /auth/forgot-password` - Quên mật khẩu
- `POST /auth/reset-password` - Reset mật khẩu

### User APIs
- `GET /api/user/profile` - Lấy profile
- `PUT /api/user/profile` - Cập nhật profile
- `PUT /api/user/change-password` - Đổi mật khẩu
- `GET /api/user/orders` - Lịch sử đơn hàng
- `POST /api/user/cart/add` - Thêm vào giỏ
- `POST /api/user/checkout` - Thanh toán

### Admin APIs
- `GET /admin/api/dashboard` - Dashboard
- `GET /admin/api/users` - Quản lý users
- `GET /admin/api/branches` - Quản lý chi nhánh
- `GET /admin/api/revenue` - Thống kê doanh thu

### Branch Manager APIs
- `GET /branch-manager/api/dashboard` - Dashboard
- `GET /branch-manager/api/orders` - Quản lý đơn hàng
- `POST /branch-manager/api/orders/{id}/assign-shipper` - Phân công shipper

Xem đầy đủ tại: http://localhost:8080/swagger-ui.html

---

## 🧪 Testing

### Chạy unit tests

```bash
mvn test
```

### Test Accounts

Để tạo tài khoản test cho các role khác nhau:

#### Bước 1: Truy cập trang tạo test users

Mở trình duyệt và điều hướng đến:

```
http://localhost:8080/test/create-users
```

#### Bước 2: Click nút "Create Test Users"

Bạn sẽ thấy form hiển thị thông tin 3 tài khoản test:
- ✅ **Admin**: Email `admin@test.com` | Password `admin123`
- ✅ **Branch Manager**: Email `branch@test.com` | Password `branch123`
- ✅ **Shipper**: Email `shipper@test.com` | Password `shipper123`

Click nút **"Create Test Users"** để tạo các tài khoản này.

#### Bước 3: Đăng nhập

Sau khi tạo xong, chuyển đến trang đăng nhập:

```
http://localhost:8080/auth
```

Đăng nhập với một trong các tài khoản test ở trên.

### Test Accounts Summary

| Role | Email | Password | Access URL |
|------|-------|----------|------------|
| 🛠️ **Admin** | `admin@test.com` | `admin123` | http://localhost:8080/admin/dashboard |
| 🏪 **Branch Manager** | `branch@test.com` | `branch123` | http://localhost:8080/branch-manager/dashboard |
| 🚚 **Shipper** | `shipper@test.com` | `shipper123` | http://localhost:8080/shipper/dashboard |
| 👤 **User** | Cần đăng ký mới qua `/auth` | - | http://localhost:8080/user/home |

> ⚠️ **Lưu ý**: Các tài khoản test chỉ được tạo 1 lần. Nếu đã tồn tại, hệ thống sẽ bỏ qua.

---

## 🤖 Tính năng nâng cao

### AI Chatbot (Gemini)
- Hỗ trợ khách hàng 24/7
- Đọc database để trả lời về menu, giá, khuyến mãi
- Giọng điệu Gen Z, thân thiện
- Tích hợp widget chat trực tiếp trên web

### WebSocket Chat
- Chat realtime giữa User và Branch Manager
- Lưu lịch sử trò chuyện
- Notification khi có tin nhắn mới

### Coupon Scheduler
- Tự động kích hoạt/vô hiệu hóa coupon theo lịch
- Quét và cập nhật trạng thái coupon mỗi 5 phút

### Redis Caching
- Cache OTP để verify nhanh
- Giảm tải database
- Tăng tốc độ xử lý

---

## 📊 Database Schema

### Các bảng chính

- **users** - Thông tin người dùng
- **user_roles** - Phân quyền
- **user_otps** - Mã OTP
- **branches** - Chi nhánh
- **categories** - Danh mục món ăn
- **items** - Sản phẩm
- **branch_items** - Món ăn theo chi nhánh
- **orders** - Đơn hàng
- **order_items** - Chi tiết đơn hàng
- **shipments** - Giao hàng
- **shippers** - Nhân viên giao hàng
- **coupons** - Mã giảm giá
- **reviews** - Đánh giá
- **conversations** - Cuộc trò chuyện
- **messages** - Tin nhắn

---

## 🐳 Docker Services

| Service | Port | Mô tả |
|---------|------|-------|
| **sqlserver** | 1433 | SQL Server database |
| **redis** | 6379 | Redis cache |
| **app** | 8080 | Spring Boot application |

---

## 🔧 Troubleshooting

### Lỗi kết nối database

```bash
# Kiểm tra SQL Server đã chạy
docker ps | grep sqlserver

# Kiểm tra logs
docker-compose logs sqlserver
```

### Lỗi Redis

```bash
# Restart Redis
docker-compose restart redis
```

### Email không gửi được

- Kiểm tra Gmail App Password
- Xác thực 2 lớp phải bật
- Kiểm tra firewall/antivirus

---

## 📝 Changelog

### Version 1.0.0 (2025)
- ✨ Triển khai tính năng cơ bản
- 🤖 Tích hợp Gemini AI Chatbot
- 💬 WebSocket chat realtime
- 💳 Thanh toán VNPay
- 🎫 Hệ thống coupon động
- 📊 Dashboard admin & branch manager
- 🚚 Quản lý shipper và giao hàng

---

## 👥 Đóng góp

Contributions are welcome! Vui lòng:

1. Fork project
2. Tạo branch mới (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Tạo Pull Request

---

## 📄 License

Copyright © 2025 **Nguyễn Văn Hiếu và Dương Thế Vinh**. All rights reserved.

---

<div align="center">

**Made with ❤️ by AloTraBong Team**

🍚 *"Ăn nhanh, tiện lợi, giao tận nơi!"* 🚀

</div>
