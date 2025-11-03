# 🔌 Hướng dẫn kết nối WebSocket cho React

## 📦 Bước 1: Cài đặt thư viện

```bash
npm install sockjs-client @stomp/stompjs
# hoặc
yarn add sockjs-client @stomp/stompjs
```

---

## 🎯 Bước 2: Tạo WebSocket Service

Tạo file: `src/services/WebSocketService.js`

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = new Map();
    }

    connect(userId, onConnectCallback, onErrorCallback) {
        if (this.client && this.connected) {
            console.log('Already connected');
            return;
        }

        const socket = new SockJS('http://localhost:8080/ws');
        
        this.client = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
                console.log('STOMP:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            
            onConnect: (frame) => {
                console.log('✅ WebSocket Connected:', frame);
                this.connected = true;
                if (onConnectCallback) onConnectCallback(frame);
            },
            
            onStompError: (frame) => {
                console.error('❌ STOMP Error:', frame.headers['message']);
                console.error('Details:', frame.body);
                this.connected = false;
                if (onErrorCallback) onErrorCallback(frame);
            },
            
            onDisconnect: () => {
                console.log('🔌 WebSocket Disconnected');
                this.connected = false;
            }
        });

        this.client.activate();
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.clear();
            this.client.deactivate();
            this.connected = false;
            console.log('👋 Disconnected from WebSocket');
        }
    }

    /**
     * Subscribe để nhận thông báo cá nhân cho user
     * @param {string} userId - ID của user
     * @param {string} postId - ID của charging post
     * @param {function} callback - Hàm xử lý khi nhận message
     */
    subscribeToNotifications(userId, postId, callback) {
        if (!this.client || !this.connected) {
            console.error('WebSocket chưa kết nối!');
            return null;
        }

        const destination = `/user/queue/notifications/${postId}`;
        
        const subscription = this.client.subscribe(destination, (message) => {
            console.log('📩 Notification received:', message.body);
            if (callback) callback(message.body);
        });

        this.subscriptions.set(`notifications-${postId}`, subscription);
        console.log('✅ Subscribed to:', destination);
        
        return subscription;
    }

    /**
     * Subscribe topic chung (broadcast cho tất cả client)
     * @param {string} postId - ID của charging post
     * @param {function} callback - Hàm xử lý khi nhận message
     */
    subscribeToTopic(postId, callback) {
        if (!this.client || !this.connected) {
            console.error('WebSocket chưa kết nối!');
            return null;
        }

        const destination = `/topic/waiting/${postId}`;
        
        const subscription = this.client.subscribe(destination, (message) => {
            console.log('📢 Topic message:', message.body);
            if (callback) callback(message.body);
        });

        this.subscriptions.set(`topic-${postId}`, subscription);
        console.log('✅ Subscribed to:', destination);
        
        return subscription;
    }

    unsubscribe(key) {
        const subscription = this.subscriptions.get(key);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(key);
            console.log('❌ Unsubscribed:', key);
        }
    }

    unsubscribeAll() {
        this.subscriptions.forEach((subscription, key) => {
            subscription.unsubscribe();
            console.log('❌ Unsubscribed:', key);
        });
        this.subscriptions.clear();
    }

    isConnected() {
        return this.connected;
    }
}

// Export singleton instance
const wsService = new WebSocketService();
export default wsService;
```

---

## 🎨 Bước 3: Tạo React Hook (Tuỳ chọn, dễ dùng hơn)

Tạo file: `src/hooks/useWebSocket.js`

```javascript
import { useEffect, useState, useCallback } from 'react';
import wsService from '../services/WebSocketService';

export const useWebSocket = (userId, postId) => {
    const [connected, setConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const [position, setPosition] = useState(null);

    useEffect(() => {
        if (!userId || !postId) return;

        // Connect to WebSocket
        wsService.connect(
            userId,
            () => setConnected(true),
            () => setConnected(false)
        );

        // Wait for connection then subscribe
        const timer = setTimeout(() => {
            if (wsService.isConnected()) {
                // Subscribe to notifications
                wsService.subscribeToNotifications(userId, postId, (message) => {
                    setMessages(prev => [...prev, { type: 'notification', text: message, time: new Date() }]);
                    
                    // Parse position from message
                    const posMatch = message.match(/vị trí số (\d+)/);
                    if (posMatch) {
                        setPosition(parseInt(posMatch[1]));
                    }
                });

                // Subscribe to topic (optional)
                wsService.subscribeToTopic(postId, (message) => {
                    setMessages(prev => [...prev, { type: 'broadcast', text: message, time: new Date() }]);
                });
            }
        }, 1000);

        // Cleanup on unmount
        return () => {
            clearTimeout(timer);
            wsService.unsubscribeAll();
            wsService.disconnect();
            setConnected(false);
        };
    }, [userId, postId]);

    const clearMessages = useCallback(() => {
        setMessages([]);
    }, []);

    return { connected, messages, position, clearMessages };
};
```

---

## 📱 Bước 4: Sử dụng trong React Component

### Cách 1: Dùng Hook (Đơn giản nhất - Khuyên dùng)

```jsx
import React, { useState } from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import axios from 'axios';

function WaitingListComponent() {
    const [userId] = useState('USER123'); // Lấy từ auth context
    const [postId, setPostId] = useState('POST456');
    
    const { connected, messages, position, clearMessages } = useWebSocket(userId, postId);

    const handleJoinWaitingList = async () => {
        try {
            const response = await axios.post(
                `http://localhost:8080/api/waiting-list/add/${postId}`,
                {},
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                }
            );
            console.log('✅ Joined waiting list:', response.data);
        } catch (error) {
            console.error('❌ Error:', error);
        }
    };

    const handleCancelWaiting = async (waitingListId) => {
        try {
            const response = await axios.post(
                `http://localhost:8080/api/waiting-list/cancel/${waitingListId}`,
                {},
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                }
            );
            console.log('✅ Cancelled:', response.data);
        } catch (error) {
            console.error('❌ Error:', error);
        }
    };

    return (
        <div className="waiting-list-container">
            <h2>🔌 WebSocket Waiting List</h2>
            
            {/* Connection Status */}
            <div className={`status ${connected ? 'connected' : 'disconnected'}`}>
                {connected ? '✅ Connected' : '❌ Disconnected'}
            </div>

            {/* User Info */}
            <div className="info">
                <p>User ID: <strong>{userId}</strong></p>
                <input 
                    type="text" 
                    value={postId} 
                    onChange={(e) => setPostId(e.target.value)}
                    placeholder="Enter Charging Post ID"
                />
            </div>

            {/* Position Display */}
            {position && (
                <div className="position-card">
                    <h3>Vị trí của bạn trong hàng đợi</h3>
                    <div className="position-number">{position}</div>
                </div>
            )}

            {/* Actions */}
            <div className="actions">
                <button onClick={handleJoinWaitingList} disabled={!connected}>
                    Tham gia hàng đợi
                </button>
                <button onClick={() => handleCancelWaiting('WAITING123')} className="danger">
                    Huỷ hàng đợi
                </button>
                <button onClick={clearMessages}>
                    Xoá tin nhắn
                </button>
            </div>

            {/* Messages Display */}
            <div className="messages">
                <h3>📩 Thông báo ({messages.length})</h3>
                {messages.map((msg, idx) => (
                    <div key={idx} className={`message ${msg.type}`}>
                        <span className="time">
                            {msg.time.toLocaleTimeString()}
                        </span>
                        <span className="text">{msg.text}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default WaitingListComponent;
```

### CSS cho component trên:

```css
.waiting-list-container {
    max-width: 600px;
    margin: 20px auto;
    padding: 20px;
    background: white;
    border-radius: 10px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.status {
    padding: 10px;
    margin: 10px 0;
    border-radius: 5px;
    font-weight: bold;
    text-align: center;
}

.status.connected {
    background: #d4edda;
    color: #155724;
}

.status.disconnected {
    background: #f8d7da;
    color: #721c24;
}

.info {
    margin: 20px 0;
}

.info input {
    width: 100%;
    padding: 10px;
    margin-top: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
}

.position-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px;
    border-radius: 10px;
    text-align: center;
    margin: 20px 0;
}

.position-number {
    font-size: 48px;
    font-weight: bold;
    margin: 10px 0;
}

.actions {
    display: flex;
    gap: 10px;
    margin: 20px 0;
}

.actions button {
    flex: 1;
    padding: 12px;
    border: none;
    border-radius: 5px;
    background: #007bff;
    color: white;
    font-weight: bold;
    cursor: pointer;
    transition: background 0.3s;
}

.actions button:hover {
    background: #0056b3;
}

.actions button:disabled {
    background: #ccc;
    cursor: not-allowed;
}

.actions button.danger {
    background: #dc3545;
}

.actions button.danger:hover {
    background: #c82333;
}

.messages {
    margin-top: 20px;
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #ddd;
    border-radius: 5px;
    padding: 10px;
    background: #f9f9f9;
}

.message {
    padding: 10px;
    margin: 5px 0;
    border-radius: 5px;
    display: flex;
    gap: 10px;
    align-items: start;
}

.message.notification {
    background: #e3f2fd;
    border-left: 3px solid #2196F3;
}

.message.broadcast {
    background: #fff3e0;
    border-left: 3px solid #ff9800;
}

.message .time {
    font-size: 0.85em;
    color: #666;
    min-width: 80px;
}

.message .text {
    flex: 1;
    font-weight: 500;
}
```

---

### Cách 2: Dùng trực tiếp Service (Linh hoạt hơn)

```jsx
import React, { useEffect, useState } from 'react';
import wsService from './services/WebSocketService';

function WaitingListDirect() {
    const [userId] = useState('USER123');
    const [postId] = useState('POST456');
    const [connected, setConnected] = useState(false);
    const [messages, setMessages] = useState([]);

    useEffect(() => {
        // Connect
        wsService.connect(
            userId,
            () => {
                setConnected(true);
                
                // Subscribe after connected
                wsService.subscribeToNotifications(userId, postId, (msg) => {
                    setMessages(prev => [...prev, msg]);
                });
            },
            () => setConnected(false)
        );

        // Cleanup
        return () => {
            wsService.disconnect();
        };
    }, [userId, postId]);

    return (
        <div>
            <h2>WebSocket Status: {connected ? '✅' : '❌'}</h2>
            <ul>
                {messages.map((msg, idx) => (
                    <li key={idx}>{msg}</li>
                ))}
            </ul>
        </div>
    );
}

export default WaitingListDirect;
```

---

## 🧪 Bước 5: Test

### 1. Start Backend:
```bash
cd D:\STUDY\SWP\EV\SWP391-BE
mvn spring-boot:run
```

### 2. Start React:
```bash
cd your-react-app
npm start
```

### 3. Mở Browser Console (F12) và kiểm tra:
- ✅ "WebSocket Connected"
- ✅ "Subscribed to: /user/queue/notifications/{postId}"

### 4. Test join waiting list:
- Click button "Tham gia hàng đợi"
- Kiểm tra console: "User ... joined waiting list"
- Kiểm tra có nhận được vị trí không

---

## 🔍 Troubleshooting

### Không kết nối được?
1. ✅ Kiểm tra backend đã chạy: `http://localhost:8080`
2. ✅ Kiểm tra Redis đã chạy: `redis-cli ping` → PONG
3. ✅ Kiểm tra console có lỗi CORS không
4. ✅ Kiểm tra SecurityConfig đã cho phép `/ws/**`

### Không nhận được message?
1. ✅ Kiểm tra userId có đúng không (phải có trong DB)
2. ✅ Kiểm tra postId có đúng không
3. ✅ Kiểm tra subscription path: `/user/queue/notifications/{postId}`
4. ✅ Mở Backend console xem có log gì không

### Message bị duplicate?
- Đảm bảo cleanup `useEffect` đúng cách
- Unsubscribe trước khi component unmount

---

## 📊 Flow hoàn chỉnh:

```
1. FE: Connect WebSocket → ws://localhost:8080/ws
2. FE: Subscribe → /user/queue/notifications/{postId}
3. FE: Call API → POST /api/waiting-list/add/{postId}
4. BE: Save to DB + Push to Redis
5. BE: Send WebSocket → convertAndSendToUser(userId, "/queue/notifications/...", message)
6. FE: Receive message via subscription
7. FE: Update UI (hiển thị vị trí, thông báo)
```

---

## 🎯 Các API endpoints:

### Join waiting list:
```javascript
POST http://localhost:8080/api/waiting-list/add/{postId}
Headers: { Authorization: 'Bearer YOUR_JWT_TOKEN' }
```

### Cancel waiting list:
```javascript
POST http://localhost:8080/api/waiting-list/cancel/{waitingListId}
Headers: { Authorization: 'Bearer YOUR_JWT_TOKEN' }
```

### Get waiting list:
```javascript
GET http://localhost:8080/api/waiting-list/queue/post/{postId}
GET http://localhost:8080/api/waiting-list/queue/users/{userId}
```

---

✅ **Hoàn thành!** Bây giờ bạn đã có đầy đủ code để kết nối WebSocket trong React.

