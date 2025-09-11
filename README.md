# AloTraBong - Hệ thống Fast Food với AI

## Tổng quan dự án

AloTraBong là một hệ thống ứng dụng web fast food hiện đại được xây dựng theo kiến trúc **Domain Driven Design (DDD)** và **Clean Architecture**, sử dụng Spring Boot framework. Dự án tích hợp các tính năng AI cho recommendation và chatbot.

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.5.5, Java 17
- **Database**: SQL Server (Production), H2 (Development/Testing)
- **Security**: Spring Security
- **Template Engine**: Thymeleaf
- **ORM**: Spring Data JPA/Hibernate
- **Build Tool**: Maven
- **Documentation**: SpringDoc OpenAPI (Swagger)

## Cấu trúc dự án

```
alotrabong/
├── src/
│   ├── main/
│   │   ├── java/com/alotrabong/
│   │   │   ├── boot/                    # Application Entry Point
│   │   │   ├── config/                  # Configuration Classes
│   │   │   ├── shared/                  # Shared Components
│   │   │   ├── identityaccess/         # User Management Module
│   │   │   ├── catalog/                # Product Catalog Module
│   │   │   ├── ordering/               # Order Management Module
│   │   │   ├── promotion/              # Promotion Module
│   │   │   ├── review/                 # Review System Module
│   │   │   ├── admin/                  # Admin Dashboard Module
│   │   │   └── ai/                     # AI Features Module
│   │   └── resources/
│   │       ├── templates/              # Thymeleaf Templates
│   │       ├── static/                 # Static Resources
│   │       └── application.yml         # Application Configuration
│   └── test/                           # Test Classes
├── target/                             # Compiled Classes
├── pom.xml                            # Maven Dependencies
└── README.md                          # Project Documentation
```

## Chi tiết từng module

### 📁 `/boot` - Application Entry Point
Chứa class khởi động ứng dụng và các controller cơ bản.

**Files:**
- `AlotrabongApplication.java` - Main class khởi động Spring Boot
- `HealthController.java` - Health check endpoint

**Mục đích:** Điểm khởi đầu của ứng dụng, cấu hình scan các package.

---

### 📁 `/config` - Configuration Classes
Chứa các class cấu hình cho toàn bộ ứng dụng.

**Files:**
- `SecurityConfig.java` - Cấu hình bảo mật Spring Security
- `CorsConfig.java` - Cấu hình CORS cho frontend
- `OpenApiConfig.java` - Cấu hình Swagger documentation

**Mục đích:** Tập trung hóa các cấu hình hệ thống.

---

### 📁 `/shared` - Shared Components
Chứa các component dùng chung cho toàn bộ ứng dụng.

**Cấu trúc:**
```
shared/
├── error/                    # Exception Handling
│   ├── AppException.java         # Custom Exception
│   └── GlobalExceptionHandler.java # Global Error Handler
├── kernel/                   # Core Components
│   ├── BaseEntity.java           # Base Entity với audit fields
│   ├── Pagination.java           # Pagination utilities
│   └── Result.java               # API Response wrapper
├── mapper/                   # Object Mapping
└── security/                # Security Utilities
```

**Mục đích:** Tránh code duplicate, cung cấp utilities chung.

---

### 📁 `/identityaccess` - User Management Module
Quản lý người dùng, authentication và authorization.

**Cấu trúc DDD:**
```
identityaccess/
├── api/                      # Controllers (Interface Layer)
│   └── ProfileController.java    # REST API cho profile
├── application/              # Service Layer
│   └── UserService.java          # Business logic
├── domain/                   # Domain Layer
│   └── User.java                 # User Entity
└── infrastructure/           # Data Layer
    └── UserRepository.java       # Data access
```

**Chức năng:**
- Đăng ký/đăng nhập người dùng
- Quản lý profile cá nhân
- Phân quyền người dùng (Customer, Staff, Manager, Admin)
- Xác thực email/phone

---

### 📁 `/catalog` - Product Catalog Module
Quản lý danh mục sản phẩm và menu.

**Cấu trúc DDD:**
```
catalog/
├── api/                      # Product & Category Controllers
├── application/              # Product & Category Services
├── domain/                   # Domain Entities
│   ├── Product.java              # Sản phẩm
│   └── Category.java             # Danh mục
└── infrastructure/           # Repositories
    ├── ProductRepository.java
    └── CategoryRepository.java
```

**Chức năng:**
- Quản lý danh mục sản phẩm
- CRUD sản phẩm với hình ảnh
- Tìm kiếm và lọc sản phẩm
- Quản lý giá và khuyến mãi
- Thông tin dinh dưỡng

---

### 📁 `/ordering` - Order Management Module
Xử lý đặt hàng và quản lý đơn hàng.

**Cấu trúc DDD:**
```
ordering/
├── api/                      # Order Controllers
├── application/              # Order Services
├── domain/                   # Domain Entities
│   ├── Order.java                # Đơn hàng
│   └── OrderItem.java            # Chi tiết đơn hàng
└── infrastructure/           # Repositories
    ├── OrderRepository.java
    └── OrderItemRepository.java
```

**Chức năng:**
- Tạo đơn hàng từ giỏ hàng
- Theo dõi trạng thái đơn hàng
- Tính toán phí giao hàng
- Quản lý thanh toán
- Lịch sử đơn hàng

---

### 📁 `/promotion` - Promotion Module
Quản lý khuyến mãi và mã giảm giá.

**Cấu trúc DDD:**
```
promotion/
├── api/                      # Promotion Controllers
├── application/              # Promotion Services
├── domain/                   # Domain Entities
│   └── Promotion.java            # Khuyến mãi
└── infrastructure/           # Repositories
    └── PromotionRepository.java
```

**Chức năng:**
- Tạo và quản lý mã khuyến mãi
- Áp dụng discount cho đơn hàng
- Quản lý thời gian hiệu lực
- Giới hạn số lượng sử dụng
- Targeting theo nhóm khách hàng

---

### 📁 `/review` - Review System Module
Hệ thống đánh giá và feedback.

**Cấu trúc DDD:**
```
review/
├── api/                      # Review Controllers
├── application/              # Review Services
├── domain/                   # Domain Entities
│   └── Review.java               # Đánh giá
└── infrastructure/           # Repositories
    └── ReviewRepository.java
```

**Chức năng:**
- Đánh giá sản phẩm với sao và comment
- Upload hình ảnh review
- Kiểm duyệt review
- Phản hồi từ admin
- Thống kê rating

---

### 📁 `/admin` - Admin Dashboard Module
Giao diện quản trị cho admin.

**Cấu trúc:**
```
admin/
├── api/                      # Admin Controllers
├── application/              # Admin Services
├── domain/                   # Admin Entities
└── infrastructure/           # Admin Repositories
```

**Chức năng:**
- Dashboard thống kê
- Quản lý users, products, orders
- Báo cáo doanh thu
- Cấu hình hệ thống

---

### 📁 `/ai` - AI Features Module
Tích hợp các tính năng AI.

**Cấu trúc:**
```
ai/
├── rag/                      # RAG Chatbot
│   ├── api/                      # Chat API
│   ├── application/              # Chat Services
│   ├── domain/                   # Chat Entities
│   └── infrastructure/           # Vector DB, LLM integration
└── recommendation/           # Product Recommendation
    ├── api/                      # Recommendation API
    ├── application/              # ML Services
    ├── domain/                   # Recommendation Models
    └── infrastructure/           # ML Pipeline
```

**Chức năng:**
- **RAG Chatbot**: Trả lời câu hỏi khách hàng
- **Recommendation**: Gợi ý sản phẩm dựa trên lịch sử

---

### 📁 `/resources` - Resources
Chứa các file tài nguyên của ứng dụng.

**Cấu trúc:**
```
resources/
├── templates/                # Thymeleaf Templates
│   ├── admin.html                # Admin dashboard page
│   └── user.html                 # User interface page
├── static/                   # Static Files
│   ├── css/                      # Stylesheets
│   ├── js/                       # JavaScript files
│   └── images/                   # Images
└── application.yml           # Configuration file
```

**Mục đích:** Templates cho web UI và static resources.

---

## Kiến trúc DDD trong từng module

Mỗi module business (catalog, ordering, etc.) tuân theo cấu trúc **4 layers** của DDD:

### 1. **API Layer** (`/api`)
- **Mục đích:** Xử lý HTTP requests/responses
- **Chứa:** Controllers, DTOs, Request/Response models
- **Ví dụ:** `ProfileController.java`, `ProductController.java`

### 2. **Application Layer** (`/application`) 
- **Mục đích:** Orchestrate business logic, transaction management
- **Chứa:** Services, Use cases, Application DTOs
- **Ví dụ:** `UserService.java`, `OrderService.java`

### 3. **Domain Layer** (`/domain`)
- **Mục đích:** Core business logic và rules
- **Chứa:** Entities, Value Objects, Domain Services
- **Ví dụ:** `User.java`, `Order.java`, `Product.java`

### 4. **Infrastructure Layer** (`/infrastructure`)
- **Mục đích:** External concerns (database, external APIs)
- **Chứa:** Repositories, External service clients
- **Ví dụ:** `UserRepository.java`, `ProductRepository.java`

## Flow xử lý request

```
HTTP Request → Controller (API) → Service (Application) → Repository (Infrastructure)
                     ↓                    ↓                        ↓
              Validate & Map    →    Business Logic    →      Database Access
                     ↓                    ↓                        ↓
              HTTP Response ←    Return Result    ←      Return Data
```

## Quy tắc dependencies

1. **Domain** không phụ thuộc vào layer nào khác
2. **Application** chỉ phụ thuộc vào Domain
3. **Infrastructure** phụ thuộc vào Domain và Application
4. **API** phụ thuộc vào Application và Domain

## Cách chạy dự án

### Prerequisites
- Java 17+
- Maven 3.6+
- SQL Server (hoặc dùng H2 cho testing)

### Các bước chạy

1. **Clone project:**
```bash
git clone <repository-url>
cd alotrabong
```

2. **Cấu hình database** trong `application.yml`

3. **Build project:**
```bash
mvn clean install
```

4. **Chạy ứng dụng:**
```bash
mvn spring-boot:run
```

5. **Truy cập:**
- Web UI: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

## API Documentation

Dự án sử dụng **SpringDoc OpenAPI** để tự động generate API documentation.

- Swagger UI: `/swagger-ui.html`
- API Docs JSON: `/v3/api-docs`

## Testing

```bash
# Chạy unit tests
mvn test

# Chạy integration tests
mvn verify
```

## Database Schema

Dự án sử dụng **Hibernate DDL auto-update** để tự động tạo/cập nhật database schema.

**Main Tables:**
- `users` - Thông tin người dùng
- `categories` - Danh mục sản phẩm  
- `products` - Sản phẩm
- `orders` - Đơn hàng
- `order_items` - Chi tiết đơn hàng
- `promotions` - Khuyến mãi
- `reviews` - Đánh giá

## Security

- **Authentication:** Session-based (có thể mở rộng sang JWT)
- **Authorization:** Role-based (CUSTOMER, STAFF, MANAGER, ADMIN)
- **CORS:** Configured cho frontend domains
- **CSRF:** Disabled for API endpoints

## Monitoring & Health

- **Health Check:** `/actuator/health`
- **Metrics:** `/actuator/info`
- **Logging:** Console + File (có thể cấu hình)

## Deployment

Dự án có thể deploy lên:
- **Traditional Server:** Export WAR file
- **Container:** Docker image
- **Cloud:** Heroku, AWS, Azure

## Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes với message rõ ràng
4. Push và tạo Pull Request
5. Code review và merge

## Contact

- **Developer:** haha spkt
- **Email:** bacdoan52@gmail.com

---

**Happy Coding! 🍔🚀**
