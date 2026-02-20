package io.hhplus.tdd.point

import io.hhplus.tdd.common.error.PointBadRequestException
import io.hhplus.tdd.common.error.PointErrorResponses
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable

@ExtendWith(MockitoExtension::class)
@DisplayName("PointService")
class PointServiceTest {
    @Mock
    lateinit var userPointTable: UserPointTable

    @Mock
    lateinit var pointHistoryTable: PointHistoryTable

    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    @Test
    fun givenNewUser_whenGetUserPoint_thenReturnsZeroPoint() {
        // Given: 포인트 정보가 없는 신규 유저 ID를 준비한다.
        val userId = 1L
        val userPoint = UserPoint(id = userId, point = 0L, updateMillis = 1L)
        given(userPointTable.selectById(userId)).willReturn(userPoint)

        // When: getUserPoint(userId)를 호출한다.
        val result = pointService.getUserPoint(userId)

        // Then: point=0인 UserPoint를 반환한다.
        assertEquals(userId, result.id)
        assertEquals(0L, result.point)
        then(userPointTable).should().selectById(userId)
    }

    @Test
    fun givenExistingUser_whenGetUserPoint_thenReturnsCurrentPoint() {
        // Given: 기존 유저의 포인트 데이터가 저장되어 있다.
        val userId = 2L
        val saved = UserPoint(id = userId, point = 1000L, updateMillis = 10L)
        given(userPointTable.selectById(userId)).willReturn(saved)

        // When: getUserPoint(userId)를 호출한다.
        val result = pointService.getUserPoint(userId)

        // Then: 저장된 현재 포인트를 반환한다.
        assertEquals(userId, result.id)
        assertEquals(1000L, result.point)
        then(userPointTable).should().selectById(userId)
    }

    @Test
    fun givenNoHistory_whenGetUserPointHistory_thenReturnsEmptyList() {
        // Given: 해당 유저의 충전/사용 이력이 없다.
        val userId = 3L
        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(emptyList())

        // When: getUserPointHistory(userId)를 호출한다.
        val histories = pointService.getUserPointHistory(userId)

        // Then: 빈 리스트를 반환한다.
        assertTrue(histories.isEmpty())
        then(pointHistoryTable).should().selectAllByUserId(userId)
    }

    @Test
    fun givenExistingHistories_whenGetUserPointHistory_thenReturnsHistoryList() {
        // Given: 해당 유저의 충전/사용 이력이 1건 이상 존재한다.
        val userId = 4L
        val histories = listOf(
            PointHistory(id = 1L, userId = userId, type = TransactionType.CHARGE, amount = 1000L, timeMillis = 10L),
            PointHistory(id = 2L, userId = userId, type = TransactionType.USE, amount = -300L, timeMillis = 20L),
        )
        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(histories)

        // When: getUserPointHistory(userId)를 호출한다.
        val result = pointService.getUserPointHistory(userId)

        // Then: 시간순 이력 리스트를 반환한다.
        assertEquals(2, result.size)
        assertEquals(userId, result.first().userId)
        then(pointHistoryTable).should().selectAllByUserId(userId)
    }

    @Test
    fun givenValidChargeAmount_whenChargePoint_thenReturnsUpdatedPoint() {
        // Given: 유효한 충전 금액(amount > 0)을 준비한다.
        val userId = 5L
        val amount = 1000L
        val current = UserPoint(id = userId, point = 500L, updateMillis = 10L)
        val updated = UserPoint(id = userId, point = 1500L, updateMillis = 20L)
        given(userPointTable.selectById(userId)).willReturn(current)
        given(userPointTable.insertOrUpdate(userId, 1500L)).willReturn(updated)

        // When: chargePoint(userId, amount)를 호출한다.
        val result = pointService.chargePoint(userId, amount)

        // Then: 충전 후 UserPoint를 반환하고 CHARGE 이력을 저장한다.
        assertEquals(userId, result.id)
        assertEquals(1500L, result.point)
        then(userPointTable).should().selectById(userId)
        then(userPointTable).should().insertOrUpdate(userId, 1500L)
        then(pointHistoryTable).should()
            .insert(userId, amount, TransactionType.CHARGE, updated.updateMillis)
    }

    @Test
    fun givenInvalidChargeAmount_whenChargePoint_thenThrowsException() {
        // Given: 유효하지 않은 충전 금액(amount <= 0)을 준비한다.
        val userId = 6L
        val amount = 0L

        // When: chargePoint(userId, amount)를 호출한다.
        val exception = assertThrows<PointBadRequestException> {
            pointService.chargePoint(userId, amount)
        }

        // Then: 예외를 던진다. (PointErrorResponses.INVALID_CHARGE_AMOUNT)
        assertEquals(PointErrorResponses.INVALID_CHARGE_AMOUNT, exception.errorResponse)
        assertEquals(PointErrorResponses.INVALID_CHARGE_AMOUNT.msg, exception.message)
    }

    @Test
    fun givenSmallerAmountThanCurrentPoint_whenUsePoint_thenReturnsRemainingPoint() {
        // Given: 현재 포인트보다 작은 사용 금액(amount)을 준비한다.
        val userId = 7L
        val amount = 300L
        val current = UserPoint(id = userId, point = 1000L, updateMillis = 10L)
        val updated = UserPoint(id = userId, point = 700L, updateMillis = 20L)
        given(userPointTable.selectById(userId)).willReturn(current)
        given(userPointTable.insertOrUpdate(userId, 700L)).willReturn(updated)

        // When: usePoint(userId, amount)를 호출한다.
        val result = pointService.usePoint(userId, amount)

        // Then: 차감된 UserPoint를 반환하고 USE 이력을 저장한다.
        assertEquals(userId, result.id)
        assertEquals(700L, result.point)
        then(userPointTable).should().selectById(userId)
        then(userPointTable).should().insertOrUpdate(userId, 700L)
        then(pointHistoryTable).should()
            .insert(userId, -amount, TransactionType.USE, updated.updateMillis)
    }

    @Test
    fun givenAmountEqualToCurrentPoint_whenUsePoint_thenReturnsZeroPoint() {
        // Given: 현재 포인트와 동일한 사용 금액(amount)을 준비한다.
        val userId = 8L
        val amount = 500L
        val current = UserPoint(id = userId, point = 500L, updateMillis = 10L)
        val updated = UserPoint(id = userId, point = 0L, updateMillis = 20L)
        given(userPointTable.selectById(userId)).willReturn(current)
        given(userPointTable.insertOrUpdate(userId, 0L)).willReturn(updated)

        // When: usePoint(userId, amount)를 호출한다.
        val result = pointService.usePoint(userId, amount)

        // Then: point=0인 UserPoint를 반환하고 USE 이력을 저장한다.
        assertEquals(userId, result.id)
        assertEquals(0L, result.point)
        then(pointHistoryTable).should()
            .insert(userId, -amount, TransactionType.USE, updated.updateMillis)
    }

    @Test
    fun givenAmountGreaterThanCurrentPoint_whenUsePoint_thenThrowsException() {
        // Given: 현재 포인트보다 큰 사용 금액(amount)을 준비한다.
        val userId = 9L
        val amount = 1000L
        given(userPointTable.selectById(userId)).willReturn(UserPoint(userId, 100L, 10L))

        // When: usePoint(userId, amount)를 호출한다.
        val exception = assertThrows<PointBadRequestException> {
            pointService.usePoint(userId, amount)
        }

        // Then: 예외를 던진다. (PointErrorResponses.INSUFFICIENT_POINT)
        assertEquals(PointErrorResponses.INSUFFICIENT_POINT, exception.errorResponse)
        assertEquals(PointErrorResponses.INSUFFICIENT_POINT.msg, exception.message)
    }

    @Test
    fun givenNegativeAmount_whenUsePoint_thenThrowsException() {
        // Given: 음수 사용 금액(amount < 0)을 준비한다.
        val userId = 10L
        val amount = -100L

        // When: usePoint(userId, amount)를 호출한다.
        val exception = assertThrows<PointBadRequestException> {
            pointService.usePoint(userId, amount)
        }

        // Then: 예외를 던진다. (PointErrorResponses.INVALID_USE_AMOUNT)
        assertEquals(PointErrorResponses.INVALID_USE_AMOUNT, exception.errorResponse)
        assertEquals(PointErrorResponses.INVALID_USE_AMOUNT.msg, exception.message)
    }
}
