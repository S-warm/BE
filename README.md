# SWARM BE 실행 가이드

## 현재 실행 기준

- DB는 Docker Desktop의 PostgreSQL 컨테이너를 사용합니다.
- BE 서버는 로컬에서 Spring Boot로 실행합니다.
- 현재 기본 포트는 `8081`입니다.

## 권장 실행 순서

### 1. Docker Desktop 실행

- Docker Desktop을 먼저 켭니다.

### 2. DB 컨테이너 실행

BE 폴더에서 아래 명령을 실행합니다.

```powershell
cd C:\Users\skyko\Desktop\SWARM\BE
docker compose up -d postgres
```

정상 실행 확인:

```powershell
docker ps
```

`uxswarm-postgres`가 떠 있으면 됩니다.

### 3. BE 서버 실행

#### 가장 쉬운 방법: IntelliJ 실행 버튼

- IntelliJ에서 `SwarmServerApplication (8081)` 실행
- 이 설정은 `.run` 폴더에 공유되어 있습니다.

#### 터미널 실행 방법

```powershell
cd C:\Users\skyko\Desktop\SWARM\BE
.\start-dev-8081.ps1
```

이 스크립트는:

- 기존 `8080`, `8081` 포트 점유 프로세스를 정리하고
- `SERVER_PORT=8081`로 서버를 실행합니다.

## FE 연결 기준

FE는 아래 주소를 바라봐야 합니다.

```txt
http://localhost:8081/api
```

## 왜 8081을 쓰는가

- 현재 개발 환경에서는 Windows/Docker 네트워크 예약 문제로 `8080`이 불안정할 수 있습니다.
- 그래서 기본 실행 포트를 `8081`로 통일했습니다.

## 자주 발생하는 문제

### 1. `Port 8081 was already in use`

원인:

- 이미 실행 중인 BE 서버가 있는데 한 번 더 실행한 경우

해결:

- IntelliJ Run 창에서 기존 서버를 먼저 중지
- 또는 아래 명령으로 정리 후 다시 실행

```powershell
cd C:\Users\skyko\Desktop\SWARM\BE
.\start-dev-8081.ps1
```

### 2. `no configuration file provided: not found`

원인:

- `docker compose up`를 `BE` 폴더가 아닌 곳에서 실행한 경우

해결:

```powershell
cd C:\Users\skyko\Desktop\SWARM\BE
docker compose up -d postgres
```

### 3. Swagger 접속 주소

서버 실행 후 아래 주소로 확인합니다.

```txt
http://localhost:8081/swagger-ui.html
```

## 현재 반영된 실행 관련 변경

- `application.yaml`
  - 기본 서버 포트 `8081`
- `docker-compose.yml`
  - app 포트 `8081:8081`
- `.run/SwarmServerApplication (8081).run.xml`
  - IntelliJ 공유 실행 설정 추가
- `start-dev-8081.ps1`
  - 포트 정리 후 실행 스크립트 추가

## 한 줄 요약

1. Docker Desktop 켠다
2. `docker compose up -d postgres`
3. IntelliJ에서 `SwarmServerApplication (8081)` 실행
