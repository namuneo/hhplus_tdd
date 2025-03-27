package io.hhplus.tdd;

import io.hhplus.tdd.exception.AmountOverFlowException;
import io.hhplus.tdd.exception.UserNotFoundException;
import io.hhplus.tdd.exception.WrongAmountException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(404).body(new ErrorResponse("404", exception.getMessage()));
    }

    @ExceptionHandler(value = AmountOverFlowException.class)
    public ResponseEntity<ErrorResponse> handleAmountOverFlow(AmountOverFlowException exception) {
        return ResponseEntity.status(404).body(new ErrorResponse("404", exception.getMessage()));
    }

    @ExceptionHandler(value = WrongAmountException.class)
    public ResponseEntity<ErrorResponse> handleWrongAmount(WrongAmountException exception) {
        return ResponseEntity.status(404).body(new ErrorResponse("404", exception.getMessage()));
    }
}
