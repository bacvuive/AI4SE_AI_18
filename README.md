# Blood Request — Unit Testing Challenge
Java 17 · Maven · JUnit 5 · Mockito · JaCoCo

## 1) Mục tiêu
- Test đơn vị cho 5 hàm của **BloodRequestService**:
    - `createBloodRequest(hospitalId, bloodType, quantity)`
    - `approveRequest(requestId, adminId)`
    - `rejectRequest(requestId, reason)`
    - `fulfillRequest(requestId)`
    - `notifyHospital(requestId)`
- Yêu cầu: **≥ 15 test** và **≥ 80% LINE & BRANCH coverage** (JaCoCo check ở phase `verify`).
- Tất cả dependency (repo/service) đều **mock** bằng Mockito.

---

## 2) Yêu cầu môi trường
- **JDK 17**, Maven **3.9+**
- IntelliJ IDEA (Project SDK/Language level = 17)
- Bật plugin **Markdown** (để xem README preview)

---

## 3) Cách chạy & xem coverage
### IntelliJ
1. Mở **View → Tool Windows → Maven**
2. Lifecycle: double-click **`clean`** → **`verify`**
3. Mở báo cáo: **`target/site/jacoco/index.html`** → Right-click → **Open in Browser**

### CLI
```bash
mvn -q clean verify
# macOS: open target/site/jacoco/index.html
# Linux: xdg-open target/site/jacoco/index.html
# Windows: mở file trong Explorer hoặc Project window

---

## IntelliJ VM options (ẩn cảnh báo ByteBuddy)

- **JUnit Run/Debug Configuration → VM options**:
```
-XX:+EnableDynamicAgentLoading
```
- Hoặc trong Maven Surefire `<argLine>` (giữ `${argLine}` của JaCoCo):
```xml
<argLine>${argLine} -XX:+EnableDynamicAgentLoading</argLine>
```
