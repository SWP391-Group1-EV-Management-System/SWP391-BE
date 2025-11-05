# Hướng dẫn Test Tính năng Early Charging Offer với Postman

## 📋 Chuẩn bị

### 1. Cài đặt Postman
- Download tại: https://www.postman.com/downloads/
- Hoặc dùng Postman Web

### 2. Thông tin cần thiết
- **Base URL**: `http://localhost:8080`
- **WebSocket URL**: `ws://localhost:8080/ws`
- **JWT Token**: Lấy từ API login

---

## 🔐 Bước 1: Login để lấy Token

### Request: Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

Body (raw JSON):
{
    "email": "driver1@example.com",
    "password": "password123"
}
```

### Response:
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "USER001"
}
```

**📝 Lưu token này vào Postman Environment:**
- Tạo Environment mới: `EV_Charging_Test`
- Thêm variable: `authToken` = token vừa lấy
- Thêm variable: `userId` = USER001

---

## 🧪 Bước 2: Setup Test Scenario

### Kịch bản test:
1. **Driver A** (USER001) tạo booking và bắt đầu sạc
2. **Driver B** (USER002) vào waiting list
3. **Driver C** (USER003) vào waiting list
4. **Driver A** rút sạc sớm
5. **Driver B** nhận notification (cần WebSocket)
6. **Driver B** đồng ý hoặc từ chối

---

## 📨 Bước 3: Tạo Booking cho Driver A

### Request: Create Booking
```
POST http://localhost:8080/api/booking/create
Authorization: Bearer {{authToken}}
Content-Type: application/json

Body:
{
    "user": "USER001",
    "chargingPost": "POST001",
    "car": "CAR001"
}
```

### Response:
```json
{
    "status": "booking",
    "rank": -1,
    "idAction": "BOOK12345"
}
```

**Lưu bookingId:** `BOOK12345`

---

## 📨 Bước 4: Driver A bắt đầu Session

### Request: Start Charging Session
```
POST http://localhost:8080/api/charging/session/start
Authorization: Bearer {{authToken}}
Content-Type: application/json

Body:
{
    "bookingId": "BOOK12345",
    "expectedEndTime": "2025-11-05T15:00:00"
}
```

### Response:
```json
{
    "sessionId": "SESS67890",
    "startTime": "2025-11-05T14:00:00",
    "expectedEndTime": "2025-11-05T15:00:00"
}
```

**Lưu sessionId:** `SESS67890`

---

## 📨 Bước 5: Driver B và C vào Waiting List

### Request: Driver B (USER002) join waiting list
```
POST http://localhost:8080/api/booking/create
Authorization: Bearer {{authTokenB}}
Content-Type: application/json

Body:
{
    "user": "USER002",
    "chargingPost": "POST001",
    "car": "CAR002"
}
```

### Response:
```json
{
    "status": "waiting",
    "rank": 1,
    "idAction": "WAIT12345"
}
```

### Request: Driver C (USER003) join waiting list
```
POST http://localhost:8080/api/booking/create
Authorization: Bearer {{authTokenC}}
Content-Type: application/json

Body:
{
    "user": "USER003",
    "chargingPost": "POST001",
    "car": "CAR003"
}
```

### Response:
```json
{
    "status": "waiting",
    "rank": 2,
    "idAction": "WAIT67890"
}
```

---

## 📨 Bước 6: Driver A rút sạc sớm (CASE 1)

### Request: End Session sớm
```
POST http://localhost:8080/api/charging/session/end
Authorization: Bearer {{authToken}}
Content-Type: application/json

Body:
{
    "sessionId": "SESS67890"
}
```

### Response:
```json
{
    "success": true,
    "message": "Session ended successfully"
}
```

### ⚠️ Lúc này Backend sẽ:
1. ✅ End session
2. ✅ Tạo payment
3. ✅ **GỬI WEBSOCKET NOTIFICATION ĐẾN DRIVER B**

**📝 Console log Backend:**
```
🔔 [CASE 1] A ended early - Sent offer to driver: USER002 (early by 30 minutes)
```

---

## 🌐 Bước 7: Test WebSocket (Nhận Notification)

### Option 1: Dùng Postman WebSocket (Postman v10+)

1. **Tạo WebSocket Request mới**
   - New → WebSocket Request
   - URL: `ws://localhost:8080/ws`

2. **Connect với STOMP header**
   ```
   CONNECT
   user-name:USER002
   accept-version:1.1,1.0
   heart-beat:10000,10000

   ^@
   ```

3. **Subscribe channel**
   ```
   SUBSCRIBE
   id:sub-0
   destination:/user/queue/early-charging-offer

   ^@
   ```

4. **Nhận message khi A rút sạc:**
   ```json
   {
       "postId": "POST001",
       "message": "Trạm sạc đã sẵn sàng sớm. Bạn có muốn sạc ngay không?",
       "minutesEarly": 30,
       "expectedTime": "2025-11-05T15:00:00",
       "availableNow": true
   }
   ```

### Option 2: Dùng Browser Console

```javascript
// Mở browser tại http://localhost:8080
// F12 → Console

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
    { 'user-name': 'USER002' },
    (frame) => {
        console.log('Connected:', frame);
        
        stompClient.subscribe('/user/queue/early-charging-offer', (message) => {
            console.log('Received offer:', JSON.parse(message.body));
        });
    }
);
```

### Option 3: Dùng WebSocket Client Extension
- Chrome Extension: "Simple WebSocket Client"
- URL: `ws://localhost:8080/ws/websocket`

---

## 📨 Bước 8: Driver B đồng ý sạc sớm

### Request: Accept Early Charging
```
POST http://localhost:8080/api/waiting-list/accept-early-charging/USER002/POST001
Authorization: Bearer {{authTokenB}}
```

### Response:
```
200 OK
Đã chuyển bạn vào booking. Vui lòng đến trạm sạc!
```

### ✅ Kiểm tra kết quả:

**1. Check Driver B đã chuyển sang booking:**
```
GET http://localhost:8080/api/booking/getByPost/POST001
Authorization: Bearer {{authToken}}
```

**Response:**
```json
[
    {
        "bookingId": "BOOK99999",
        "userId": "USER002",
        "status": "CONFIRMED",
        "chargingPostId": "POST001"
    }
]
```

**2. Check Driver C đã lên vị trí 1:**
```
GET http://localhost:8080/api/waiting-list/queue/post/POST001
Authorization: Bearer {{authToken}}
```

**Response:**
```json
[
    {
        "waitingListId": "WAIT67890",
        "userId": "USER003",
        "status": "WAITING"
    }
]
```

---

## 📨 Bước 9: Test CASE - Driver B từ chối

### Request: Decline Early Charging
```
POST http://localhost:8080/api/waiting-list/decline-early-charging/USER002/POST001
Authorization: Bearer {{authTokenB}}
```

### Response:
```
200 OK
Bạn sẽ được thông báo khi đến giờ dự kiến
```

### ✅ Kiểm tra kết quả:

**Driver B vẫn ở vị trí 1 trong waiting list:**
```
GET http://localhost:8080/api/waiting-list/queue/post/POST001
Authorization: Bearer {{authToken}}
```

**Response:**
```json
[
    {
        "waitingListId": "WAIT12345",
        "userId": "USER002",
        "status": "WAITING"
    },
    {
        "waitingListId": "WAIT67890",
        "userId": "USER003",
        "status": "WAITING"
    }
]
```

---

## 📨 Bước 10: Test CASE 2 - Session tự động kết thúc

### Cách 1: Chờ đến đúng giờ (15:00)
- Session sẽ tự động end
- Backend tự động gọi `processBooking()`
- Driver B tự động chuyển vào booking

### Cách 2: Mock đúng giờ (test nhanh)

**Sửa `expectedEndTime` thành thời gian hiện tại:**
```
POST http://localhost:8080/api/charging/session/start
Body:
{
    "bookingId": "BOOK12345",
    "expectedEndTime": "2025-11-05T14:00:10"  // 10 giây sau
}
```

**Sau 10 giây, check:**
```
GET http://localhost:8080/api/booking/getByPost/POST001
```

**Driver B đã tự động vào booking (không cần accept)**

---

## 📋 Postman Collection Template

### Import Collection này vào Postman:

```json
{
    "info": {
        "name": "EV Charging - Early Charging Offer",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "item": [
        {
            "name": "1. Login Driver A",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\n    \"email\": \"driver1@example.com\",\n    \"password\": \"password123\"\n}"
                },
                "url": {
                    "raw": "{{baseUrl}}/api/auth/login",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "auth", "login"]
                }
            }
        },
        {
            "name": "2. Create Booking Driver A",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    },
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\n    \"user\": \"{{userIdA}}\",\n    \"chargingPost\": \"POST001\",\n    \"car\": \"CAR001\"\n}"
                },
                "url": {
                    "raw": "{{baseUrl}}/api/booking/create",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "booking", "create"]
                }
            }
        },
        {
            "name": "3. Start Session Driver A",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\n    \"bookingId\": \"{{bookingIdA}}\",\n    \"expectedEndTime\": \"2025-11-05T15:00:00\"\n}"
                },
                "url": {
                    "raw": "{{baseUrl}}/api/charging/session/start",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "charging", "session", "start"]
                }
            }
        },
        {
            "name": "4. Driver B Join Waiting List",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authTokenB}}"
                    }
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\n    \"user\": \"{{userIdB}}\",\n    \"chargingPost\": \"POST001\",\n    \"car\": \"CAR002\"\n}"
                },
                "url": {
                    "raw": "{{baseUrl}}/api/booking/create",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "booking", "create"]
                }
            }
        },
        {
            "name": "5. End Session Early (A rút sạc sớm)",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\n    \"sessionId\": \"{{sessionId}}\"\n}"
                },
                "url": {
                    "raw": "{{baseUrl}}/api/charging/session/end",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "charging", "session", "end"]
                }
            }
        },
        {
            "name": "6. Accept Early Charging (B đồng ý)",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authTokenB}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/waiting-list/accept-early-charging/{{userIdB}}/POST001",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "waiting-list", "accept-early-charging", "{{userIdB}}", "POST001"]
                }
            }
        },
        {
            "name": "7. Decline Early Charging (B từ chối)",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authTokenB}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/waiting-list/decline-early-charging/{{userIdB}}/POST001",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "waiting-list", "decline-early-charging", "{{userIdB}}", "POST001"]
                }
            }
        }
    ],
    "variable": [
        {
            "key": "baseUrl",
            "value": "http://localhost:8080"
        },
        {
            "key": "authToken",
            "value": ""
        },
        {
            "key": "authTokenB",
            "value": ""
        },
        {
            "key": "userIdA",
            "value": "USER001"
        },
        {
            "key": "userIdB",
            "value": "USER002"
        },
        {
            "key": "bookingIdA",
            "value": ""
        },
        {
            "key": "sessionId",
            "value": ""
        }
    ]
}
```

---

## 🔍 Debug Tips

### 1. Check Backend Logs
```bash
# Xem console log để biết khi nào gửi notification
tail -f logs/application.log

# Tìm messages:
🔔 [CASE 1] A ended early - Sent offer to driver: USER002
✅ [CASE 2] Session ended on time - Automatically processing next booking
```

### 2. Check Redis Queue
```bash
# Connect Redis CLI
redis-cli

# Check waiting list
LRANGE queue:post:POST001 0 -1

# Kết quả:
# 1) "USER002"
# 2) "USER003"
```

### 3. Check Database
```sql
-- Check booking status
SELECT * FROM booking WHERE charging_post_id = 'POST001';

-- Check waiting list
SELECT * FROM waiting_list WHERE charging_post_id = 'POST001' AND status = 'WAITING';
```

---

## ✅ Checklist Test Hoàn chỉnh

- [ ] Driver A tạo booking thành công
- [ ] Driver A start session thành công
- [ ] Driver B, C vào waiting list theo thứ tự
- [ ] Driver A end session sớm
- [ ] Backend log hiển thị "Sent offer to driver: USER002"
- [ ] WebSocket nhận được notification (test bằng browser)
- [ ] Driver B accept → chuyển sang booking
- [ ] Driver C lên vị trí 1
- [ ] Driver B decline → giữ vị trí 1
- [ ] Session auto-end → B tự động vào booking

---

## 🚨 Common Issues

### Issue 1: Không nhận được WebSocket notification
**Giải pháp:**
- Kiểm tra `user-name` trong STOMP header có đúng userId không
- Check WebSocketConfig đã enable chưa
- Xem Backend log có gửi message không

### Issue 2: Accept/Decline trả về 400 Bad Request
**Giải pháp:**
- Kiểm tra userId có đúng với người đầu tiên trong queue không
- Check Redis: `LRANGE queue:post:POST001 0 -1`

### Issue 3: Session không tự động end
**Giải pháp:**
- Kiểm tra scheduled task có chạy không
- Check `expectedEndTime` đã đến chưa

---

Chúc bạn test thành công! 🚀

