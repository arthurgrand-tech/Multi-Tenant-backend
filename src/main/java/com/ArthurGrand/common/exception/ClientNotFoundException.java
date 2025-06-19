package com.ArthurGrand.common.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(Integer clientId) {
        super("Client with ID '" + clientId + "' not found.");
    }

    public ClientNotFoundException(String email) {
        super("Client with email '" + email + "' not found.");
    }
}