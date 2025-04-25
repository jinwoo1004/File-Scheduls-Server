# 🚀파일 업로드/다운로드 API
> 대학원 석사 논문 주제

## ✅ 단일 파일 업로드
> UPLOADFILE

### Request
**POST** `http://localhost/nlipUploadFile`  

### Headers
- `Content-Type`: `multipart/form-data`

### Form Data
- `type`: `플랫폼`으로 업로드 시 shpManager.java 호출 → GeoServer 쉐입/레이어 발행 작업 병행

| Key      | Value                                                   |
|----------|---------------------------------------------------------|
| `file`   | 업로드할 파일                                            |
| `filepath` | 파일 저장 경로 (예: `D:/TEST/temp/bbk`)                |
| `type`   | `ALL`(일반) 또는 `NLIP`(플랫폼 호출 시)         |

### CURL Example
```bash
curl --location --request POST 'http://localhost/nlipUploadFile' \
--form 'file=@"C:/Program Files (x86)/Google/Chrome/Application/chrome.exe"' \
--form 'filepath="D:/TEST/temp/bbk"' \
--form 'type="ALL"'
```

## ✅ 파일 다운로드
> DOWNLOADFILE

### Request
**GET** http://localhost/downloadFile/{filename}

### Example
**GET** http://localhost/downloadFile/2110130302.zip

### CURL Example
``` bash
curl --location --request GET 'http://localhost/downloadFile/2110130302.zip'
```

## ✅ 다중 파일 업로드
> MULTIUPLOAD FILE

### Request
**POST** http://localhost/nlipUploadMultipleFiles

### Headers
- `Content-Type`: `multipart/form-data`

### Form Data
- `type`: `플랫폼`으로 업로드 시 shpManager.java 호출 → GeoServer 쉐입/레이어 발행 작업 병행

| Key      | Value                                                   |
|----------|---------------------------------------------------------|
| `files`   | 업로드할 파일들 (여러 개 가능)                                      |
| `filepath` | 파일 저장 경로 (예: D:/TEST/temp/bbk)               |
| `type`   | `ALL`(일반) 또는 `NLIP`(플랫폼 호출 시)         |

### CURL Example
``` bash
curl --location --request POST 'http://localhost/nlipUploadMultipleFiles' \
--form 'files=@"C:/Users/USER/Desktop/[강원도-양양] 여름힐링휴가.docx"' \
--form 'files=@"C:/Program Files (x86)/Google/Chrome/Application/chrome_proxy.exe"' \
--form 'files=@"C:/Users/USER/Desktop/CROP_SURVEY_DB_V21.sqlite"' \
--form 'files=@"C:/Program Files/EditPlus/editplus.exe"' \
--form 'filepath="D:/TEST/temp/bbk"' \
--form 'type="ALL"'
```
