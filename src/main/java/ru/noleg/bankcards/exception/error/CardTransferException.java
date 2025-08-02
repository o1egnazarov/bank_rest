package ru.noleg.bankcards.exception.error;

public class CardTransferException extends RuntimeException {
    public CardTransferException(String message) {
        super(message);
    }
}
