**Step to run**
- cd ไปที่ folder project Ex. /Users/Developer/moneytransfer-service
- docker compose up -d

**Swagger UI**
- เมื่อ run container เรียบร้อยแล้ว เข้า URL http://localhost:8080/swagger-ui/index.html#/

**OpenAPISpec (file.yaml)**
- https://drive.google.com/file/d/1vLWQcUwu_7CDmY8O3rnx-7OHt5KG7f2b/view?usp=sharing

**Postman Collection**
- https://drive.google.com/file/d/1XzcLurbxLLrOsNDM2w5_Y-1hQ-T4c6Ye/view?usp=drive_link

**สรุปสถานะงาน**
| Tasks                  | Status | Remark        |
|----------------------------|--------|---------------------|
| /api/v1/accounts           | Done   |          |
| /api/v1/accounts/{id}      | Done   |          |
| api/v1/accounts/{id}/balance | Done |          |
| /api/v1/accounts/{id}/transactions | Done |          |
| /api/v1/accounts/{id}/status | Done |          |
| /api/v1/accounts/{id}/deposit | Done |          |
| /api/v1/accounts/{id}/withdraw | Partially |          |
| /api/v1/transfers          | Partially |       |
| /api/v1/transfers/{id}       | Done |       |


