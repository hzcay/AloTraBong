# Hướng dẫn sử dụng tính năng Upload Ảnh/Video cho Admin

## Tổng quan
Tính năng upload ảnh/video đã được tích hợp hoàn chỉnh vào admin panel của AloTraBong, cho phép admin upload và quản lý media cho các món ăn.

## Các thành phần đã được tạo

### 1. Backend Services
- **CloudinaryUploadService**: Interface định nghĩa các method upload
- **CloudinaryUploadServiceImpl**: Implementation xử lý upload lên Cloudinary
- **UploadController**: REST API endpoints cho upload file

### 2. Frontend Components
- **Modal Upload**: Form upload với preview và progress bar
- **JavaScript Functions**: Xử lý upload, validation, và hiển thị media
- **Media Management**: CRUD operations cho media

## API Endpoints

### Upload Image
```
POST /api/admin/upload/image
Content-Type: multipart/form-data
Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "alotrabong/images")
```

### Upload Video
```
POST /api/admin/upload/video
Content-Type: multipart/form-data
Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "alotrabong/videos")
```

### Upload Any File
```
POST /api/admin/upload/file
Content-Type: multipart/form-data
Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "alotrabong/files")
```

### Delete File
```
DELETE /api/admin/upload/file/{publicId}
```

## Cách sử dụng trong Admin Panel

### 1. Truy cập Media Tab
- Vào Admin Panel → Menu & Categories
- Click tab "Ảnh & Video"

### 2. Chọn Món ăn
- Chọn món ăn từ dropdown
- Hệ thống sẽ load danh sách media hiện có

### 3. Thêm Media mới
- Click nút "Thêm Ảnh/Video"
- Chọn file từ máy tính
- Hệ thống tự động:
  - Validate file type và size
  - Upload lên Cloudinary
  - Hiển thị preview
  - Tự động detect loại media (IMAGE/VIDEO)

### 4. Quản lý Media
- **Xem**: Preview ảnh/video thumbnail
- **Sửa**: Thay đổi thứ tự hiển thị, loại media
- **Xóa**: Xóa media khỏi hệ thống

## Tính năng nổi bật

### 1. Validation tự động
- **File Type**: Chỉ chấp nhận ảnh (JPG, PNG, GIF) và video (MP4, AVI, MOV)
- **File Size**: 
  - Ảnh: tối đa 10MB
  - Video: tối đa 100MB
- **Auto-detect**: Tự động phát hiện loại media

### 2. Upload với Progress
- Progress bar hiển thị tiến độ upload
- Status text cập nhật real-time
- Error handling chi tiết

### 3. Preview trước khi lưu
- Hiển thị preview ảnh/video ngay khi chọn file
- Thông tin file (tên, kích thước)
- Responsive design

### 4. Quản lý thứ tự
- Sort order để sắp xếp thứ tự hiển thị
- Số nhỏ hơn hiển thị trước

## Cấu hình Cloudinary

Đã được cấu hình sẵn trong `application.properties`:
```properties
cloudinary.cloud_name=dtsuqd2sp
cloudinary.api_key=851399725233298
cloudinary.api_secret=bassjof7KuqQ7MZFcPxHbDKaLlo
```

## Security

- Chỉ ADMIN và BRANCH_MANAGER có thể upload
- Validation file type và size ở cả frontend và backend
- Secure upload với Cloudinary

## Error Handling

- Frontend validation trước khi upload
- Backend validation và error messages
- User-friendly error messages
- Logging chi tiết cho debugging

## File Structure

```
src/main/java/com/example/Alotrabong/
├── service/
│   ├── CloudinaryUploadService.java
│   └── impl/
│       └── CloudinaryUploadServiceImpl.java
├── controller/
│   └── UploadController.java
└── config/
    └── CloudinaryConfig.java

src/main/resources/templates/admin/
└── menu.html (đã được cập nhật)
```

## Testing

Để test tính năng:
1. Start ứng dụng Spring Boot
2. Truy cập `/admin/menu`
3. Login với tài khoản ADMIN hoặc BRANCH_MANAGER
4. Vào tab "Ảnh & Video"
5. Test upload ảnh/video

## Lưu ý

- File được upload vào folder `alotrabong/images` hoặc `alotrabong/videos` trên Cloudinary
- URL trả về là HTTPS secure URL
- File được optimize tự động bởi Cloudinary
- Có thể xóa file từ Cloudinary thông qua API
