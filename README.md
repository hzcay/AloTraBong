# 🍔 Website Chuỗi Đồ Ăn Nhanh – **AloTraBong**

> 🚀 Dự án xây dựng hệ thống website đặt đồ ăn nhanh trực tuyến, đa chi nhánh, tích hợp chat realtime, thanh toán điện tử và quản lý vận hành toàn diện.

---

## 🧭 I. Giới thiệu tổng quan

### **1️⃣ Tên đề tài**
**Website chuỗi cửa hàng đồ ăn nhanh – AloTraBong**

### **2️⃣ Mô tả ngắn gọn**
**AloTraBong** là hệ thống website cho phép khách hàng:
- Xem **menu**, đặt món, **thanh toán** và **theo dõi đơn hàng** theo từng chi nhánh.
- Hỗ trợ nhiều **vai trò quản lý**: Admin, Branch Manager, Shipper, User, Guest.
- Tích hợp các tính năng hiện đại: chat realtime, thanh toán điện tử, khuyến mãi động, theo dõi giao hàng, bảo mật nâng cao.

### **3️⃣ Mục tiêu dự án**
- Xây dựng hệ thống đặt đồ ăn trực tuyến **đẹp, thân thiện, responsive**.
- Cho phép khách chọn chi nhánh gần nhất, đặt món và thanh toán nhanh chóng.
- Hỗ trợ quản lý **nhiều chi nhánh** trong cùng thương hiệu **AloTraBong**.
- Tích hợp **bảo mật nâng cao**: OTP, mã hóa mật khẩu (BCrypt), JWT, Spring Security.
- Hỗ trợ quy trình đơn hàng hoàn chỉnh: *Tạo → Xác nhận → Giao → Hoàn tất*.

---

## 👥 II. Đối tượng sử dụng và phạm vi hệ thống

| **Vai trò** | **Mô tả khái quát** |
|--------------|----------------------|
| 🧑‍🤝‍🧑 **Guest** | Người dùng chưa đăng nhập, có thể xem menu, chi nhánh. |
| 👤 **User (Khách hàng)** | Đặt món, thanh toán, theo dõi đơn hàng, chat với chi nhánh. |
| 🏪 **Branch Manager (Quản lý chi nhánh)** | Quản lý món ăn, đơn hàng, nhân viên, khuyến mãi tại chi nhánh. |
| 🚚 **Shipper (Nhân viên giao hàng)** | Nhận và giao đơn hàng, cập nhật trạng thái, thống kê doanh thu. |
| 🛠 **Admin (Quản trị hệ thống)** | Quản lý toàn bộ hệ thống, chi nhánh, user, sản phẩm, doanh thu. |

---

## ⚙️ III. Phân tích chức năng theo từng vai trò

### 🧑‍💻 **1. Guest**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Trang chủ | Hiển thị top 10 món bán chạy nhất toàn hệ thống. |
| Tìm kiếm món ăn | Theo tên, danh mục, giá, chi nhánh. |
| Xem chi tiết món | Hình ảnh, giá, mô tả, đánh giá. |
| Đăng ký tài khoản | OTP qua email để kích hoạt tài khoản. |
| Đăng nhập / Đăng xuất | Sử dụng email + mật khẩu. |
| Quên mật khẩu | OTP qua email để khôi phục. |
| Responsive | Giao diện tự động thích ứng trên mọi thiết bị. |

---

### 🍱 **2. User (Khách hàng)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Trang chủ | Hiển thị món theo danh mục: Mới, Bán chạy, Yêu thích. |
| Xem chi tiết món | Ảnh, giá, thông tin dinh dưỡng, review, video. |
| Giỏ hàng | Thêm/xóa món, cập nhật số lượng, lưu theo user_id. |
| Thanh toán | COD, VNPAY, MOMO, lưu hóa đơn. |
| Quản lý địa chỉ | Nhiều địa chỉ, chọn mặc định khi thanh toán. |
| Lịch sử đơn hàng | Theo dõi trạng thái: *Mới – Đã xác nhận – Đang giao – Hoàn tất*. |
| Đánh giá & bình luận | >= 50 ký tự, có thể đính kèm ảnh/video. |
| Sản phẩm yêu thích | Lưu danh sách món ưa thích. |
| Mã giảm giá | Nhập hoặc chọn coupon hợp lệ. |
| Chat realtime | Chat với chi nhánh qua WebSocket/Firebase. |
| Bảo mật | Mật khẩu mã hóa (BCrypt), xác thực JWT. |

---

### 🏪 **3. Branch Manager (Quản lý chi nhánh)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Dashboard chi nhánh | Thống kê doanh thu, số đơn, top món bán chạy. |
| Quản lý sản phẩm | CRUD món ăn, tồn kho, giá, hình ảnh. |
| Quản lý đơn hàng | Cập nhật trạng thái: *Mới → Xác nhận → Giao → Hoàn tất*. |
| Quản lý khuyến mãi | Tạo chương trình riêng cho chi nhánh. |
| Quản lý nhân viên | Thêm/sửa/xóa nhân viên (tùy chọn). |
| Thống kê doanh thu | Báo cáo theo ngày/tuần/tháng. |
| Chat với khách hàng | Chat realtime với User. |

---

### 🚚 **4. Shipper (Nhân viên giao hàng)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Danh sách đơn hàng | Nhận danh sách đơn được phân công. |
| Cập nhật trạng thái | *Đã nhận – Đang giao – Đã giao – Hủy*. |
| Thống kê đơn hàng | Tổng đơn, đơn hủy, doanh thu cá nhân. |
| Theo dõi vị trí giao hàng | (Tùy chọn) Sử dụng Google Maps API. |
| Báo cáo sự cố | Gửi phản hồi khi có vấn đề. |

---

### 🛠 **5. Admin (Quản trị hệ thống)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Quản lý tài khoản User | Khóa/mở, reset mật khẩu, phân quyền. |
| Quản lý chi nhánh | Tạo, cập nhật, tạm ngưng/kích hoạt chi nhánh. |
| Quản lý danh mục món ăn | CRUD danh mục (Gà rán, Pizza, Burger, Đồ uống,...). |
| Quản lý sản phẩm | Theo dõi và can thiệp dữ liệu toàn hệ thống. |
| Quản lý khuyến mãi hệ thống | Giảm giá %, miễn phí ship,... |
| Quản lý vận chuyển | Cấu hình đơn vị & phí ship theo khu vực. |
| Quản lý chiết khấu | Thiết lập chiết khấu giữa hệ thống ↔ chi nhánh. |
| Thống kê tổng doanh thu | Theo chi nhánh, món, thời gian. |
| Phân quyền | Gán quyền Branch Manager / Shipper / User. |

---

## 💡 IV. Tính năng nâng cao & sáng tạo (Bonus)

| **Tính năng** | **Mô tả** | **Mục tiêu** |
|----------------|------------|---------------|
| 💬 Chat realtime | Dùng WebSocket/Firebase giữa User ↔ Chi nhánh | Tăng tương tác, trải nghiệm người dùng |
| 🗺 Theo dõi vị trí giao hàng | Shipper chia sẻ định vị bản đồ | Theo dõi đơn hàng realtime |
| 🔔 Push Notification | Gửi thông báo khi đơn thay đổi trạng thái | Tăng sự chuyên nghiệp |
| 🚗 Tính phí vận chuyển động | Tự động tính phí theo km hoặc khu vực | Mô phỏng thực tế hệ thống giao hàng |

---

## 🧱 Công nghệ gợi ý
- **Frontend:** HTML / CSS / JS   
- **Backend:** Spring Boot 
- **Database:** SQLserver / PostgreSQL 
- **Realtime:** WebSocket / Firebase Realtime Database  
- **Authentication:** JWT / OAuth2 / OTP qua email  
- **Payment Integration:** VNPAY / MOMO API  

---

---

## 🏁 Kết luận
Dự án **AloTraBong** hướng tới việc mô phỏng hệ thống đặt đồ ăn nhanh **thực tế, tiện ích, an toàn và hiện đại**, với trải nghiệm người dùng mượt mà và quản trị đa tầng linh hoạt.

> 🧾 *“AloTraBong – Ăn nhanh, tiện lợi, giao tận nơi!”* 🚀

---

📌 **© 2025 AloTraBong Team** | *All rights reserved.*

