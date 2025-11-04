package charging_manage_be.controller.count;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/countdown")
public class CountdownController {

    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter startCountdown(@RequestParam int minutes) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        LocalDateTime endTime = LocalDateTime.now().plusMinutes(minutes);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                long remainingSeconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();

                if (remainingSeconds <= 0) {
                    Map<String, Object> finalData = new HashMap<>();
                    finalData.put("remainingSeconds", 0);
                    finalData.put("status", "COMPLETED");
                    finalData.put("message", "Countdown finished!");

                    emitter.send(SseEmitter.event()
                            .name("countdown")
                            .data(finalData));

                    emitter.complete();
                    executor.shutdown();
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("remainingSeconds", remainingSeconds);
                data.put("remainingMinutes", remainingSeconds / 60);
                data.put("displayTime", formatTime(remainingSeconds));
                data.put("endTime", endTime);
                data.put("status", "RUNNING");

                emitter.send(SseEmitter.event()
                        .name("countdown")
                        .data(data));

            } catch (Exception e) {
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        emitter.onError(e -> executor.shutdown());

        return emitter;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
