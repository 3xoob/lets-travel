package com.letstravel.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entity, Object id) {
        super(
            id != null ? entity + " not found: " + id : entity + " not found",
            HttpStatus.NOT_FOUND
        );
    }
}
