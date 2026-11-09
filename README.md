# FingerAI – Palm Reader (Kotlin + XML)

App xem chỉ tay, tử vi, tarot theo luồng và phong cách giao diện của "Taqde – AI Palm Reading" 1.0.5. **Quét chỉ tay chạy hoàn toàn trên máy**, không cần server. Không có chat AI.

## Luồng màn hình
Onboarding (3 trang giới thiệu → tên & giới tính → ngày sinh) → Trang chủ (lời chào theo buổi, thẻ AI Palm Reader, báo cáo hằng ngày, lưới "Khám phá vận mệnh") →
- **Phân tích bàn tay:** Yêu cầu ảnh chụp → Camera có hướng dẫn trực tiếp ("Đặt lòng bàn tay vào khung" / "Đưa tay lại gần" / "Đã nhận diện bàn tay"), đèn flash, chọn ảnh từ thư viện → Đang phân tích → Kết quả (ảnh tô 3 đường + luận giải).
- Tử vi hằng ngày, Tarot 78 lá, Test tình duyên, Chiêm tinh Vệ Đà, Hồ sơ.

## Quét chỉ tay hoạt động thế nào
Port từ [yeonsumia/palmistry](https://github.com/yeonsumia/palmistry) (Apache-2.0, giấy phép trong `app/src/main/assets/LICENSE-palmistry.txt`):

1. **MediaPipe Hand Landmarker** (`hand_landmarker.task`) lấy 21 điểm mốc.
2. **Nắn ảnh** về bàn tay chuẩn 256×256 bằng homography (`PalmLines.homography`).
3. **Tách nền** theo màu da HSV → **U-Net** (`palm_unet.onnx`, fp16, chạy bằng ONNX Runtime) → mặt nạ đường chỉ.
4. **Làm mảnh** (Zhang-Suen, giống skimage) → **gom đường** → **phân loại** tâm đạo/trí đạo/sinh đạo bằng tâm cụm K-means có sẵn.
5. **Đo độ dài** so với điểm mốc → luận giải dài/ngắn.

`PalmLinesTest` đối chiếu bản Kotlin với pipeline Python tham chiếu trên 4 ảnh mẫu của repo: từng điểm của cả 3 đường đều khớp.

Khác với repo gốc: dùng API MediaPipe Tasks thay cho `mp.solutions`; homography bình phương tối thiểu thay vì RANSAC; skeletonize 2D (repo vô tình gọi bản 3D trên ảnh 3 kênh); sửa lỗi `classify_lines` có thể chọn trùng một đường cho hai loại.

## Build
Cần JDK 17–21 (không dùng Java 25 của Android Studio cho Gradle 8.10).

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21" ./gradlew testDebugUnitTest assembleDebug
```

APK debug ~155 MB vì gồm thư viện native cho arm64 + x86_64 (emulator). Bản phát hành chỉ arm64 hoặc dạng App Bundle sẽ khoảng 80 MB.

## Giới hạn
- Chỉ nhận 3 đường chính, luận giải dài/ngắn; chưa có đường số mệnh, đường mặt trời.
- Độ chính xác ở mức đồ án nghiên cứu, nhạy với ánh sáng và màu da (tách nền dùng ngưỡng HSV cố định).
- Model được huấn luyện trên bộ dữ liệu PLSU; điều khoản của bộ dữ liệu này chưa được xác minh.
- Không dùng hình ảnh, biểu tượng hay font của app gốc – giao diện dùng emoji và drawable vẽ bằng XML.
