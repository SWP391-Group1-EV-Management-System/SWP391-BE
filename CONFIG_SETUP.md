# README - Configuration Setup

## Cách thiết lập file cấu hình (application.properties)

### Bước 1: Tạo file cấu hình thực tế
1. Copy file `application.properties.example` thành `application.properties`:
   ```bash
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

2. Mở file `application.properties` và điền các thông tin thực tế của bạn:
   - Database username/password
   - Email và app password (tạo app password từ Google Account)
   - MoMo access key và secret key (lấy từ MoMo Developer Portal)
   - OpenCage API key
   - IPN URL (sử dụng ngrok hoặc domain thực)

### Bước 2: Kiểm tra gitignore
File `.gitignore` đã được cấu hình để tự động bỏ qua:
- `src/main/resources/application.properties` (file chứa config thật)
- `src/main/resources/application-*.properties` (các profile khác)
- `.env` (file môi trường local)

### Bước 3: Xóa file application.properties khỏi git history (nếu đã commit trước đó)
Nếu bạn đã từng commit file `application.properties` lên GitHub, cần xóa nó khỏi git history:

```bash
# Xóa file khỏi git tracking (giữ file local)
git rm --cached src/main/resources/application.properties

# Commit thay đổi
git add .gitignore
git add src/main/resources/application.properties.example
git commit -m "feat: Hide sensitive configuration files"

# Push lên GitHub
git push origin main
```

### Lưu ý quan trọng:
- ❌ **KHÔNG BAO GIỜ** commit file `application.properties` có chứa thông tin nhạy cảm
- ✅ Chỉ commit file `application.properties.example` (template không có thông tin thật)
- ✅ Các thành viên khác trong team sẽ copy file `.example` và tự điền config của họ
- ✅ Với production, sử dụng environment variables hoặc secret management tools

## Lấy MoMo UAT Credentials

1. Truy cập: https://developers.momo.vn/
2. Đăng ký tài khoản developer
3. Tạo ứng dụng mới và chọn môi trường **UAT (Test)**
4. Lấy thông tin:
   - Partner Code
   - Access Key
   - Secret Key
5. Điền vào file `application.properties` của bạn

## Sử dụng ngrok cho IPN URL (Development)

```bash
# Cài đặt ngrok: https://ngrok.com/download

# Chạy ngrok
ngrok http 8080

# Copy HTTPS URL và cập nhật vào application.properties
# Ví dụ: https://abc123.ngrok-free.app/api/payment/ipn-handler
```

