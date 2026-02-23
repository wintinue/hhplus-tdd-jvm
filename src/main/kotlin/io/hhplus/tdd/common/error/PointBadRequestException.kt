package io.hhplus.tdd.common.error

class PointBadRequestException(
    val errorResponse: ErrorResponse,
) : IllegalArgumentException(errorResponse.msg)
