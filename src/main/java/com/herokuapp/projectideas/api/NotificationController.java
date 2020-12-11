package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.event.MessageNotification;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationController {

    private final HashMap<String, SseEmitter> emitters = new HashMap<>();

    @GetMapping(
        value = "/api/notifications/messages/{userId}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeToMessageNotifications(
        @PathVariable String userId
    ) {
        SseEmitter emitter = new SseEmitter();
        this.emitters.put(userId, emitter);
        emitter.onCompletion(() -> this.emitters.remove(userId));
        emitter.onTimeout(() -> this.emitters.remove(userId));
        return emitter;
    }

    @EventListener
    public void onMessageSent(MessageNotification notification) {
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
