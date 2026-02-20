package io.hhplus.tdd.common.error

data class ErrorResponse(
    val code: String,
    val msg: String,
)

object PointErrorResponses {
    val INSUFFICIENT_POINT = ErrorResponse(
        code = "POINT_001",
        msg = "보유 포인트가 부족합니다.",
    )

    val INVALID_USE_AMOUNT = ErrorResponse(
        code = "POINT_002",
        msg = "사용 포인트는 0보다 커야 합니다.",
    )

    val INVALID_CHARGE_AMOUNT = ErrorResponse(
        code = "POINT_003",
        msg = "충전 포인트는 0보다 커야 합니다.",
    )
}

object CommonErrorResponses {
    val INTERNAL_SERVER_ERROR = ErrorResponse(
        code = "COMMON_500",
        msg = "에러가 발생했습니다.",
    )
}
