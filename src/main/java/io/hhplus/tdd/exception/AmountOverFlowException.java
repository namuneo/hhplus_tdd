package io.hhplus.tdd.exception;

public class AmountOverFlowException extends RuntimeException {
    public AmountOverFlowException(String message) {
        super(message);
    }
}