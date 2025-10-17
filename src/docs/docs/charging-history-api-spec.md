# ĐẶC TẢ API LỊCH SỬ PHIÊN SẠC - FE <-> BE

## 1. Lấy lịch sử phiên sạc của user OK
### GET /api/payment/paymentByUser/{userId}
- **Trả về:** Danh sách payment của user, mỗi payment gồm:
  - paymentId
  - chargingSessionId
  - price (tổng tiền)
  - paidAt, paymentMethod, paid, ...
- **Logic BE:** Trả về tất cả payment liên quan đến user (đã thanh toán hoặc chưa).

---

## 2. Lấy chi tiết từng phiên sạc từ payment OK
### GET /api/charging/session/show/{sessionId}
- **Trả về:** Thông tin chi tiết phiên sạc:
  - chargingSessionId
  - station (id trạm)
  - chargingPost (id trụ)
  - startTime, endTime
  - kwh (năng lượng đã sạc)
  - totalAmount (tổng tiền)
  - status, done
  - chargingType, batteryStart, batteryEnd, avgPower, maxPower, ...
- **Logic BE:** Trả về chi tiết phiên sạc theo sessionId.

---

## 3. Lấy thông tin trạm sạc (để hiển thị tên trạm) OK
### GET /api/charging/station/all
- **Trả về:** Danh sách trạm sạc, mỗi trạm gồm:
  - idChargingStation, nameChargingStation, address, ...
- **Logic BE:** Trả về tất cả trạm sạc để FE map tên trạm theo id.

---

## 4. Lấy thông tin trụ sạc (để hiển thị tên trụ) OK
### GET /api/charging/station/posts/{stationId}
- **Trả về:** Danh sách trụ sạc của trạm, mỗi trụ gồm:
  - idChargingPost, nameChargingPost, maxPower, ...
- **Logic BE:** Trả về tất cả trụ của một trạm để FE map tên trụ theo id.

---

## 5. Tổng hợp logic controller BE cần có:
- **PaymentController**
  - getPaymentsByUser(userId): trả về danh sách payment của user
- **ChargingSessionController**
  - getSessionDetail(sessionId): trả về chi tiết phiên sạc
- **ChargingStationController**
  - getAllStations(): trả về danh sách trạm sạc
  - getStationPosts(stationId): trả về danh sách trụ của trạm

---

## 6. Dữ liệu FE cần nhận:
- Từ payment: paymentId, chargingSessionId, price, paidAt, paymentMethod, paid
- Từ session: chargingSessionId, station, chargingPost, startTime, endTime, kwh, totalAmount, status, done, chargingType, batteryStart, batteryEnd, avgPower, maxPower
- Từ station: idChargingStation, nameChargingStation
- Từ post: idChargingPost, nameChargingPost

---

## 7. Lưu ý cho BE: OK
- Trả về dữ liệu đã format đúng cho UI, FE chỉ cần gọi API và xử lý giao diện.
- Xử lý nghiệp vụ, kiểm tra hợp lệ, tính toán tổng tiền, trạng thái ở BE.
