package com.liftkart.cart.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}