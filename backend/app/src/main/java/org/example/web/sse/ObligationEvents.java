package org.example.web.sse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class ObligationEvents {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    public void obligationDeleted(UUID id) {
        ObligationDeletedEvent event = new ObligationDeletedEvent(id);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("obligation_deleted").data(event));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
