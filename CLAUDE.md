# Quy tắc bắt buộc cho AI

## Nghiệp vụ

- Đọc `BUSINESS_RULES.md` trước khi implement bất kỳ logic nào liên quan đến đặt phòng, giá, checkout, huỷ, hoá đơn
- Mọi vi phạm rule trong `BUSINESS_RULES.md` (hardcode giá, hardcode mã NV, SQL trong View...) phải báo ngay, không tự ý bỏ qua

## Git & GitHub

- TUYỆT ĐỐI không thêm `Co-Authored-By: Claude` hoặc bất kỳ dòng nào nhắc đến AI/Claude vào commit message
- Commit message chỉ dùng tên và phong cách của người dùng, không có chữ ký AI
- Trước khi push, kiểm tra toàn bộ commit message gần nhất không chứa: `Claude`, `Anthropic`, `AI`, `Co-Authored-By`
- Nếu có commit bẩn → `git commit --amend` để sửa trước khi push

## Code

- Không để lại comment kiểu `// Added by AI`, `// Claude:`, `// Generated`
- Không tạo file README hoặc tài liệu trừ khi được yêu cầu rõ ràng

## Trước khi push

Chạy kiểm tra này:
```
git log --oneline -10 | grep -i "claude\|anthropic\|co-authored"
```
Nếu có kết quả → dừng lại, sửa sạch rồi mới push.
