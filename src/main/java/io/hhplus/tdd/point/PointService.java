package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.AmountOverFlowException;
import io.hhplus.tdd.exception.WrongAmountException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private static final long MAX_POINT = 1_000_000;
    private static final String INVALID_AMOUNT_MESSAGE = "금액은 0보다 커야 합니다.";
    private static final String EXCEED_MAX_POINT_MESSAGE = "최대 잔고(" + MAX_POINT + " 포인트)를 초과할 수 없습니다.";
    private static final String INSUFFICIENT_BALANCE_MESSAGE = "잔고가 부족합니다.";

    /**
     * 유저의 포인트를 조회합니다.
     * @param userId 유저 ID
     * @return 유저의 포인트 정보
     */
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 유저의 포인트 충전/사용 내역을 조회합니다.
     * @param userId 유저 ID
     * @return 포인트 내역 리스트
     */
    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 유저의 포인트를 충전합니다.
     * @param userId 유저 ID
     * @param amount 충전할 금액
     * @return 업데이트된 유저 포인트 정보
     * @throws WrongAmountException 금액이 0 이하일 때
     * @throws AmountOverFlowException 최대 잔고를 초과할 때
     */
    public UserPoint chargePoint(long userId, long amount) {
        if (amount <= 0) {
            throw new WrongAmountException(INVALID_AMOUNT_MESSAGE);
        }

        UserPoint currentPoint = userPointTable.selectById(userId);
        long newPoint = currentPoint.point() + amount;

        if (newPoint > MAX_POINT) {
            throw new AmountOverFlowException(EXCEED_MAX_POINT_MESSAGE);
        }

        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newPoint);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return updatedPoint;
    }

    /**
     * 유저의 포인트를 사용합니다.
     * @param userId 유저 ID
     * @param amount 사용할 금액
     * @return 업데이트된 유저 포인트 정보
     * @throws WrongAmountException 금액이 0 이하일 때
     * @throws AmountOverFlowException 잔고가 부족할 때
     */
    public UserPoint usePoint(long userId, long amount) {
        if (amount <= 0) {
            throw new WrongAmountException(INVALID_AMOUNT_MESSAGE);
        }

        UserPoint currentPoint = userPointTable.selectById(userId);
        if (currentPoint.point() < amount) {
            throw new AmountOverFlowException(INSUFFICIENT_BALANCE_MESSAGE);
        }

        long newPoint = currentPoint.point() - amount;
        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newPoint);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        return updatedPoint;
    }
}