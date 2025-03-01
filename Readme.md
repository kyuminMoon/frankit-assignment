### `[문규민] - 프랜킷 과제`

# 상품 및 옵션 관리 API 아키텍처 설계 문서

## 1. 개요

프랜킷 과제 관련 API 아키텍처 정리. 
상품과 관련된 CRUD 기능과 함께 상품 옵션 관리 기능을 제공.

- 상품과 관련된 CRUD 기능을 구현하고, 상품 옵션 관리 기능을 포함한 RESTful API를 설계 및 개발.
- 완벽한 명세는 아니니, 고민의 여지가 있는 경우 자의적으로 해석하여 구현/구성하시면 됩니다.
- 요구사항 이외 추가 기능들을 자유롭게 구현/구성해도 무방합니다. 다만 제출 시 어필해 주셔야 평가 시 참고됩니다.

## 2. 기술 스택

- **Language**: Java 23
- **Framework**: Spring Boot 3.4.3
- **Build**: Gradle
- **Database**: MySQL 8.0, Redis 7.0
- **Container**: Docker(Podman), Docker(Podman) Compose
- **View**: Thymeleaf

## 3. 아키텍처 개요

레이어드 아키텍처(Layered Architecture)를 적용하여 프로젝트를 진행했습니다.

### 3.1 계층 구조

```
클라이언트 <-> 프레젠테이션 계층 <-> 애플리케이션 계층 <-> 도메인 계층 <-> 인프라스트럭처 계층
```

- **프레젠테이션 계층 (Presentation Layer)**:
    - 외부 요청을 받아 처리하고 응답을 반환합니다.
    - 사용자 인터페이스와 시스템 간의 상호작용을 담당합니다.
    - HTTP 요청 검증, 응답 형식 변환, 예외 처리를 담당합니다.

- **애플리케이션 계층 (Application Layer)**:
    - 비즈니스 프로세스와 워크플로우를 조정합니다.
    - 트랜잭션 관리 및 여러 도메인 서비스 간의 조율을 담당합니다.
    - 도메인 객체를 조작하여 사용자의 의도를 실현합니다.

- **도메인 계층 (Domain Layer)**:
    - 핵심 비즈니스 로직과 규칙을 포함합니다.
    - 비즈니스 엔티티와 값 객체를 정의합니다.
    - 도메인 객체 간의 관계와 상호작용을 정의합니다.
    - 특정 기술이나 인프라스트럭처에 의존하지 않습니다.

- **인프라스트럭처 계층 (Infrastructure Layer)**:
    - 외부 시스템과의 통합 및 기술적 세부 사항을 구현합니다.
    - 데이터베이스, 메시징, 외부 API와의 통신을 담당합니다.
    - 도메인 계층에 정의된 인터페이스의 구체적인 구현을 제공합니다.
    - 공통 유틸리티와 보안, 로깅 등의 횡단 관심사를 처리합니다.

### 3.2 패키지 구조

```
com.tistory.kmmoon.frankit
├── FrankitAssignmentApplication.java
├── presentation/                      
│   ├── controller/                    
│   │   ├── api/                       
│   │   └── view/                      
│   ├── advice/                        
│   └── dto/                           
│       ├── request/                   
│       └── response/                  
├── application/                       
│   └── service/                       
├── domain/                            
│   ├── entity/                        
│   ├── repository/                    
│   ├── exception/                     
│   └── service/                       
├── infrastructure/                    
│   ├── config/                        
│   │   ├── security/                  
│   │   └── redis/                     
│   ├── repository/                    
│   ├── aspect/                        
│   └── util/                          
└── common/                            
    ├── annotation/                    
    └── constant/                      
```

### 3.3 주요 컴포넌트와 책임

#### Presentation 계층
- **API 컨트롤러**: REST API 엔드포인트를 제공하며, 요청을 검증하고 적절한 서비스 메서드를 호출합니다.
    - `AuthController`: 인증 관련 요청 처리
    - `ProductController`: 상품 관리 관련 요청 처리
- **View 컨트롤러**: 웹 페이지 렌더링을 위한 컨트롤러입니다.
    - `ProductViewController`: 상품 관련 뷰 페이지 제공
- **DTO**: 계층 간 데이터 전송을 위한 객체로, 요청 및 응답 형식을 정의합니다.
    - `request`: 클라이언트 요청 데이터 구조
    - `response`: API 응답 데이터 구조
- **Exception Handling**: API 오류 응답 형식을 표준화합니다.
    - `GlobalExceptionHandler`: 전역 예외 처리기

#### Application 계층
- **Service**: 비즈니스 프로세스를 구현하고 트랜잭션을 관리합니다.
    - `AuthService`: 인증 및 토큰 관리 비즈니스 로직
    - `ProductService`: 상품 관리 비즈니스 로직

#### Domain 계층
- **Entity**: 핵심 비즈니스 객체와 규칙을 구현합니다.
    - `User`: 사용자 도메인 모델
    - `Product`: 상품 도메인 모델
    - `ProductOption`: 상품 옵션 도메인 모델
    - `OptionValue`: 옵션 값 도메인 모델
- **Repository**: 도메인 객체의 영속성 작업을 정의합니다.
    - `UserRepository`: 사용자 영속성 관리 인터페이스
    - `ProductRepository`: 상품 영속성 관리 인터페이스
- **Domain Exception**: 비즈니스 규칙 위반에 대한 예외를 정의합니다.
    - `ResourceNotFoundException`: 리소스 미존재 예외
    - `LockAcquisitionException`: 락 획득 실패 예외

#### 인프라스트럭처 계층
- **Config**: 외부 시스템 연동 및 인프라 설정을 관리합니다.
    - `SecurityConfig`: Spring Security 설정
    - `RedisConfig`: Redis 연결 설정
    - `RedissonConfig`: Redisson 클라이언트 설정
- **Repository Impl**: 도메인 리포지토리 인터페이스의 구현을 제공합니다.
    - 필요한 경우 `UserRepositoryImpl`, `ProductRepositoryImpl` 등 커스텀 구현
- **Security**: 인증 및 권한 검사 관련 기능을 구현합니다.
    - `JwtTokenProvider`: JWT 토큰 생성 및 검증
    - `JwtAuthenticationFilter`: JWT 기반 인증 필터
- **Distributed**: 분산락 관련 기능을 구현합니다.
    - `DistributedLockAspect`: 분산락 AOP 구현
    - `DistributedLockTemplate`: 분산락 사용을 위한 템플릿

### 3.4 의존성 규칙

레이어드 아키텍처에서 가장 중요한 원칙은 의존성의 방향이 항상 외부 계층에서 내부 계층으로 향해야 한다는 것입니다.

```
프레젠테이션 -> 애플리케이션 -> 도메인 <- 인프라스트럭처
```

- 프레젠테이션 계층은 애플리케이션 계층에 의존합니다.
- 애플리케이션 계층은 도메인 계층에 의존합니다.
- 인프라스트럭처 계층은 도메인 계층에 의존합니다.
- 도메인 계층은 다른 어떤 계층에도 의존하지 않습니다.

이러한 의존성 규칙을 통해 핵심 비즈니스 로직을 보호하고, 기술적 변경에 따른 영향을 최소화할 수 있습니다. 또한 계층 간의 명확한 경계는 테스트 용이성과 유지보수성을 향상시킵니다.

### 3.5 분산락 아키텍처

분산 환경에서의 데이터 일관성을 위해 Redis 기반 분산락을 구현하였습니다:

- **선언적 락 적용**: `@DistributedLock` 어노테이션 사용
- **명령형 락 적용**: `DistributedLockTemplate` 유틸리티 클래스 사용
- **락 세분화 전략**: 리소스 유형 및 ID 기반 락 키 설계

이러한 구조를 통해 동시성 이슈를 효과적으로 제어하면서도 시스템의 확장성과 유연성을 유지할 수 있습니다.

## 4. 로깅 전략

local, dev, prod로 profile을 나눠 local, dev는 디버깅 시 편하게 디버깅 가능하도록, prod의 경우 로그가 너무 많이 쌓이지 않게 조정하여 WARN, ERROR 레벨의 에러를 traceid와 request 값을 함께 로그에 추가하여 에러가 발생 시, 로그를 보고 찾아갈수 있도록 조치합니다.
현업이라면 ELK의 ElasticAPM + Kibana, Datadog, Grafana 등을 사용해 모니터링 하겠지만 해당 부분은 시간상 구현이 불가능해 제외하였습니다.

### 4.1 profile 별 검토 사항
profile이 dev, prod 환경이라면 에러 로그를 logback을 사용해 slack과 같은 메신저로 웹훅을 통해 알람을 보내겠지만, 해당 부분은 검토하지 않았습니다.
또한, 롤링 규칙이나 운영 시 로그 관리 부분, 민감한 정보에 대한 마스킹 처리도 빠져있습니다.
시간 관계상 exception에 따른 에러 로깅을 상세화하여 에러가 발생한 구체적 원인, 요청 파라미터 등도 간략하게 설정했습니다.
