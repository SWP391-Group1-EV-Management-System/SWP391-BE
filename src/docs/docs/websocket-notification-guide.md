# WebSocket Notification Guide cho Frontend

## Tổng quan
Backend đã được cập nhật để gửi **real-time notifications** khi có thay đổi trong booking/waiting list thông qua WebSocket.

---

## 1. Kết nối WebSocket

### Endpoint
```
ws://localhost:8080/ws
```

### Cấu hình kết nối (JavaScript)
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Tạo SockJS connection
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Kết nối với user-name trong header
stompClient.connect(
    { 'user-name': userId }, // ⚠️ QUAN TRỌNG: Phải gửi userId trong header
    (frame) => {
        console.log('Connected: ' + frame);
        
        // Subscribe các channels sau khi kết nối thành công
        subscribeToNotifications(userId);
    },
    (error) => {
        console.error('Connection error:', error);
    }
);
```

---

## 2. Các Channels cần Subscribe

### Channel 1: `/user/queue/booking-status`
**Mục đích**: Nhận thông báo khi user được chuyển từ waiting list → booking

**Message format**:
```json
{
    "status": "CONFIRMED",
    "bookingId": "ABC12345",
    "message": "Your booking has been confirmed",
    "postId": "POST001"
}
```

**Code subscribe**:
```javascript
stompClient.subscribe('/user/queue/booking-status', (message) => {
    const data = JSON.parse(message.body);
    
    if (data.status === 'CONFIRMED') {
        // ✅ User đã được chuyển sang booking
        console.log('Booking confirmed:', data.bookingId);
        
        // Cập nhật UI: Chuyển từ waiting list screen → booking screen
        navigateToBookingScreen(data.bookingId);
        
        // Hiển thị notification
        showNotification('Booking đã được xác nhận!', 'success');
    }
});
```

---

### Channel 2: `/user/queue/position-update`
**Mục đích**: Nhận cập nhật vị trí mới trong hàng đợi

**Message format**:
```json
{
    "position": 2,
    "postId": "POST001",
    "message": "Your position has been updated to 2"
}
```

**Code subscribe**:
```javascript
stompClient.subscribe('/user/queue/position-update', (message) => {
    const data = JSON.parse(message.body);
    
    // ✅ Cập nhật vị trí trong UI
    console.log('New position:', data.position);
    
    // Update waiting list position display
    updateQueuePosition(data.position);
    
    // Hiển thị notification
    showNotification(`Vị trí mới: ${data.position}`, 'info');
});
```

---

## 3. Flow hoàn chỉnh

### Khi Driver A hủy booking:

1. **Driver B** (đứng đầu waiting list):
   - Nhận message từ `/user/queue/booking-status`
   - Status: `CONFIRMED`
   - → **Tự động chuyển sang màn hình booking**

2. **Driver C** (đứng thứ 2 trong waiting list):
   - Nhận message từ `/user/queue/position-update`
   - Position: `1` (giảm từ 2 → 1)
   - → **Cập nhật hiển thị vị trí mới**

3. **Driver D** (đứng thứ 3):
   - Position: `2` (giảm từ 3 → 2)

---

## 4. Code mẫu đầy đủ (React/Vue)

### React Example
```javascript
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function WaitingListComponent({ userId, postId }) {
    const [position, setPosition] = useState(null);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        // Connect WebSocket
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.connect(
            { 'user-name': userId },
            () => {
                console.log('WebSocket connected');

                // Subscribe booking status
                client.subscribe('/user/queue/booking-status', (message) => {
                    const data = JSON.parse(message.body);
                    if (data.status === 'CONFIRMED') {
                        // Navigate to booking screen
                        window.location.href = `/booking/${data.bookingId}`;
                    }
                });

                // Subscribe position update
                client.subscribe('/user/queue/position-update', (message) => {
                    const data = JSON.parse(message.body);
                    setPosition(data.position);
                });
            },
            (error) => {
                console.error('WebSocket error:', error);
            }
        );

        setStompClient(client);

        // Cleanup on unmount
        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, [userId]);

    return (
        <div>
            <h2>Waiting List</h2>
            {position && <p>Your position: {position}</p>}
        </div>
    );
}
```

### Vue Example
```javascript
export default {
    data() {
        return {
            position: null,
            stompClient: null
        };
    },
    mounted() {
        this.connectWebSocket();
    },
    methods: {
        connectWebSocket() {
            const socket = new SockJS('http://localhost:8080/ws');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect(
                { 'user-name': this.userId },
                () => {
                    // Subscribe booking status
                    this.stompClient.subscribe('/user/queue/booking-status', (message) => {
                        const data = JSON.parse(message.body);
                        if (data.status === 'CONFIRMED') {
                            this.$router.push(`/booking/${data.bookingId}`);
                        }
                    });

                    // Subscribe position update
                    this.stompClient.subscribe('/user/queue/position-update', (message) => {
                        const data = JSON.parse(message.body);
                        this.position = data.position;
                    });
                }
            );
        }
    },
    beforeUnmount() {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.disconnect();
        }
    }
};
```

---

## 5. Testing

### Test từ Browser Console
```javascript
// Connect
const socket = new SockJS('http://localhost:8080/ws');
const client = Stomp.over(socket);

client.connect(
    { 'user-name': 'USER123' },
    () => {
        console.log('Connected!');
        
        // Subscribe
        client.subscribe('/user/queue/booking-status', (msg) => {
            console.log('Booking status:', JSON.parse(msg.body));
        });
        
        client.subscribe('/user/queue/position-update', (msg) => {
            console.log('Position update:', JSON.parse(msg.body));
        });
    }
);
```

---

## 6. Troubleshooting

### Không nhận được message?
1. ✅ Kiểm tra `user-name` trong STOMP header có đúng userId không
2. ✅ Kiểm tra WebSocket connection đã thành công chưa
3. ✅ Kiểm tra subscribe đúng channel chưa
4. ✅ Check console log để debug

### CORS issues?
Backend đã config `setAllowedOriginPatterns("*")` nên không có vấn đề CORS.

---

## 7. Dependencies cần cài đặt

### NPM
```bash
npm install sockjs-client @stomp/stompjs
```

### Yarn
```bash
yarn add sockjs-client @stomp/stompjs
```

---

## 8. Tóm tắt

| Sự kiện | Channel | Payload | Action |
|---------|---------|---------|--------|
| User được chuyển sang booking | `/user/queue/booking-status` | `{ status, bookingId, message, postId }` | Navigate to booking screen |
| Vị trí trong queue thay đổi | `/user/queue/position-update` | `{ position, postId, message }` | Update position display |

---

## Liên hệ
Nếu có vấn đề, liên hệ Backend team để debug.

