# ĐẶC TẢ API CHO TRANG STAFF - FE <-> BE

## 1. Quản lý phiên sạc (Charging Session Management)
### GET /api/charging/session/all
- **Trả về:** Danh sách tất cả phiên sạc đang hoạt động và đã hoàn thành
  - sessionId, userDriver, post, station, startTime, endTime, kwh, totalAmount, status, payment, ...
- **Logic BE:** Trả về toàn bộ phiên sạc cho staff giám sát, lọc theo trạng thái, ngày, user.

### GET /api/charging/session/{sessionId}
- **Trả về:** Chi tiết phiên sạc (dùng cho modal hoặc chi tiết)
  - sessionId, userDriver, post, station, startTime, endTime, kwh, totalAmount, status, payment, ...
- **Logic BE:** Trả về chi tiết phiên sạc theo sessionId.

### PUT /api/charging/session/{sessionId}/status
- **Nhận:** { status: "charging" | "completed" | "inactive" | "paused" }
- **Logic BE:** Staff cập nhật trạng thái phiên sạc (dừng, hoàn thành, tạm dừng).

### DELETE /api/charging/session/{sessionId}
- **Nhận:** sessionId
- **Logic BE:** Xóa phiên sạc (chỉ khi chưa thanh toán hoặc chưa hoàn thành).

---

## 2. Quản lý hàng chờ (Waiting Queue Management)
### GET /api/waiting-list/all
- **Trả về:** Danh sách hàng chờ tại các trụ/trạm
  - waitingListId, stationId, postId, user, phone, waitTime, estimatedTime, queueCount, status
- **Logic BE:** Trả về toàn bộ hàng chờ để staff giám sát, lọc theo trạm/trụ.

### POST /api/waiting-list/activate/{waitingListId}
- **Nhận:** waitingListId
- **Logic BE:** Staff kích hoạt phiên sạc cho user trong hàng chờ.

### DELETE /api/waiting-list/{waitingListId}
- **Nhận:** waitingListId
- **Logic BE:** Staff hủy hàng chờ cho user.

---

## 3. Quản lý trụ sạc (Charging Post Management)
### GET /api/charging/station/posts/{stationId}
- **Trả về:** Danh sách trụ sạc của trạm
  - postId, name, maxPower, status, isAvailable, ...
- **Logic BE:** Trả về danh sách trụ để staff giám sát trạng thái.

---

## 4. Báo cáo sự cố (Report Issue)
### POST /api/report/create
- **Nhận:** { title, description, type, isUrgent, stationId, postId, createdBy }
- **Logic BE:** Staff gửi báo cáo sự cố về trụ/trạm.

### GET /api/report/all
- **Trả về:** Danh sách báo cáo sự cố
  - reportId, title, description, type, isUrgent, status, createdAt, resolvedAt, ...
- **Logic BE:** Trả về toàn bộ báo cáo để staff/the admin xử lý.

---

## 5. Thống kê phiên sạc/trụ/hàng chờ (Statistics)
### GET /api/charging/session/statistics
- **Trả về:** Tổng số phiên sạc, số phiên đang sạc, số phiên hoàn thành, tổng doanh thu, ...
- **Logic BE:** Tính toán số liệu tổng hợp cho dashboard staff.

### GET /api/charging/station/statistics
- **Trả về:** Tổng số trụ online, offline, đang sạc, trống, ...
- **Logic BE:** Tính toán số liệu tổng hợp cho dashboard staff.

---

## 6. Logic controller BE cần có:
- **ChargingSessionController**
  - getAllSessions(), getSessionDetail(sessionId), updateSessionStatus(sessionId, status), deleteSession(sessionId), getStatistics()
- **WaitingListController**
  - getAllWaiting(), activateWaiting(waitingListId), deleteWaiting(waitingListId)
- **ChargingPostController**
  - getPostsByStation(stationId)
- **ReportController**
  - createReport(data), getAllReports()
- **StationStatisticsController**
  - getStationStatistics()

---

## 7. Kết nối API FE <-> BE:
- FE gọi API lấy danh sách phiên sạc, hàng chờ, trụ sạc, báo cáo, thống kê
- FE gọi API cập nhật trạng thái phiên sạc, kích hoạt/hủy hàng chờ, gửi báo cáo sự cố
- FE chỉ cần nhận dữ liệu đã format đúng cho UI, BE xử lý nghiệp vụ, trạng thái, tính toán

---

## 8. Lưu ý cho BE:
- Đảm bảo trả về dữ liệu đầy đủ cho staff thao tác nhanh, realtime nếu có thể
- Xử lý nghiệp vụ xác thực, phân quyền staff
- Trả về dữ liệu đã format đúng cho UI, FE chỉ việc gọi API và xử lý giao diện
