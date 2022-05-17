package com.thomasvitale.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

}

record Message(String content){}
record Settings(int period){}

@Controller
class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @MessageMapping("request-response")
    public Mono<Message> requestResponse(final Message message) {
        log.info("Received request-response request. Message: {}", message.content());
        return Mono.just(new Message("You wrote: " + message.content()));
    }

    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(final Message message) {
        log.info("Received fire-and-forget request. Message: {}", message.content());
        return Mono.empty();
    }

    @MessageMapping("request-stream")
    public Flux<Message> requestStream(final Settings settings) {
        log.info("Received request-stream request. Period: '{}'", settings.period());
        return Flux.interval(Duration.ofSeconds(settings.period()))
                .map(index -> new Message("Message " + index))
                .log();
    }

    @MessageMapping("stream-stream")
    public Flux<Message> channel(final Flux<Settings> settings) {
        log.info("Received stream-stream request.");
        return settings
                .doOnNext(setting -> log.info("Requested interval is {} seconds", setting.period()))
                .doOnCancel(() -> log.warn("The client cancelled the channel."))
                .switchMap(setting -> Flux.interval(Duration.ofSeconds(setting.period()))
                        .map(index -> new Message("Message " + index))
                ).log();
    }

}
