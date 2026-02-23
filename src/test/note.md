### 통합테스트에 가까운 테스트 케이스
```kotlin
    // 기존 비즈니스 규칙 중심 케이스 (보존)
    @Test
    fun givenNewUser_whenGetPoint_thenReturnsZero() {
        // Given: 신규 유저 ID를 준비한다.
        // When: GET /point/{id} 요청을 보낸다.
        // Then: 상태코드 200과 point=0을 반환한다.
        val userId = 1L
    }

    @Test
    fun givenExistingUser_whenGetPoint_thenReturnsExistingPoint() {
        // Given: 기존 유저의 포인트가 미리 적립되어 있다.
        // When: GET /point/{id} 요청을 보낸다.
        // Then: 상태코드 200과 저장된 기존 point 값을 반환한다.
    }

    @Test
    fun givenNewUser_whenGetPointHistory_thenReturnsEmptyHistory() {
        // Given: 신규 유저로 충전/사용 이력이 없다.
        // When: GET /point/{id}/histories 요청을 보낸다.
        // Then: 상태코드 200과 빈 배열([])을 반환한다.
    }

    @Test
    fun givenExistingUser_whenGetPointHistory_thenReturnsHistories() {
        // Given: 기존 유저의 충전/사용 이력이 1건 이상 존재한다.
        // When: GET /point/{id}/histories 요청을 보낸다.
        // Then: 상태코드 200과 이력 목록(타입/금액/시간)을 반환한다.
    }

    @Test
    fun givenExistingUser_whenChargePoint_thenReturnsChargedPoint() {
        // Given: 기존 유저와 충전 금액(amount)을 준비한다.
        // When: PATCH /point/{id}/charge 요청으로 amount를 전달한다.
        // Then: 상태코드 200과 (기존 포인트 + amount) 값을 반환한다.
    }

    @Test
    fun givenSmallerAmountThanCurrentPoint_whenUsePoint_thenReturnsRemainingPoint() {
        // Given: 현재 포인트보다 작은 사용 금액(amount)을 준비한다.
        // When: PATCH /point/{id}/use 요청으로 amount를 전달한다.
        // Then: 상태코드 200과 (현재 포인트 - amount) 값을 반환한다.
    }

    @Test
    fun givenAmountEqualToCurrentPoint_whenUsePoint_thenReturnsZero() {
        // Given: 현재 포인트와 동일한 사용 금액(amount)을 준비한다.
        // When: PATCH /point/{id}/use 요청으로 amount를 전달한다.
        // Then: 상태코드 200과 point=0을 반환한다.
    }

    @Test
    fun givenAmountGreaterThanCurrentPoint_whenUsePoint_thenRejectsRequest() {
        // Given: 현재 포인트보다 큰 사용 금액(amount)을 준비한다.
        // When: PATCH /point/{id}/use 요청으로 amount를 전달한다.
        // Then: 실패 응답을 반환한다. (PointErrorResponses.INSUFFICIENT_POINT)
    }

    @Test
    fun givenNegativeAmount_whenUsePoint_thenRejectsRequest() {
        // Given: 음수 사용 금액(amount < 0)을 준비한다.
        // When: PATCH /point/{id}/use 요청으로 amount를 전달한다.
        // Then: 실패 응답을 반환한다. (PointErrorResponses.INVALID_USE_AMOUNT)
    }
```