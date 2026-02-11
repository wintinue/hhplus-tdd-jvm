package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable

class PointService {
    fun getUserPoint(userId: Long): UserPoint {
        return UserPointTable().selectById(userId)
    }

    fun getUserPointHistory(userId: Long): List<PointHistory> {
        return PointHistoryTable().selectAllByUserId(userId)
    }

    fun chargePoint(userId: Long, amount: Long): UserPoint {
        val userPoint = UserPointTable().insertOrUpdate(userId, amount)
        PointHistoryTable().insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis)
        return userPoint
    }

    fun usePoint(userId: Long, amount: Long): UserPoint {
        val userPoint = chargePoint(userId, -amount)
        PointHistoryTable().insert(userId, -amount, TransactionType.USE, userPoint.updateMillis)
        return userPoint
    }
}

