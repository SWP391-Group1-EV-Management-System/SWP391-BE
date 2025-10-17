# ĐẶC TẢ API PHIÊN SẠC - FE <-> BE

## 1. Charging Session (Phiên sạc) OK
### GET /charging/session/current/{userId}
- Trả về thông tin phiên sạc hiện tại của user:
  - chargingSessionId
  - chargingStation: { name, address, ... }
  - chargingPost: { connectorType, power }
  - currentBatteryLevel
  - energyConsumed
  - chargingPower
  - duration, startTime, endTime
  - voltage, current
  - totalCost, pricePerKwh, pricePerMin
  - status
  - booking: { bookingId, ... }

### POST /charging/session/create OK
- Nhận bookingData:
  - bookingId, userId, chargingStationId, chargingPostId, arrivalTime, ...
- Trả về thông tin phiên sạc vừa tạo (giống GET ở trên)

### PUT /charging/session/{sessionId}/status OK
- Nhận:
  - status: "stop" | "completed" | "paused"
- Trả về thông tin phiên sạc đã cập nhật

---

## 2. Booking (Đặt chỗ) OK
### GET /booking/getByUser/{userId}
- Trả về danh sách booking hiện tại của user:
  - bookingId, chargingStationId, chargingPostId, status, arrivalTime, ...

### POST /booking/cancel/{bookingId} OK
- Nhận: bookingId
- Trả về kết quả hủy booking

---

## 3. Waiting Queue (Hàng chờ) OK
### GET /waiting-list/{stationId}?userId=...
- Trả về:
  - queuePosition
  - estimatedWaitTime
  - totalInQueue
  - averageSessionTime
  - currentSessionRemaining

### DELETE /waiting-list/{waitingListId} OK
- Nhận: waitingListId
- Trả về kết quả hủy hàng chờ

---

## 4. Charging Station (Trạm sạc) OK
### GET /charging/station/{stationId}
- Trả về:
  - idChargingStation, nameChargingStation, address, numberOfPosts, chargingPosts, active, ...

---

## 5. Pricing (Giá cả) OK
### GET /charging/station/{stationId}/pricing
- Trả về:
  - pricePerKwh, pricePerMin, ...

---

## 6. Logic Controller BE cần có: 
- SessionController: getCurrentSession, createSession, updateSessionStatus
- BookingController: getBookingByUser, cancelBooking
- WaitingListController: getQueueInfo, cancelWaiting
- ChargingStationController: getStationDetail, getPricing

---

## 7. Lưu ý cho BE: OK
- Trả về dữ liệu đã format đúng cho UI, FE chỉ cần gọi API và xử lý giao diện.
- Xử lý nghiệp vụ, kiểm tra hợp lệ, tính toán thời gian, chi phí, trạng thái ở BE.
