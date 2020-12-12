package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.event.MessageNotification;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationController {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(
        value = "/api/notifications/{userId}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE,
        headers = { "Connection=keep-alive", "Cache-Control=no-cache" }
    )
    public SseEmitter subscribeToMessageNotifications(
        @PathVariable String userId
    ) {
        SseEmitter emitter = new SseEmitter();
        this.emitters.put(userId, emitter);
        emitter.onCompletion(() -> this.emitters.remove(userId));
        emitter.onTimeout(
            () -> {
                emitter.complete();
                this.emitters.remove(userId);
            }
        );
        return emitter;
    }

    @EventListener
    public void onMessageNotification(MessageNotification notification) {
        String recipientId = notification.getRecipientId();
        SseEmitter emitter = emitters.get(recipientId);
        if (emitter != null) {
            try {
                emitter.send("new_message");
            } catch (IOException e) {
                this.emitters.remove(recipientId);
            }
        }
    }
}
