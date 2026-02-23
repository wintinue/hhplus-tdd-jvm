package io.hhplus.tdd.point

import io.hhplus.tdd.common.error.CommonErrorResponses
import io.hhplus.tdd.common.error.PointBadRequestException
import io.hhplus.tdd.common.error.PointErrorResponses
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@WebMvcTest(PointController::class)
@DisplayName("PointController")
class PointControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @MockBean
    lateinit var pointService: PointService

    @Test
    fun givenValidUserId_whenGetPoint_thenReturns200AndUserPointPayload() {
        // Given: 유효한 userId와 서비스가 반환할 UserPoint를 준비한다.
        val userId = 1L
        val userPoint = UserPoint(id = userId, point = 1000L, updateMillis = 123456789L)
        given(pointService.getUserPoint(userId)).willReturn(userPoint)

        // When: GET /point/{id} 요청을 보낸다.
        val result = mockMvc.get("/point/{id}", userId) {
            accept = MediaType.APPLICATION_JSON
        }

        // Then: 200 OK와 UserPoint 응답 필드(id, point, updateMillis) 매핑을 검증한다.
        result.andExpect {
            status { isOk() }
            jsonPath("$.id", `is`(userId.toInt()))
            jsonPath("$.point", `is`(userPoint.point.toInt()))
            jsonPath("$.updateMillis", `is`(userPoint.updateMillis.toInt()))
        }
    }

    @Test
    fun givenNonNumericUserId_whenGetPoint_thenReturns400() {
        // Given: 숫자가 아닌 userId(path variable)를 준비한다.
        val invalidUserId = "abc"

        // When: GET /point/{id} 요청을 보낸다.
        val result = mockMvc.get("/point/{id}", invalidUserId) {
            accept = MediaType.APPLICATION_JSON
        }

        // Then: 400 Bad Request를 반환한다.
        result.andExpect { status { isBadRequest() } }
    }

    @Test
    fun givenServiceThrowsException_whenGetPoint_thenReturnsErrorResponse() {
        // Given: 서비스에서 예외가 발생하도록 스텁한다.
        val userId = 1L
        given(pointService.getUserPoint(userId)).willThrow(RuntimeException("unexpected"))

        // When: GET /point/{id} 요청을 보낸다.
        val result = mockMvc.get("/point/{id}", userId) {
            accept = MediaType.APPLICATION_JSON
        }

        // Then: 에러 응답(code, msg)과 상태코드를 검증한다. (CommonErrorResponses.INTERNAL_SERVER_ERROR)
        result.andExpect {
            status { isInternalServerError() }
            jsonPath("$.code", `is`(CommonErrorResponses.INTERNAL_SERVER_ERROR.code))
            jsonPath("$.msg", `is`(CommonErrorResponses.INTERNAL_SERVER_ERROR.msg))
        }
    }

    @Test
    fun givenValidUserId_whenGetPointHistories_thenReturns200AndHistoryArray() {
        // Given: 유효한 userId와 서비스가 반환할 이력 목록을 준비한다.
        val userId = 1L
        val histories = listOf(
            PointHistory(
                id = 1L,
                userId = userId,
                amount = 1000L,
                type = TransactionType.CHARGE,
                timeMillis = 1111L,
            ),
            PointHistory(
                id = 2L,
                userId = userId,
                amount = -500L,
                type = TransactionType.USE,
                timeMillis = 2222L,
            ),
        )
        given(pointService.getUserPointHistory(userId)).willReturn(histories)

        // When: GET /point/{id}/histories 요청을 보낸다.
        val result = mockMvc.get("/point/{id}/histories", userId) {
            accept = MediaType.APPLICATION_JSON
        }

        // Then: 200 OK와 배열 응답의 필드(userId, amount, type, timeMillis) 매핑을 검증한다.
        result.andExpect {
            status { isOk() }
            jsonPath("$[0].userId", `is`(userId.toInt()))
            jsonPath("$[0].amount", `is`(1000))
            jsonPath("$[0].type", `is`("CHARGE"))
            jsonPath("$[0].timeMillis", `is`(1111))
        }
    }

    @Test
    fun givenEmptyHistories_whenGetPointHistories_thenReturnsEmptyArray() {
        // Given: 서비스가 빈 이력 목록을 반환하도록 스텁한다.
        val userId = 999L
        given(pointService.getUserPointHistory(userId)).willReturn(emptyList())

        // When: GET /point/{id}/histories 요청을 보낸다.
        val result = mockMvc.get("/point/{id}/histories", userId) {
            accept = MediaType.APPLICATION_JSON
        }

        // Then: 200 OK와 빈 배열([])을 반환한다.
        result.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun givenValidChargeRequest_whenChargePoint_thenReturns200AndUpdatedUserPoint() {
        // Given: 유효한 userId와 amount, 서비스가 반환할 UserPoint를 준비한다.
        val userId = 2L
        val amount = 1000L
        val charged = UserPoint(id = userId, point = amount, updateMillis = 2222L)
        given(pointService.chargePoint(userId, amount)).willReturn(charged)

        // When: PATCH /point/{id}/charge 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/charge", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = amount.toString()
        }

        // Then: 200 OK와 갱신된 UserPoint 응답 매핑을 검증한다.
        result.andExpect {
            status { isOk() }
            jsonPath("$.id", `is`(userId.toInt()))
            jsonPath("$.point", `is`(amount.toInt()))
            jsonPath("$.updateMillis", `is`(charged.updateMillis.toInt()))
        }
    }

    @Test
    fun givenMissingRequestBody_whenChargePoint_thenReturns400() {
        // Given: 요청 본문이 없는 충전 요청을 준비한다.
        val userId = 3L

        // When: PATCH /point/{id}/charge 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/charge", userId) {
            contentType = MediaType.APPLICATION_JSON
        }

        // Then: 400 Bad Request를 반환한다.
        result.andExpect { status { isBadRequest() } }
    }

    @Test
    fun givenInvalidBodyFormat_whenChargePoint_thenReturns400() {
        // Given: Long으로 파싱할 수 없는 요청 본문을 준비한다.
        val userId = 4L

        // When: PATCH /point/{id}/charge 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/charge", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = "\"invalid-number\""
        }

        // Then: 400 Bad Request를 반환한다.
        result.andExpect { status { isBadRequest() } }
    }

    @Test
    fun givenValidUseRequest_whenUsePoint_thenReturns200AndUpdatedUserPoint() {
        // Given: 유효한 userId와 amount, 서비스가 반환할 UserPoint를 준비한다.
        val userId = 5L
        val amount = 500L
        val used = UserPoint(id = userId, point = 500L, updateMillis = 3333L)
        given(pointService.usePoint(userId, amount)).willReturn(used)

        // When: PATCH /point/{id}/use 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/use", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = amount.toString()
        }

        // Then: 200 OK와 갱신된 UserPoint 응답 매핑을 검증한다.
        result.andExpect {
            status { isOk() }
            jsonPath("$.id", `is`(userId.toInt()))
            jsonPath("$.point", `is`(used.point.toInt()))
            jsonPath("$.updateMillis", `is`(used.updateMillis.toInt()))
        }
    }

    @Test
    fun givenInsufficientPointException_whenUsePoint_thenReturnsErrorResponse() {
        // Given: 서비스가 잔액 부족 예외를 던지도록 스텁한다.
        val userId = 6L
        val amount = 999999L
        given(pointService.usePoint(userId, amount))
            .willThrow(PointBadRequestException(PointErrorResponses.INSUFFICIENT_POINT))

        // When: PATCH /point/{id}/use 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/use", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = amount.toString()
        }

        // Then: 실패 응답(code, msg)을 반환한다. (PointErrorResponses.INSUFFICIENT_POINT)
        result.andExpect {
            status { isBadRequest() }
            jsonPath("$.code", `is`(PointErrorResponses.INSUFFICIENT_POINT.code))
            jsonPath("$.msg", `is`(PointErrorResponses.INSUFFICIENT_POINT.msg))
        }
    }

    @Test
    fun givenInvalidUseAmountException_whenUsePoint_thenReturnsErrorResponse() {
        // Given: 서비스가 유효하지 않은 사용 금액 예외를 던지도록 스텁한다.
        val userId = 7L
        val amount = -100L
        given(pointService.usePoint(userId, amount))
            .willThrow(PointBadRequestException(PointErrorResponses.INVALID_USE_AMOUNT))

        // When: PATCH /point/{id}/use 요청을 보낸다.
        val result = mockMvc.patch("/point/{id}/use", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = amount.toString()
        }

        // Then: 실패 응답(code, msg)을 반환한다. (PointErrorResponses.INVALID_USE_AMOUNT)
        result.andExpect {
            status { isBadRequest() }
            jsonPath("$.code", `is`(PointErrorResponses.INVALID_USE_AMOUNT.code))
            jsonPath("$.msg", `is`(PointErrorResponses.INVALID_USE_AMOUNT.msg))
        }
    }
}
