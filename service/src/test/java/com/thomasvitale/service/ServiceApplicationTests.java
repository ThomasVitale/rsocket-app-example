package com.thomasvitale.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class ServiceApplicationTests {

    private static RSocketRequester requester;

    @BeforeAll
    static void setupOnce(
            @Autowired RSocketRequester.Builder builder,
            @LocalRSocketServerPort Integer port
    ) {
        requester = builder.tcp("localhost", port);
    }

    @Test
    void testRequestResponse() {
        var messageToSend = new Message("I like 'The Lord of The Rings'!");

        Mono<Message> messageResponse = requester
                .route("request-response")
                .data(messageToSend)
                .retrieveMono(Message.class);

        StepVerifier.create(messageResponse)
                .expectNext(new Message("You wrote: " + messageToSend.content()))
                .verifyComplete();
    }

    @Test
    void testFireAndForget() {
        var messageToSend = new Message("I like 'The Lord of The Rings'!");

        Mono<Void> messageResponse = requester
                .route("fire-and-forget")
                .data(messageToSend)
                .retrieveMono(Void.class);

        StepVerifier.create(messageResponse)
                .verifyComplete();
    }

    @Test
    void testRequestStream() {
        var settingsToSend = new Settings(1);

        Flux<Message> requestStream = requester
                .route("request-stream")
                .data(settingsToSend)
                .retrieveFlux(Message.class);

        StepVerifier.create(requestStream)
                .thenCancel()
                .verify();
    }

}
