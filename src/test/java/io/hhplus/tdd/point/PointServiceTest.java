package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.AmountOverFlowException;
import io.hhplus.tdd.exception.WrongAmountException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("유저 포인트 조회 성공")
    void getUserPoint_success() {
        // Arrange
        long userId = 1L;
        UserPoint mockPoint = new UserPoint(userId, 500L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(mockPoint);

        // Act
        UserPoint result = pointService.getUserPoint(userId);

        // Assert
        assertEquals(500L, result.point());
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("포인트 내역 조회 성공 - 내역 존재")
    void getPointHistory_success() {
        // Arrange
        long userId = 1L;
        PointHistory history = new PointHistory(1L, userId, 100L, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> mockHistories = List.of(history);
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(mockHistories);

        // Act
        List<PointHistory> result = pointService.getPointHistory(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).amount());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName("포인트 내역 조회 성공 - 내역 없음")
    void getPointHistory_empty() {
        // Arrange
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<PointHistory> result = pointService.getPointHistory(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePoint_success() {
        // Arrange
        long userId = 1L;
        long amount = 200L;
        UserPoint currentPoint = new UserPoint(userId, 300L, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, 500L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);
        when(userPointTable.insertOrUpdate(userId, 500L)).thenReturn(updatedPoint);

        // Act
        UserPoint result = pointService.chargePoint(userId, amount);

        // Assert
        assertEquals(500L, result.point());
        verify(userPointTable, times(1)).insertOrUpdate(userId, 500L);
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("포인트 충전 실패 - 유효하지 않은 금액")
    void chargePoint_invalidAmount() {
        // Arrange
        long userId = 1L;
        long amount = 0L;

        // Act & Assert
        assertThrows(WrongAmountException.class, () -> pointService.chargePoint(userId, amount));
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최대 잔고 초과")
    void chargePoint_exceedMaxPoint() {
        // Arrange
        long userId = 1L;
        long amount = 1_000_001L;
        UserPoint currentPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);

        // Act & Assert
        assertThrows(AmountOverFlowException.class, () -> pointService.chargePoint(userId, amount));
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoint_success() {
        // Arrange
        long userId = 1L;
        long amount = 200L;
        UserPoint currentPoint = new UserPoint(userId, 500L, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, 300L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);
        when(userPointTable.insertOrUpdate(userId, 300L)).thenReturn(updatedPoint);

        // Act
        UserPoint result = pointService.usePoint(userId, amount);

        // Assert
        assertEquals(300L, result.point());
        verify(userPointTable, times(1)).insertOrUpdate(userId, 300L);
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(amount), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 유효하지 않은 금액")
    void usePoint_invalidAmount() {
        // Arrange
        long userId = 1L;
        long amount = -1L;

        // Act & Assert
        assertThrows(WrongAmountException.class, () -> pointService.usePoint(userId, amount));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔고 부족")
    void usePoint_insufficientBalance() {
        // Arrange
        long userId = 1L;
        long amount = 600L;
        UserPoint currentPoint = new UserPoint(userId, 500L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);

        // Act & Assert
        assertThrows(AmountOverFlowException.class, () -> pointService.usePoint(userId, amount));
    }
}