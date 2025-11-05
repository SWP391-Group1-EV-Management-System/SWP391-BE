# Tài liệu: Tính năng Early Charging Offer (Đề nghị sạc sớm)

## 📋 Tổng quan

**CHỈ CÓ 2 TRƯỜNG HỢP ĐỐI VỚI SESSION:**

### CASE 1: A rút sạc sớm
- A tự rút sạc trước giờ dự kiến
- Backend gửi notification hỏi **driver B**: "Bạn có muốn sạc ngay hay đợi đến đúng giờ?"
- B có thể:
  - ✅ **Đồng ý** → Chuyển vào booking ngay
  - ❌ **Từ chối** → Giữ nguyên vị trí, chờ đến đúng giờ (scheduled task sẽ tự động xử lý)

### CASE 2: Đến đúng giờ
- Session tự động kết thúc khi đến `expectedEndTime`
- Backend **TỰ ĐỘNG** chuyển B vào booking (không hỏi)

**LƯU Ý:** Session **KHÔNG THỂ kết thúc muộn** vì sẽ tự động end khi đến `expectedEndTime`.

---

## 🔄 Flow đơn giản

### Kịch bản:
- **Driver A** đang sạc, dự kiến kết thúc lúc **15:00**
- **Driver B** đứng đầu waiting list
- **Driver C** đứng thứ 2

---

### CASE 1: A rút sạc sớm (bất kỳ thời điểm nào trước 15:00)

```
14:30 → A tự rút sạc (sớm 30 phút)
      → Backend gửi notification đến B
      → B nhận offer: "Sạc ngay hay chờ đến 15:00?"
      
Option 1: B đồng ý
      → B chuyển vào booking ngay
      → C lên vị trí 1
      
Option 2: B từ chối
      → B giữ vị trí 1, chờ đến 15:00
      → 15:00 → Scheduled task tự động chuyển B vào booking
      → C lên vị trí 1
```

---

### CASE 2: Session tự động kết thúc đúng giờ

```
15:00 → Session tự động end (A không rút sạc hoặc rút đúng giờ)
      → Backend TỰ ĐỘNG chuyển B vào booking
      → C lên vị trí 1
```

---

## 🔌 API: End Session (Driver A rút sạc)

### **POST** `/api/charging/session/finish/{sessionId}`

Khi driver A kết thúc session (rút sạc), API này sẽ trả về thông tin chi tiết.

#### **Request:**
```
POST /api/charging/session/finish/ABC12345
```

#### **Response - CASE 1: Rút sạc sớm + Có người chờ**

```json
{
  "success": true,
  "message": "Session kết thúc thành công. Đã gửi offer sạc sớm cho driver tiếp theo.",
  "sessionId": "ABC12345",
  
  // Thông tin về early charging offer
  "hasWaitingDriver": true,       // ✅ Có driver đang chờ
  "sentEarlyOffer": true,          // ✅ Đã gửi offer cho B
  "nextDriverId": "USER789",       // ID của driver B
  "minutesEarly": 30,              // Rút sạc sớm 30 phút
  
  // Thông tin thời gian
  "expectedEndTime": "2025-11-05T15:00:00",
  "actualEndTime": "2025-11-05T14:30:00",
  
  // Thông tin session
  "chargedEnergy": 25.5,           // kWh đã sạc
  "totalAmount": 98379.0           // Tổng tiền
}
```

#### **Response - CASE 2: Rút sạc sớm + Không có người chờ**

```json
{
  "success": true,
  "message": "Session kết thúc thành công. Không có driver nào trong hàng đợi.",
  "sessionId": "ABC12345",
  "hasWaitingDriver": false,       // ❌ Không có driver chờ
  "sentEarlyOffer": false,         // ❌ Không gửi offer
  "nextDriverId": null,
  "minutesEarly": 30,
  "expectedEndTime": "2025-11-05T15:00:00",
  "actualEndTime": "2025-11-05T14:30:00",
  "chargedEnergy": 25.5,
  "totalAmount": 98379.0
}
```

#### **Response - CASE 3: Kết thúc đúng giờ**

```json
{
  "success": true,
  "message": "Session kết thúc thành công. Driver tiếp theo đã được tự động chuyển vào booking.",
  "sessionId": "ABC12345",
  "hasWaitingDriver": false,       // B đã được tự động chuyển rồi
  "sentEarlyOffer": false,         // Không phải rút sớm
  "nextDriverId": null,
  "minutesEarly": null,
  "expectedEndTime": "2025-11-05T15:00:00",
  "actualEndTime": "2025-11-05T15:00:30",
  "chargedEnergy": 45.8,
  "totalAmount": 176618.4
}
```

### **FE của Driver A nên xử lý như sau:**

```javascript
async function endChargingSession(sessionId) {
    try {
        const response = await axios.post(
            `/api/charging/session/finish/${sessionId}`
        );

        if (!response.data.success) {
            showError(response.data.message);
            return;
        }

        // Hiển thị thông báo dựa trên response
        if (response.data.hasWaitingDriver && response.data.sentEarlyOffer) {
            // Có gửi offer cho driver B
            showNotification({
                type: 'success',
                title: '✅ Kết thúc sạc thành công!',
                message: `📊 Năng lượng: ${response.data.chargedEnergy} kWh\n` +
                         `💰 Tổng tiền: ${formatCurrency(response.data.totalAmount)}\n\n` +
                         `🔔 Đã gửi thông báo cho driver tiếp theo (${response.data.nextDriverId})\n` +
                         `⏰ Trạm sạc sẵn sàng sớm ${response.data.minutesEarly} phút`,
                duration: 5000
            });
        } else {
            // Không có driver chờ hoặc kết thúc đúng giờ
            showNotification({
                type: 'success',
                title: '✅ Kết thúc sạc thành công!',
                message: `📊 Năng lượng: ${response.data.chargedEnergy} kWh\n` +
                         `💰 Tổng tiền: ${formatCurrency(response.data.totalAmount)}`,
                duration: 3000
            });
        }

        // Navigate to payment
        navigateToPayment({
            sessionId: response.data.sessionId,
            chargedEnergy: response.data.chargedEnergy,
            totalAmount: response.data.totalAmount
        });

    } catch (error) {
        showError('Lỗi khi kết thúc session. Vui lòng thử lại.');
    }
}
```

---

## 📡 WebSocket Integration (Driver B)

### ⚠️ **QUAN TRỌNG: FE phải setup WebSocket ngay từ đầu**

**Driver B** phải **luôn kết nối WebSocket** khi đang trong waiting list để nhận thông báo realtime.

```javascript
// Khi user join waiting list hoặc mở app
function setupWebSocketForWaitingDriver(userId) {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function() {
        console.log('✅ WebSocket connected for user:', userId);
        
        // ⚡ Subscribe early charging offer
        stompClient.subscribe('/user/queue/early-charging-offer', (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 Received early charging offer:', data);
            
            // 🎨 TỰ ĐỘNG hiện dialog cho user
            showEarlyChargingDialog(data);
        });
        
        // ⚡ Subscribe booking status (auto-booking)
        stompClient.subscribe('/user/queue/booking-status', (message) => {
            const data = JSON.parse(message.body);
            console.log('📢 Received booking status:', data);
            
            if (data.status === 'CONFIRMED') {
                showBookingConfirmedNotification(data);
                navigateToBookingDetail(data.bookingId);
            }
        });
        
        // ⚡ Subscribe position update
        stompClient.subscribe('/user/queue/position-update', (message) => {
            const data = JSON.parse(message.body);
            console.log('📍 Position updated:', data.position);
            updateQueuePosition(data.position);
        });
    });
    
    return stompClient;
}
```

### 📱 Timeline Flow (FE Perspective)

```
DRIVER B (đang ở Waiting List Screen)
    ↓
[WebSocket đã connect sẵn]
    ↓
DRIVER A bấm "Kết thúc sạc" (14:30)
    ↓
Backend gửi WebSocket → /user/queue/early-charging-offer
    ↓
⚡ CALLBACK TỰ ĐỘNG CHẠY trên FE của B
    ↓
🎨 showEarlyChargingDialog() được gọi
    ↓
B thấy popup: "Sạc ngay hay chờ đến giờ?"
```

### Subscribe channel `/user/queue/early-charging-offer`

Khi A rút sạc sớm, B sẽ **TỰ ĐỘNG** nhận WebSocket notification:

```javascript
// ✅ Setup này phải chạy KHI B VÀO WAITING LIST
stompClient.subscribe('/user/queue/early-charging-offer', (message) => {
    const data = JSON.parse(message.body);
    
    // {
    //   "postId": "POST001",
    //   "message": "Trạm sạc đã sẵn sàng sớm. Bạn có muốn sạc ngay không?",
    //   "minutesEarly": 30,
    //   "expectedTime": "2025-11-05T15:00:00",
    //   "availableNow": true
    // }
    
    // 🎨 TỰ ĐỘNG hiện dialog - KHÔNG CẦN POLLING!
    showEarlyChargingDialog(data);
});
```

### Subscribe channel `/user/queue/booking-status`

Khi B được tự động chuyển vào booking (hoặc B accept offer):

```javascript
stompClient.subscribe('/user/queue/booking-status', (message) => {
    const data = JSON.parse(message.body);
    
    // {
    //   "status": "CONFIRMED",
    //   "bookingId": "BOOK789",
    //   "message": "Your booking has been confirmed",
    //   "postId": "POST123"
    // }
    
    if (data.status === 'CONFIRMED') {
        showBookingConfirmedNotification(data);
        navigateToBookingDetail(data.bookingId);
    }
});
```

---

## 🎨 UI Dialog (Driver B)

### Component: Early Charging Offer Dialog

```javascript
function showEarlyChargingDialog(data) {
    const minutesEarly = data.minutesEarly;
    const expectedTime = new Date(data.expectedTime).toLocaleTimeString('vi-VN');
    
    // 🎨 Hiện dialog/modal/bottom sheet
    const modal = {
        title: '🔋 Trạm sạc sẵn sàng sớm!',
        message: `
            Trạm sạc đã sẵn sàng sớm ${minutesEarly} phút.
            Bạn có muốn sạc ngay không?
            
            ⏰ Nếu từ chối, bạn sẽ tự động vào booking lúc: ${expectedTime}
        `,
        buttons: [
            {
                text: '✅ Sạc ngay',
                primary: true,
                onClick: () => acceptEarlyCharging(data.postId)
            },
            {
                text: '❌ Chờ đến giờ',
                secondary: true,
                onClick: () => declineEarlyCharging(data.postId)
            }
        ],
        dismissible: false  // ⚠️ User PHẢI chọn, không cho đóng
    };
    
    displayModal(modal);
    
    // Optional: Play sound để thu hút attention
    playNotificationSound();
    
    // Optional: Vibrate device
    if (navigator.vibrate) {
        navigator.vibrate([200, 100, 200]);
    }
}
```

### React Example:

```jsx
import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function WaitingListScreen({ userId, postId }) {
    const [stompClient, setStompClient] = useState(null);
    const [showOfferDialog, setShowOfferDialog] = useState(false);
    const [offerData, setOfferData] = useState(null);

    useEffect(() => {
        // 🔌 Connect WebSocket khi component mount
        const socket = new SockJS('/ws');
        const client = Client.over(() => socket);

        client.onConnect = () => {
            console.log('✅ WebSocket connected');

            // 📡 Subscribe early charging offer
            client.subscribe('/user/queue/early-charging-offer', (message) => {
                const data = JSON.parse(message.body);
                console.log('🔔 Received offer:', data);
                
                // ⚡ TỰ ĐỘNG hiện dialog
                setOfferData(data);
                setShowOfferDialog(true);
            });

            // 📡 Subscribe booking status
            client.subscribe('/user/queue/booking-status', (message) => {
                const data = JSON.parse(message.body);
                if (data.status === 'CONFIRMED') {
                    // Navigate to booking screen
                    history.push(`/booking/${data.bookingId}`);
                }
            });
        };

        client.activate();
        setStompClient(client);

        // Cleanup khi unmount
        return () => {
            if (client) {
                client.deactivate();
            }
        };
    }, [userId]);

    const handleAccept = async () => {
        await acceptEarlyCharging(userId, postId);
        setShowOfferDialog(false);
    };

    const handleDecline = async () => {
        await declineEarlyCharging(userId, postId);
        setShowOfferDialog(false);
    };

    return (
        <div>
            <h2>Waiting List - Vị trí: #1</h2>
            
            {/* Dialog tự động hiện khi nhận WebSocket */}
            {showOfferDialog && (
                <Dialog open={showOfferDialog}>
                    <DialogTitle>🔋 Trạm sạc sẵn sàng sớm!</DialogTitle>
                    <DialogContent>
                        <p>Trạm sạc đã sẵn sàng sớm {offerData?.minutesEarly} phút.</p>
                        <p>Bạn có muốn sạc ngay không?</p>
                        <p>⏰ Nếu từ chối, bạn sẽ tự động vào booking lúc: 
                           {new Date(offerData?.expectedTime).toLocaleTimeString('vi-VN')}
                        </p>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleDecline} variant="outlined">
                            ❌ Chờ đến giờ
                        </Button>
                        <Button onClick={handleAccept} variant="contained" color="primary">
                            ✅ Sạc ngay
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
        </div>
    );
}
```

### Flutter Example:

```dart
class WaitingListScreen extends StatefulWidget {
  @override
  _WaitingListScreenState createState() => _WaitingListScreenState();
}

class _WaitingListScreenState extends State<WaitingListScreen> {
  StompClient? stompClient;

  @override
  void initState() {
    super.initState();
    _setupWebSocket();
  }

  void _setupWebSocket() {
    stompClient = StompClient(
      config: StompConfig.sockJS(
        url: 'https://your-api.com/ws',
        onConnect: (StompFrame frame) {
          print('✅ WebSocket connected');

          // 📡 Subscribe early charging offer
          stompClient!.subscribe(
            destination: '/user/queue/early-charging-offer',
            callback: (StompFrame frame) {
              final data = jsonDecode(frame.body!);
              print('🔔 Received offer: $data');
              
              // ⚡ TỰ ĐỘNG hiện dialog
              _showEarlyChargingDialog(data);
            },
          );

          // 📡 Subscribe booking status
          stompClient!.subscribe(
            destination: '/user/queue/booking-status',
            callback: (StompFrame frame) {
              final data = jsonDecode(frame.body!);
              if (data['status'] == 'CONFIRMED') {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => BookingScreen(bookingId: data['bookingId']),
                  ),
                );
              }
            },
          );
        },
      ),
    );

    stompClient!.activate();
  }

  void _showEarlyChargingDialog(Map<String, dynamic> data) {
    showDialog(
      context: context,
      barrierDismissible: false,  // ⚠️ User PHẢI chọn
      builder: (context) => AlertDialog(
        title: Text('🔋 Trạm sạc sẵn sàng sớm!'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Trạm sạc đã sẵn sàng sớm ${data['minutesEarly']} phút.'),
            SizedBox(height: 8),
            Text('Bạn có muốn sạc ngay không?'),
            SizedBox(height: 8),
            Text('⏰ Nếu từ chối, bạn sẽ tự động vào booking lúc: ${data['expectedTime']}'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              _declineEarlyCharging(data['postId']);
              Navigator.pop(context);
            },
            child: Text('❌ Chờ đến giờ'),
          ),
          ElevatedButton(
            onPressed: () {
              _acceptEarlyCharging(data['postId']);
              Navigator.pop(context);
            },
            child: Text('✅ Sạc ngay'),
          ),
        ],
      ),
    );
    
    // Optional: Play sound
    AudioPlayer().play(AssetSource('notification.mp3'));
    
    // Optional: Vibrate
    Vibration.vibrate(pattern: [200, 100, 200]);
  }

  @override
  void dispose() {
    stompClient?.deactivate();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Waiting List')),
      body: Center(
        child: Text('Vị trí của bạn: #1'),
      ),
    );
  }
}
```

---

## 🎯 Điểm quan trọng cho FE:

### 1. **WebSocket PHẢI connect ngay từ đầu**
```javascript
// ❌ SAI: Connect khi nhận notification
// ✅ ĐÚNG: Connect ngay khi user vào waiting list

// When user joins waiting list:
function onUserJoinWaitingList(userId) {
    setupWebSocketForWaitingDriver(userId);
    // Sau đó user chỉ việc đợi WebSocket push notification
}
```

### 2. **Không cần polling API**
```javascript
// ❌ SAI: Polling API để check có offer không
setInterval(() => {
    fetch('/api/check-offer').then(...);  // KHÔNG CẦN!
}, 5000);

// ✅ ĐÚNG: WebSocket tự động push
stompClient.subscribe('/user/queue/early-charging-offer', (message) => {
    // Tự động nhận khi có offer
});
```

### 3. **Dialog phải block UI**
```javascript
// ⚠️ User PHẢI chọn: "Sạc ngay" hoặc "Chờ đến giờ"
const modal = {
    dismissible: false,  // Không cho đóng bằng cách bấm ngoài
    closeButton: false,  // Không có nút X
    // User BẮT BUỘC phải chọn 1 trong 2 nút
};
```

### 4. **Handle disconnect/reconnect**
```javascript
stompClient.onWebSocketClose = () => {
    console.log('❌ WebSocket disconnected');
    // Tự động reconnect sau 3 giây
    setTimeout(() => {
        setupWebSocketForWaitingDriver(userId);
    }, 3000);
};
```

### 5. **App ở background vẫn nhận được notification**
```javascript
// Web: Service Worker để nhận notification khi tab không active
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js');
}

// Mobile: Push notification khi app ở background
// Cần integrate với Firebase Cloud Messaging (FCM)
```

---

## 🐛 Troubleshooting: WebSocket không nhận được notification

### ⚠️ **Vấn đề phổ biến: FE không nhận được early charging offer**

**Nguyên nhân:**

1. **Thiếu `user-name` trong STOMP connect header** ⚠️
2. User ID không khớp với Redis queue
3. Subscribe channel sai endpoint

---

### ✅ **GIẢI PHÁP: Phải gửi user-name khi connect WebSocket**

```javascript
// ❌ SAI: Không gửi user-name
const stompClient = Stomp.over(socket);
stompClient.connect({}, callback);

// ✅ ĐÚNG: Phải gửi user-name trong header
const stompClient = Stomp.over(socket);
stompClient.connect(
    {
        'user-name': userId  // ⚠️ QUAN TRỌNG!
    },
    function(frame) {
        console.log('✅ Connected as:', userId);
        // Subscribe channels...
    }
);
```

### 📝 **Code mẫu ĐÚNG cho React:**

```jsx
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function WaitingListScreen({ userId, postId }) {
    const [stompClient, setStompClient] = useState(null);
    const [showOfferDialog, setShowOfferDialog] = useState(false);
    const [offerData, setOfferData] = useState(null);

    useEffect(() => {
        // 🔌 Connect WebSocket
        const socket = new SockJS('http://your-api.com/ws');
        const client = Stomp.over(socket);

        // ⚠️ QUAN TRỌNG: Phải gửi user-name trong connect headers
        client.connect(
            {
                'user-name': userId  // ✅ Backend cần cái này để routing!
            },
            function(frame) {
                console.log('✅ WebSocket connected for user:', userId);

                // 📡 Subscribe early charging offer
                client.subscribe('/user/queue/early-charging-offer', (message) => {
                    console.log('🔔 Raw message:', message);
                    const data = JSON.parse(message.body);
                    console.log('🔔 Parsed offer data:', data);
                    
                    // ⚡ TỰ ĐỘNG hiện dialog
                    setOfferData(data);
                    setShowOfferDialog(true);
                });

                // 📡 Subscribe booking status
                client.subscribe('/user/queue/booking-status', (message) => {
                    const data = JSON.parse(message.body);
                    console.log('📢 Booking status:', data);
                    
                    if (data.status === 'CONFIRMED') {
                        // Navigate to booking
                        window.location.href = `/booking/${data.bookingId}`;
                    }
                });

                console.log('📡 Subscribed to all channels');
            },
            function(error) {
                console.error('❌ WebSocket error:', error);
            }
        );

        setStompClient(client);

        // Cleanup
        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, [userId]);

    const handleAccept = async () => {
        try {
            await acceptEarlyCharging(userId, postId);
            setShowOfferDialog(false);
        } catch (error) {
            console.error('Error accepting:', error);
        }
    };

    const handleDecline = async () => {
        try {
            await declineEarlyCharging(userId, postId);
            setShowOfferDialog(false);
        } catch (error) {
            console.error('Error declining:', error);
        }
    };

    return (
        <div>
            <h2>Waiting List - User: {userId}</h2>
            <p>Post: {postId}</p>
            
            {showOfferDialog && offerData && (
                <div className="modal">
                    <h3>🔋 Trạm sạc sẵn sàng sớm!</h3>
                    <p>Sớm {offerData.minutesEarly} phút</p>
                    <button onClick={handleAccept}>✅ Sạc ngay</button>
                    <button onClick={handleDecline}>❌ Chờ đến giờ</button>
                </div>
            )}
        </div>
    );
}

export default WaitingListScreen;
```

---

## 🧪 **DEBUG MODE: Test WebSocket hoạt động**

### **Backend Test Endpoint** (ĐÃ TẠO SẴN)

Tôi đã tạo sẵn test endpoint trong backend để bạn test WebSocket:

```java
// File: WebSocketTestController.java
POST /api/test/send-offer/{userId}
```

**Cách test:**

#### **Bước 1: Đảm bảo Backend đang chạy**

#### **Bước 2: FE mở Waiting List page với user DRV001**

Đảm bảo WebSocket đã connect:
```javascript
console.log('✅ WebSocket connected for user: DRV001');
console.log('📡 Subscribed to all channels');
```

#### **Bước 3: Gọi test API từ Postman/Thunder Client**

```
POST http://localhost:8080/api/test/send-offer/DRV001
```

**Kết quả mong đợi:**

✅ **Backend log sẽ hiện:**
```
🧪 [TEST] Sending offer to user: DRV001
✅ [TEST] Message sent successfully to: DRV001
```

✅ **FE console sẽ hiện:**
```
🔔 Received early charging offer: {
  postId: "TEST001",
  message: "Test: Trạm sạc đã sẵn sàng sớm...",
  minutesEarly: 30,
  expectedTime: "2025-11-05T13:16:00",
  availableNow: true
}
```

✅ **Popup/Dialog tự động hiện trên FE**

---

### **Nếu test THÀNH CÔNG:**

→ **Vấn đề:** Code endSession() KHÔNG gửi message  
→ **Nguyên nhân:** Logic trong `ChargingSessionServiceImpl.endSession()` có bug

**Kiểm tra:**
1. Session có `expectedEndTime` không?
2. `actualEndTime.isBefore(expectedEndTime)` có đúng không?
3. Redis queue có user DRV001 không?

---

### **Nếu test THẤT BẠI (FE không nhận):**

→ **Vấn đề:** WebSocket setup sai trên FE  
→ **Nguyên nhân:** 
- FE không gửi `user-name` trong connect header
- Subscribe sai channel
- WebSocket disconnect

**Fix:**

```javascript
// ✅ Code ĐÚNG phải như này:
const stompClient = Stomp.over(new SockJS('/ws'));

stompClient.connect(
    {
        'user-name': 'DRV001'  // ⚠️ PHẢI CÓ!
    },
    function(frame) {
        console.log('✅ Connected as: DRV001');
        
        stompClient.subscribe('/user/queue/early-charging-offer', (message) => {
            console.log('🔔 Received:', message.body);
            const data = JSON.parse(message.body);
            // Show dialog...
        });
    }
);
```

---

## 🔍 **Debug Checklist từng bước:**

### **1️⃣ Kiểm tra Backend có gửi message không?**

Khi A kết thúc session, check backend log:

```bash
# Phải có dòng này:
🔔 [CASE 1] A ended early - Sent offer to driver: DRV001 (early by X minutes)
```

❌ **KHÔNG CÓ** → Backend không gửi → Check logic trong `endSession()`

✅ **CÓ** → Backend đã gửi → Vấn đề ở FE

---

### **2️⃣ Kiểm tra FE có connect đúng không?**

Check FE console:

```javascript
// Phải có dòng này:
✅ WebSocket connected for user: DRV001
📡 Subscribed to all channels
```

❌ **KHÔNG CÓ** → FE chưa connect → Fix WebSocket setup

---

### **3️⃣ Kiểm tra Backend có nhận được user-name không?**

Check backend log khi FE connect:

```bash
# Phải có dòng này:
🔐 [WebSocket] Setting principal for user: DRV001
```

❌ **KHÔNG CÓ** → FE chưa gửi `user-name` → **ĐÂY LÀ VẤN ĐỀ CHÍNH!**

✅ **CÓ** → Good, tiếp tục check

---

### **4️⃣ Kiểm tra Redis queue**

```bash
redis-cli
> LRANGE queue:post:POST001 0 -1
1) "DRV001"  # ✅ User phải ở đây
```

❌ **KHÔNG CÓ** → User không trong queue → Backend không gửi offer

---

### **5️⃣ Test với test endpoint**

Gọi: `POST /api/test/send-offer/DRV001`

✅ **FE nhận được** → WebSocket OK, vấn đề ở logic endSession()

❌ **FE KHÔNG nhận** → WebSocket setup SAI trên FE

---

## 🎯 **GIẢI PHÁP CUỐI CÙNG:**

### **Vấn đề 99% là: FE THIẾU `user-name` trong connect header!**

**Code FE PHẢI SỬA:**

```javascript
// ❌ CODE SAI (hiện tại):
stompClient.connect({}, function() {
    // Subscribe...
});

// ✅ CODE ĐÚNG (phải sửa thành):
stompClient.connect(
    { 'user-name': userId },  // ← THÊM DÒNG NÀY!
    function() {
        // Subscribe...
    }
);
```

**Sau khi sửa:**

1. Restart FE
2. Mở Waiting List page
3. Check console log có: `🔐 [WebSocket] Setting principal for user: DRV001`
4. Test lại A kết thúc session → B phải nhận được popup!

---

## 📞 **Nếu vẫn không được sau khi sửa:**

1. **Gửi cho tôi:**
   - Backend log khi A kết thúc session
   - FE console log khi B mở waiting list
   - Response từ test endpoint `/api/test/send-offer/DRV001`

2. **Kiểm tra:**
   - Backend có chạy không?
   - FE có đang kết nối đúng URL WebSocket không? (`ws://localhost:8080/ws`)
   - Browser có block WebSocket không? (Check Network tab)

---
