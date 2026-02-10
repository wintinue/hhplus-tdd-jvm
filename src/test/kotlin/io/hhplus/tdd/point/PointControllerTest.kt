package io.hhplus.tdd.point

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PointController")
class PointControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {

    @Test
    @DisplayName("특정 유저의 포인트를 조회한다")
    fun getPointByUserId() {
        val userId = 1L

        mockMvc.get("/point/{id}", userId) {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id", `is`(userId.toInt()))
                jsonPath("$.point", greaterThanOrEqualTo(0))
                jsonPath("$.updateMillis", greaterThanOrEqualTo(0))
            }
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회한다")
    fun getPointHistoriesByUserId() {
        val userId = 1L

        mockMvc.get("/point/{id}/histories", userId) {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                jsonPath("$").isArray()
            }
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전한다")
    fun chargePoint() {
        val userId = 1L
        val amount = 1000L

        mockMvc.patch("/point/{id}/charge", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = amount.toString()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id", `is`(userId.toInt()))
                jsonPath("$.point", `is`(amount.toInt()))
            }
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용한다")
    fun usePoint() {
        val userId = 1L

        mockMvc.patch("/point/{id}/charge", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = "1000"
        }
            .andExpect { status { isOk() } }

        mockMvc.patch("/point/{id}/use", userId) {
            contentType = MediaType.APPLICATION_JSON
            content = "300"
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id", `is`(userId.toInt()))
                jsonPath("$.point", `is`(700))
            }
    }
}
