# Firebase import data

Tool này dùng để import nhanh dữ liệu mẫu lên Cloud Firestore.

## Cách chạy

```powershell
cd "D:\HCMUTE-2nd Semester-Period 2\Mobile programming\FonosClone\tools\firebase-import"
npm install
node import-firestore-data.js --serviceAccount "E:\Download\fonosclone-firebase-adminsdk-fbsvc-877011e117.json"
```

Sau khi chạy xong, Firestore sẽ có:

- `books`: 43 documents
- `pod_courses`: 6 documents

App sẽ tự sync dữ liệu từ Firestore về Room khi mở Home.

## Lưu ý

- Không commit file service account JSON lên GitHub.
- Nếu app đang mở, hãy tắt mở lại app hoặc quay lại Home để app sync dữ liệu mới.
- `coverImage` phải trùng tên file ảnh trong `app/src/main/res/drawable-nodpi`.
