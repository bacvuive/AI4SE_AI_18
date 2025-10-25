# Prompt Log — Unit Testing với AI Prompt (Blood Request)

**Model duy nhất sử dụng:** ChatGPT — GPT-5 Thinking  
**Tech stack:** Java 17 · Maven · JUnit 5 · Mockito · JaCoCo (check LINE & BRANCH ≥ 80%)

---

## 0) Mục tiêu & Điều kiện Hoàn Thành (Definition of Done)
- **≥ 15** test cases bao phủ 5 hàm service
- **Coverage:** LINE & BRANCH ≥ **80%** (JaCoCo enforce tại `mvn verify`)
- **Mock đúng:** HospitalRepository, BloodRequestRepository, AdminRepository, BloodInventoryRepository, EmailService, NotificationService
- **Test style:** Given–When–Then; verify tương tác quan trọng
- **Deliverables:** README, `/prompts/log.md`, test suite xanh, `target/site/jacoco/index.html`

---

## 1) Giai đoạn 1 — PHÂN TÍCH

### Prompt 1.1 — Phân tích nghiệp vụ & điểm test
```
Role: Senior SDET. Stack: Java 17, Maven, JUnit5, Mockito, JaCoCo (check ≥80% line & branch).
Nhiệm vụ: Phân tích lớp service dưới đây và xuất kết quả theo FORMAT bắt buộc.

YÊU CẦU XUẤT RA:
A) TỔNG QUAN HÀM (cho từng hàm)
- Function name
- Main goal (1 câu)
- Inputs: tên, kiểu, ràng buộc
- Return/Side effects
- Edge/Error cases (liệt kê đầy đủ nhánh điều kiện)
- Dependencies cần mock
- Pass/Fail criteria: điều kiện pass, điều kiện fail

B) BẢNG TỔNG HỢP (1 dòng/hàm)
| Function | Inputs | Returns/Effects | Edge cases (tối thiểu 4) | Mocks | Pass/Fail criteria |

QUY TẮC:
- Dựa MỌI nhận định vào code (không suy đoán ngoài phạm vi).
- Ưu tiên các nhánh ảnh hưởng BRANCH coverage.

```

### Output 1 — Phân tích (tóm tắt + roll-up)
— Output: Phân tích nghiệp vụ & điểm test

### A) Tổng quan theo hàm

#### 1) `createBloodRequest(int hospitalId, String bloodType, int quantity)`
- **Mục tiêu:** Tạo yêu cầu máu mới trạng thái **PENDING** nếu đầu vào hợp lệ.
- **Inputs:** `hospitalId:int` (tồn tại), `bloodType:String` (∈ {A±, B±, AB±, O±}), `quantity:int` (>0).
- **Kết quả/Hiệu ứng:** `boolean`; `true` khi `save(PENDING)`; side-effect: `requests.save(..)`.
- **Edge/Error:** hospital **không tồn tại**; `quantity ≤ 0`; `bloodType null/invalid`; **duplicate PENDING**; **boundary `quantity=1`**.
- **Mocks:** `HospitalRepository`, `BloodRequestRepository`.
- **Pass/Fail:** Pass: `true` + `verify(save(PENDING))`; Fail: `false` + `verify(save, never())`.

#### 2) `approveRequest(int requestId, int adminId)`
- **Mục tiêu:** `PENDING → APPROVED` nếu admin hợp lệ.
- **Inputs:** `requestId:int`, `adminId:int` (tồn tại).
- **Kết quả/Hiệu ứng:** `boolean`; `updateStatus(APPROVED, adminId)`.
- **Edge/Error:** admin **invalid**; **not found**; **status ≠ PENDING**.
- **Mocks:** `AdminRepository`, `BloodRequestRepository`.
- **Pass/Fail:** Pass: `true` + `verify(updateStatus(APPROVED))`; Fail: `false` + `verify(updateStatus, never())`.

#### 3) `rejectRequest(int requestId, String reason)`
- **Mục tiêu:** Từ chối `PENDING` với `reason` hợp lệ.
- **Inputs:** `requestId:int`, `reason:String` (không null/blank).
- **Kết quả/Hiệu ứng:** `boolean`; `updateStatus(REJECTED, reason)`.
- **Edge/Error:** `reason null` / `blank`; **not found**; **status ≠ PENDING**.
- **Mocks:** `BloodRequestRepository`.
- **Pass/Fail:** Pass: `true` + `verify(updateStatus(REJECTED))`; Fail: `false` + `verify(updateStatus, never())`.

#### 4) `fulfillRequest(int requestId)`
- **Mục tiêu:** Với `APPROVED`, nếu kho **đủ** → `deduct` + `COMPLETED`; nếu **thiếu** → **throw**.
- **Inputs:** `requestId:int`.
- **Kết quả/Hiệu ứng:** `boolean` hoặc **throw `IllegalStateException`**; side-effects: `inventory.deduct(..)` + `updateStatus(COMPLETED)`.
- **Edge/Error:** **not found**; **status ≠ APPROVED**; **stock < qty → throw**; **boundary `available == qty`**.
- **Mocks:** `BloodRequestRepository`, `BloodInventoryRepository`.
- **Pass/Fail:** Pass: `true` + `verify(deduct)` & `verify(updateStatus(COMPLETED))`; Fail: `false/throw` + `verify(updateStatus, never())`.

#### 5) `notifyHospital(int requestId)`
- **Mục tiêu:** Gửi email & notification khi status ∈ {`APPROVED`,`REJECTED`,`COMPLETED`} và có email.
- **Inputs:** `requestId:int`.
- **Kết quả/Hiệu ứng:** `boolean`; `true` **chỉ khi** `email.send(..)` **và** `notification.push(..)` đều `true`.
- **Edge/Error:** **not found**; **status PENDING/invalid**; **missing email**; **partial failure** *(email OK/notif FAIL & ngược lại)*.
- **Mocks:** `BloodRequestRepository`, `HospitalRepository`, `EmailService`, `NotificationService`.
- **Pass/Fail:** Pass: `true` + `verify(send)` & `verify(push)`; Fail: `false`; thiếu email/invalid status → `verify(email, never()).send(..)`.

### B) Bảng roll-up (1 dòng/hàm)

| Function | Inputs | Returns/Effects | Edge cases (≥4) | Mocks | Pass/Fail criteria |
|---|---|---|---|---|---|
| createBloodRequest | `hospitalId:int`, `bloodType:String`, `quantity:int` | `boolean`; `save(PENDING)` | hospital !exists; qty ≤ 0; bloodType null/invalid; duplicate; boundary qty=1 | HospitalRepo, BloodRequestRepo | Pass: `true` + `save(PENDING)`; Fail: `false` + `never save()` |
| approveRequest | `requestId:int`, `adminId:int` | `boolean`; `updateStatus(APPROVED)` | admin !exists; not found; status≠PENDING | AdminRepo, BloodRequestRepo | Pass: `true` + `updateStatus(APPROVED)`; Fail: `false` + `never updateStatus()` |
| rejectRequest | `requestId:int`, `reason:String` | `boolean`; `updateStatus(REJECTED,reason)` | reason null/blank; not found; status≠PENDING | BloodRequestRepo | Pass: `true` + `updateStatus(REJECTED)`; Fail: `false` + `never updateStatus()` |
| fulfillRequest | `requestId:int` | `boolean`/**throw**; `deduct` + `COMPLETED` | not found; status≠APPROVED; stock<qty (**throw**); boundary avail==qty | BloodRequestRepo, InventoryRepo | Pass: `true` + `deduct` & `COMPLETED`; Fail: `false/throw` + `never COMPLETED` |
| notifyHospital | `requestId:int` | `boolean`; `email.send & notif.push` | not found; invalid status; missing email; **partial failures** | BloodRequestRepo, HospitalRepo, Email, Notif | Pass: cả hai `true`; Fail: `false` (thiếu email/invalid → `never send()`) |


##

---

## 2) Giai đoạn 2 — THIẾT KẾ TEST CASES

### Prompt 2.1 — Sinh TEST MATRIX
```
Role: Senior SDET. Stack: Java 17, Maven, JUnit5, Mockito, JaCoCo (≥80% line & branch).

NHIỆM VỤ:
Tạo TEST MATRIX (bảng) cho 5 hàm của BloodRequestService:
- createBloodRequest(hospitalId, bloodType, quantity)
- approveRequest(requestId, adminId)
- rejectRequest(requestId, reason)
- fulfillRequest(requestId)
- notifyHospital(requestId)

YÊU CẦU ĐẦU RA (BẮT BUỘC):
1) Xuất một bảng duy nhất với các cột:
   | Function | Case Name | Given | When | Then | Notes (mock/verify) |
2) Bao phủ đủ loại case: happy, edge, error, boundary, state-based.
3) Mỗi hàm tối thiểu 3 case (khuyến nghị ≥4) → tổng ≥ 18 case.
4) Ở cột Notes, chỉ rõ khi nào dùng when(...).thenReturn(...) và verify(...), never(), times(n).
5) KHÔNG suy đoán ngoài source; bám chính xác logic service.
6) Ưu tiên case kích hoạt BRANCH: not found, invalid input, invalid status, insufficient stock, missing email, duplicate pending, admin invalid.
```

> Ghi chú chung (áp dụng cho các hàng bên dưới):
>
> * **Subject email** phải đúng: `"Blood Request " + STATUS`
> * **Body email** phải chứa: `"Request #<id> for <bloodType> (<qty> units) is <STATUS>"`
    >
    >   * Nếu `REJECTED`: nối thêm `. Reason: <reason>`
> * **Message push**: `"Request <id> is <STATUS>"`
> * Tên mock đúng với code: `emailService`, `notificationService`

| Function           | ID | Case Name                               | Given                                                                                                                                                        | When    | Then                                                     | Notes (mock/verify)                                                                                                                                                                                                               |
| ------------------ | -- | --------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------- | -------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| createBloodRequest | C1 | create_hospital_OK_O+_qty3_true         | `hospitals.existsById(h)=true`, `requests.existsPending(h,bt)=false`, `bloodType="O+"`, `qty=3`                                                              | create  | `true`; saved **PENDING**                                | `verify(requests).save(argThat(r -> r.getStatus()==PENDING && r.getQuantity()==3))`                                                                                                                                               |
| createBloodRequest | C2 | create_hospital_not_found_false         | `hospitals.existsById(h)=false`                                                                                                                              | create  | `false`                                                  | `verify(requests, never()).save(any())`                                                                                                                                                                                           |
| createBloodRequest | C3 | create_qty_le_0_false                   | `qty=0`                                                                                                                                                      | create  | `false`                                                  | `verify(requests, never()).save(any())`                                                                                                                                                                                           |
| createBloodRequest | C4 | create_bloodType_invalid_false          | `bloodType="X0"`                                                                                                                                             | create  | `false`                                                  | `verify(requests, never()).save(any())`                                                                                                                                                                                           |
| createBloodRequest | C5 | create_duplicate_PENDING_false          | `existsPending(h,bt)=true`                                                                                                                                   | create  | `false`                                                  | `verify(requests, never()).save(any())`                                                                                                                                                                                           |
| createBloodRequest | C6 | create_quantity_boundary_min_true       | `qty=1`, `existsById=true`, `existsPending=false`                                                                                                            | create  | `true`                                                   | `verify(requests).save(argThat(r -> r.getQuantity()==1 && r.getStatus()==PENDING))`                                                                                                                                               |
| approveRequest     | A1 | approve_pending_adminOK_true            | `admins.existsById(admin)=true`, `requests.findById(id)=PENDING`                                                                                             | approve | `true`; status **APPROVED**                              | `verify(requests).updateStatus(id, APPROVED, admin, null)`                                                                                                                                                                        |
| approveRequest     | A2 | approve_not_found_false                 | `requests.findById(id)=empty`                                                                                                                                | approve | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| approveRequest     | A3 | approve_status_not_pending_false        | `requests.findById(id).status=APPROVED`                                                                                                                      | approve | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| approveRequest     | A4 | approve_admin_invalid_false             | `admins.existsById(admin)=false`                                                                                                                             | approve | `false`                                                  | `verifyNoInteractions(requests)`                                                                                                                                                                                                  |
| rejectRequest      | R1 | reject_pending_reasonOK_true            | `requests.findById(id)=PENDING`, `reason="dup"`                                                                                                              | reject  | `true`; status **REJECTED**                              | `verify(requests).updateStatus(id, REJECTED, null, "dup")`                                                                                                                                                                        |
| rejectRequest      | R2 | reject_reason_blank_false               | `reason="  "`                                                                                                                                                | reject  | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| rejectRequest      | R3 | reject_reason_null_false                | `reason=null`                                                                                                                                                | reject  | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| rejectRequest      | R4 | reject_not_found_false                  | `requests.findById(id)=empty`                                                                                                                                | reject  | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| rejectRequest      | R5 | reject_status_not_pending_false         | `requests.findById(id).status=APPROVED`                                                                                                                      | reject  | `false`                                                  | `verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                                                                           |
| fulfillRequest     | F1 | fulfill_approved_stockOK_completed_true | `findById=APPROVED(type,qty,approvedBy=10)`, `inventory.getAvailable(type) >= qty`                                                                           | fulfill | `true`; **deduct** + **COMPLETED**                       | `verify(inventory).deduct(eq(type), eq(qty)); verify(requests).updateStatus(id, COMPLETED, 10, null)`                                                                                                                             |
| fulfillRequest     | F2 | fulfill_insufficient_stock_throw        | `getAvailable(type) < qty`                                                                                                                                   | fulfill | **throws `IllegalStateException("Insufficient stock")`** | `verify(inventory, never()).deduct(any(), anyInt()); verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                       |
| fulfillRequest     | F3 | fulfill_status_not_approved_false       | `findById.status=PENDING`                                                                                                                                    | fulfill | `false`                                                  | `verify(inventory, never()).deduct(any(), anyInt()); verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                       |
| fulfillRequest     | F4 | fulfill_stock_boundary_eq_true          | `getAvailable(type) == qty`                                                                                                                                  | fulfill | `true`                                                   | `verify(inventory).deduct(eq(type), eq(qty)); verify(requests).updateStatus(id, COMPLETED, any(), isNull())`                                                                                                                      |
| fulfillRequest     | F5 | fulfill_not_found_false                 | `requests.findById(id)=empty`                                                                                                                                | fulfill | `false`                                                  | `verify(inventory, never()).deduct(any(), anyInt()); verify(requests, never()).updateStatus(anyInt(), any(), any(), any())`                                                                                                       |
| notifyHospital     | N1 | notify_completed_with_email_true        | `requests.findById(id).status=COMPLETED`, `hospitals.findEmailById(h)="hos@mail.com"`, `emailService.send= true`, `notificationService.pushToHospital= true` | notify  | `true`                                                   | `verify(emailService).send(eq("hos@mail.com"), eq("Blood Request COMPLETED"), argThat(b -> b.contains("#"+id) && b.contains("COMPLETED"))); verify(notificationService).pushToHospital(eq(h), eq("Request "+id+" is COMPLETED"))` |
| notifyHospital     | N2 | notify_invalid_status_pending_false     | `status=PENDING`                                                                                                                                             | notify  | `false`                                                  | `verify(hospitals, never()).findEmailById(anyInt()); verify(emailService, never()).send(any(), any(), any()); verify(notificationService, never()).pushToHospital(anyInt(), any())`                                               |
| notifyHospital     | N3 | notify_missing_email_false              | `status=APPROVED`, `findEmailById(h)=empty`                                                                                                                  | notify  | `false`                                                  | `verify(emailService, never()).send(any(), any(), any()); verify(notificationService, never()).pushToHospital(anyInt(), any())`                                                                                                   |
| notifyHospital     | N4 | notify_partial_emailOk_notifFail_false  | `send=true`, `push=false`                                                                                                                                    | notify  | `false`                                                  | `verify(emailService).send(...); verify(notificationService).pushToHospital(...); assertFalse`                                                                                                                                    |
| notifyHospital     | N5 | notify_partial_emailFail_notifOk_false  | `send=false`, `push=true`                                                                                                                                    | notify  | `false`                                                  | `verify(emailService).send(...); verify(notificationService).pushToHospital(...); assertFalse`                                                                                                                                    |
| notifyHospital     | N6 | notify_both_fail_false                  | `send=false`, `push=false`                                                                                                                                   | notify  | `false`                                                  | `verify(emailService).send(...); verify(notificationService).pushToHospital(...); assertFalse`                                                                                                                                    |
| notifyHospital     | N7 | notify_not_found_false                  | `requests.findById(id)=empty`                                                                                                                                | notify  | `false`                                                  | `verify(hospitals, never()).findEmailById(anyInt()); verify(emailService, never()).send(any(), any(), any()); verify(notificationService, never()).pushToHospital(anyInt(), any())`                                               |

---



### Prompt 2.2 — Coverage-by-Design
```
Role: Senior SDET. Stack: Java 17, Maven, JUnit5, Mockito, JaCoCo (mục tiêu ≥80% LINE & BRANCH).

NHIỆM VỤ:
Đánh giá coverage-by-design dựa trên TEST MATRIX cho lớp BloodRequestService.

ĐẦU VÀO:
1) TEST MATRIX (bảng Markdown) ở trên.
2) Mã nguồn hiện tại của BloodRequestService.

YÊU CẦU ĐẦU RA:
A) Checklist per function (đã/ chưa cover)
- Với mỗi function, xuất bảng checklist các nhánh/điều kiện từ code:
  • Covered: ✅/❌
  • Case Name trong matrix
  • Gợi ý bổ sung nếu ❌

B) Nhánh CHƯA có test — đề xuất case mới
- 1 bảng duy nhất: | Function | Missing Branch/Condition | Proposed Case Name | Given | When | Then | Notes |

C) Ước lượng BRANCH coverage (design-time)
- Bảng: | Function | Total Branches | Covered by Matrix | Estimated % |
- Thêm 1 dòng nhận xét cuối: nêu rõ nút thắt coverage nếu còn.
```

### Output 2.2 — Coverage-by-Design
— Output: Coverage-by-Design

### A) Checklist per function

**createBloodRequest**

| Branch/Condition | Covered | Case Name |
|---|---|---|
| hospital !exists | ✅ | create_hospital_not_found_false |
| qty ≤ 0 | ✅ | create_qty_le_0_false |
| bloodType null/invalid | ✅ | *(gộp invalid)* → **nên tách**: `create_invalid_bloodType_false` |
| duplicate pending | ✅ | create_duplicate_PENDING_false |
| boundary qty=1 | ✅ | create_quantity_boundary_min_true |
| happy (save PENDING) | ✅ | create_hospital_OK_O+_qty3_true |

**approveRequest**

| Branch/Condition | Covered | Case Name |
|---|---|---|
| admin !exists | ✅ | *(nếu thiếu, thêm `approve_admin_invalid_false`)* |
| request not found | ✅ | approve_not_found_false |
| status ≠ PENDING | ✅ | approve_status_not_pending_false |
| happy (APPROVED) | ✅ | approve_pending_admin_OK_true |

**rejectRequest**

| Branch/Condition | Covered | Case Name |
|---|---|---|
| reason blank | ✅ | reject_reason_blank_false |
| reason null | ❌ | *(đề xuất `reject_reason_null_false`)* |
| request not found | ✅ | reject_not_found_false |
| status ≠ PENDING | ✅ | *(có thể tách rõ)* |
| happy (REJECTED) | ✅ | reject_pending_reason_OK_true |

**fulfillRequest**

| Branch/Condition | Covered | Case Name |
|---|---|---|
| request not found | ❌ | *(đề xuất `fulfill_not_found_false`)* |
| status ≠ APPROVED | ✅ | fulfill_status_not_approved_false |
| insufficient stock → throw | ✅ | fulfill_insufficient_stock_throw |
| boundary available == qty | ✅ | fulfill_stock_boundary_eq_true |
| happy (deduct + COMPLETED) | ✅ | fulfill_approved_stock_OK_completed_true |

**notifyHospital**

| Branch/Condition | Covered | Case Name |
|---|---|---|
| request not found | ❌ | *(đề xuất `notify_not_found_false`)* |
| invalid status (PENDING) | ✅ | notify_invalid_status_pending_false |
| missing email | ✅ | notify_missing_email_false |
| partial: email OK / notif FAIL | ✅ | notify_partial_emailOk_notifFail_false |
| partial: email FAIL / notif OK | ✅ | notify_partial_emailFail_notifOk_false |
| happy (send && push) | ✅ | notify_completed_with_email_true |

### B) Missing branches → đề xuất test mới

| Function | Missing Branch/Condition | Proposed Case Name | Given | When | Then | Notes (mock/verify) |
|---|---|---|---|---|---|---|
| rejectRequest | `reason == null` | `reject_reason_null_false` | `reason=null` | `reject` | `false` | `verify(updateStatus, never())` |
| fulfillRequest | `request not found` | `fulfill_not_found_false` | `findById=empty` | `fulfill` | `false` | `verify(inventory, never()).deduct(..); verify(updateStatus, never())` |
| notifyHospital | `request not found` | `notify_not_found_false` | `findById=empty` | `notify` | `false` | `verify(email, never()).send(..); verify(notif, never()).push(..)` |

### C) Ước lượng BRANCH (design-time)

| Function | Total Branches (ước tính) | Covered by Matrix | Estimated % |
|---|---:|---:|---:|
| createBloodRequest | 5 | 5 | ~100% |
| approveRequest | 4 | 4 | ~100% |
| rejectRequest | 5 | 4 *(thiếu reason=null)* | ~80% |
| fulfillRequest | 5 | 4 *(thiếu not found)* | ~80% |
| notifyHospital | 6 | 5 *(thiếu not found)* | ~83% |

> **Nút thắt:** cần thêm `not found` cho `fulfill/notify` và `reason=null` cho `reject` để “đóng nắp” BRANCH.

---


## 3) Giai đoạn 3 — SINH TEST CODE

### Prompt 3.1 — Generate skeleton JUnit/Mockito
```
Sinh skeleton JUnit5 + Mockito theo TEST MATRIX.
Yêu cầu:
- @ExtendWith(MockitoExtension) hoặc MockitoAnnotations.openMocks()
- @InjectMocks cho BloodRequestService; @Mock cho dependencies
- Tách file: CreateTest, ApproveRejectTest, FulfillTest, NotifyTest
- Tên test: action_condition_expectedResult
- Mỗi test có Given–When–Then + verify() cần thiết

=== INTERFACES & SERVICE ===
[PASTE AdminRepository.java, HospitalRepository.java, BloodRequestRepository.java,
 BloodInventoryRepository.java, EmailService.java, NotificationService.java,
 BloodRequestService.java]
```

### Output 3.1–3.2 — Danh sách & trạng thái test
— Output: Kết quả sinh test code

- **4 file test** (skeleton → hoàn thiện theo Matrix):

  1) `BloodRequestServiceCreateTest.java` — **8 @Test**
  2) `BloodRequestServiceApproveTest.java` — **4 @Test**
  3) `BloodRequestServiceFulfillTest.java` — **6 @Test**
  4) `BloodRequestServiceNotifyTest.java` — **7 @Test**
  5) `BloodRequestServiceRejectTest.java` — **5 @Test**
    
- **Thiết lập:** `@ExtendWith(MockitoExtension.class)`, `@InjectMocks` + `@Mock` đầy đủ; matcher `eq/any/argThat`; **never()** ở nhánh fail.

---



---

## 4) Giai đoạn 4 — CHẠY & DEBUG

### Prompt 4.1 — Chẩn đoán test fail (dán stacktrace)
```
Chẩn đoán & đề xuất fix:
- Mô tả lỗi: [tóm tắt]
- Stacktrace:
[PASTE STACKTRACE]
- Test code:
[PASTE TEST]
- Source code:
[PASTE SOURCE]

Yêu cầu: nêu root cause, sửa test/code, cần stub gì thêm, assert/verify mới.
```

### Output 4 — Debug ví dụ
— Output: Debug (ví dụ)

```
Fail case: fulfill_insufficient_stock_throw
Error: expected IllegalStateException, but none was thrown
Root cause: inventory.getAvailable(..) chưa stub → hành vi không đúng.
Fix: when(inventory.getAvailable("B+")).thenReturn(3); assertThrows(..); verify(inventory, never()).deduct(..)
Kết quả: test pass.
```

## 5) Giai đoạn 5 — TỐI ƯU & TĂNG COVERAGE

### Prompt 5.1 — Tăng BRANCH coverage
```
BRANCH coverage <80%. Chỉ ra nhánh thiếu & viết thêm 3–5 test để vượt 80%.
```

### Prompt 5.2 — Tối ưu verify & matchers
```
Rà test & chỉnh verify(updateStatus/ send/ push) bằng eq/contains; thêm verifyNoMoreInteractions khi phù hợp.
```

### Output 5 — Kết quả tăng coverage
- **Số lượng test:** 30 @Test bao phủ 5 nhóm chức năng (Create/Approve/Reject/Fulfill/Notify).
- **Mục tiêu rubric:** đạt ≥ 15 test & ≥ 80% LINE/BRANCH coverage — **đã vượt yêu cầu**.
- **Kiểm tra coverage:**
  ```bash
  mvn -q clean verify
  # Mở báo cáo:
  # target/site/jacoco/index.html

---

## 6) Giai đoạn 6 — DOCUMENTATION & DEMO

### Prompt 6.1 — Soạn README
```
Soạn README (Java 17, Maven):
- Cách chạy (IntelliJ + CLI), vị trí coverage report
- Mục tiêu rubric: ≥15 test, ≥80% line/branch
- Ghi chú ByteBuddy warning & -XX:+EnableDynamicAgentLoading
- Rủi ro/giới hạn & hướng mở rộng
```

### Prompt 6.2 — Slide + speaker notes
```
Soạn 10–12 slide: Title, Timeline 180’, Vì sao Blood Request, Hàm & Rule,
AI workflow, Test Matrix, Mocking, Coverage, Demo script, Kết quả & bài học, Phụ lục.
Kèm speaker notes ngắn.
```

### Output 6 — Doc & Demo
- README hoàn tất (cách chạy IntelliJ/CLI, vị trí report, phạm vi & edge cases, troubleshooting).
- Demo 15’: workflow + `mvn verify` + mở `target/site/jacoco/index.html` + 2 case tiêu biểu.

