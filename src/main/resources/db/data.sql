-- 사용자 데이터
INSERT INTO product.users (email, password, name, role, created_at, updated_at)
VALUES
    ('admin@example.com', '$2a$10$whLE754sZOV0I9BcRTPN9euAQIMLe1rbAWVvrpAQS.BQ5Kd8BOjHy', '관리자', 'ADMIN', NOW(), NOW()),
    ('user@example.com', '$2a$10$whLE754sZOV0I9BcRTPN9euAQIMLe1rbAWVvrpAQS.BQ5Kd8BOjHy', '일반사용자', 'USER', NOW(), NOW());
-- 비밀번호는 모두 'password123'

-- 상품 데이터
INSERT INTO product.products (name, description, price, shipping_fee, stock, created_at, updated_at, created_by, last_modified_by)
VALUES
    ('스마트폰', '최신 스마트폰입니다.', 1000000.00, 3000.00, 1000, NOW(), NOW(), 2, 2),
    ('노트북', '가벼운 노트북입니다.', 1500000.00, 5000.00, 100, NOW(), NOW(), 2, 2),
    ('무선이어폰', '고음질 무선이어폰입니다.', 200000.00, 2500.00, 10, NOW(), NOW(), 2, 2),
    ('스마트워치', '건강관리에 좋은 스마트워치입니다.', 300000.00, 2500.00, 1,NOW(), NOW(), 2, 2),
    ('블루투스 스피커', '휴대성이 좋은 스피커입니다.', 80000.00, 2500.00, 0, NOW(), NOW(), 2, 2);

-- 상품 옵션 데이터
INSERT INTO product.product_options (product_id, name, option_type, additional_price, created_at, updated_at, created_by, last_modified_by)
VALUES
-- 스마트폰 옵션
(1, '색상', 'SELECT', 0, NOW(), NOW(), 2, 2),
(1, '용량', 'SELECT', 0, NOW(), NOW(), 2, 2),
(1, '케이스', 'SELECT', 30000.00, NOW(), NOW(), 2, 2),

-- 노트북 옵션
(2, '색상', 'SELECT', 0, NOW(), NOW(), 2, 2),
(2, 'CPU', 'SELECT', 200000.00, NOW(), NOW(), 2, 2),
(2, '메모리', 'SELECT', 100000.00, NOW(), NOW(), 2, 2),

-- 무선이어폰 옵션
(3, '색상', 'SELECT', 0, NOW(), NOW(), 2, 2),
(3, '충전케이스', 'SELECT', 20000.00, NOW(), NOW(), 2, 2),

-- 스마트워치 옵션
(4, '색상', 'SELECT', 0, NOW(), NOW(), 2, 2),
(4, '밴드타입', 'SELECT', 15000.00, NOW(), NOW(), 2, 2),

-- 블루투스 스피커 옵션
(5, '색상', 'SELECT', 0, NOW(), NOW(), 2, 2);

-- 옵션 값 데이터
INSERT INTO product.option_values (product_option_id, value, created_at, updated_at, created_by, last_modified_by)
VALUES
-- 스마트폰 색상
(1, '블랙', NOW(), NOW(), 2, 2),
(1, '화이트', NOW(), NOW(), 2, 2),
(1, '블루', NOW(), NOW(), 2, 2),

-- 스마트폰 용량
(2, '128GB', NOW(), NOW(), 2, 2),
(2, '256GB', NOW(), NOW(), 2, 2),
(2, '512GB', NOW(), NOW(), 2, 2),

-- 스마트폰 케이스
(3, '투명케이스', NOW(), NOW(), 2, 2),
(3, '가죽케이스', NOW(), NOW(), 2, 2),
(3, '실리콘케이스', NOW(), NOW(), 2, 2),

-- 노트북 색상
(4, '그레이', NOW(), NOW(), 2, 2),
(4, '실버', NOW(), NOW(), 2, 2),

-- 노트북 CPU
(5, 'i5', NOW(), NOW(), 2, 2),
(5, 'i7', NOW(), NOW(), 2, 2),
(5, 'i9', NOW(), NOW(), 2, 2),

-- 노트북 메모리
(6, '8GB', NOW(), NOW(), 2, 2),
(6, '16GB', NOW(), NOW(), 2, 2),
(6, '32GB', NOW(), NOW(), 2, 2),

-- 무선이어폰 색상
(7, '화이트', NOW(), NOW(), 2, 2),
(7, '블랙', NOW(), NOW(), 2, 2),

-- 무선이어폰 충전케이스
(8, '일반형', NOW(), NOW(), 2, 2),
(8, '무선충전형', NOW(), NOW(), 2, 2),

-- 스마트워치 색상
(9, '블랙', NOW(), NOW(), 2, 2),
(9, '실버', NOW(), NOW(), 2, 2),
(9, '골드', NOW(), NOW(), 2, 2),

-- 스마트워치 밴드타입
(10, '실리콘', NOW(), NOW(), 2, 2),
(10, '메탈', NOW(), NOW(), 2, 2),
(10, '가죽', NOW(), NOW(), 2, 2),

-- 블루투스 스피커 색상
(11, '블랙', NOW(), NOW(), 2, 2),
(11, '화이트', NOW(), NOW(), 2, 2),
(11, '블루', NOW(), NOW(), 2, 2);