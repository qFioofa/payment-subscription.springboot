package org.example.web.sse;

import java.util.UUID;

public record ObligationDeletedEvent(String type, UUID id) {

    public ObligationDeletedEvent(UUID id) {
        this("obligation_deleted", id);
    }
}
