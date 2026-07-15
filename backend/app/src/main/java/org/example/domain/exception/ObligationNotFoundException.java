package org.example.domain.exception;

import java.util.UUID;

public class ObligationNotFoundException extends RuntimeException {

    public ObligationNotFoundException(UUID id) {
        super("Обязательство не найдено: " + id);
    }
}
