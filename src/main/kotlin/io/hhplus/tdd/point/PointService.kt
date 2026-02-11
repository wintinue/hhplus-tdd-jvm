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
        return UserPointTable().insertOrUpdate(userId, amount)
    }

    fun usePoint(userId: Long, amount: Long): UserPoint {
        return chargePoint(userId, -amount)
    }
}

