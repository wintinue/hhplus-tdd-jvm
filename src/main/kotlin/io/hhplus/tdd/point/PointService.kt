package io.hhplus.tdd.point

import io.hhplus.tdd.common.error.PointBadRequestException
import io.hhplus.tdd.common.error.PointErrorResponses
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable = UserPointTable(),
    private val pointHistoryTable: PointHistoryTable = PointHistoryTable(),
) {
    fun getUserPoint(userId: Long): UserPoint {
        return userPointTable.selectById(userId)
    }

    fun getUserPointHistory(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

    fun chargePoint(userId: Long, amount: Long): UserPoint {
        if (amount <= 0) {
            throw PointBadRequestException(PointErrorResponses.INVALID_CHARGE_AMOUNT)
        }

        val current = userPointTable.selectById(userId)
        val updatedPoint = current.point + amount
        val userPoint = userPointTable.insertOrUpdate(userId, updatedPoint)
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis)
        return userPoint
    }

    fun usePoint(userId: Long, amount: Long): UserPoint {
        if (amount <= 0) {
            throw PointBadRequestException(PointErrorResponses.INVALID_USE_AMOUNT)
        }

        val current = userPointTable.selectById(userId)
        if (amount > current.point) {
            throw PointBadRequestException(PointErrorResponses.INSUFFICIENT_POINT)
        }

        val updatedPoint = current.point - amount
        val userPoint = userPointTable.insertOrUpdate(userId, updatedPoint)
        pointHistoryTable.insert(userId, -amount, TransactionType.USE, userPoint.updateMillis)
        return userPoint
    }
}
