package com.imc4k.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class SseController {
    private static final Logger log = LoggerFactory.getLogger(SseController.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Map<String, SseEmitter> connectedSses = new HashMap<>();

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                log.error(e.toString());
            }
        }));
    }

    @GetMapping("/")
    public String getRoot() {
        return "hello";
    }

    // subscribe by GET /test-sse?id=
    @GetMapping("test-sse")
    public SseEmitter sendSse(@RequestParam String id) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

        sseEmitter.onCompletion(() -> log.info("SseEmitter is completed"));

        sseEmitter.onTimeout(() -> log.info("SseEmitter is timed out"));

        sseEmitter.onError((ex) -> log.info("SseEmitter got error:", ex));

        connectedSses.put(id, sseEmitter);
        try {
            sseEmitter.send("connected " + id);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Controller exits");
        return sseEmitter;
    }

    @PostMapping("send-message")
    public void startSendingToSse(@RequestBody MessagePayload messagePayload) {
        SseEmitter sseEmitter = connectedSses.get(messagePayload.getId());
        try {
            sseEmitter.send(messagePayload.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
