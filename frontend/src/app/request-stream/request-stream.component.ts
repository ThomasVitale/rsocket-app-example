import {Component, OnDestroy, OnInit} from '@angular/core';
import {IdentitySerializer, JsonSerializer, RSocketClient} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import {CloudEvent} from "cloudevents";

@Component({
  selector: 'app-request-stream',
  templateUrl: './request-stream.component.html',
  styleUrls: ['./request-stream.component.css']
})
export class RequestStreamComponent implements OnInit, OnDestroy {

  messages: any[] = [];
  client: any;

  constructor() {}

  ngOnInit(): void {
    let host = location.host;
    // Creates an RSocket client based on the WebSocket network protocol
    this.client = new RSocketClient({
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
      },
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://' + host +'/ws/'
      }),
    });

    // Open an RSocket connection to the server
    this.client.connect().subscribe({
      onComplete: (socket: any) => {
        socket
          .requestStream({
            data: {"period": 2},
            metadata: this.route('request-stream')
          }).subscribe({
          onComplete: () => console.log('complete'),
          onError: (error: string) => {
            console.log("Connection has been closed due to: " + error);
          },
          onNext: (payload: { data: CloudEvent; }) => {
            console.log(payload);
            this.addMessage(payload.data);
          },
          onSubscribe: (subscription: any) => {
            subscription.request(1000000);
          },
        });
      },
      onError: (error: string) => {
        console.log("RSocket connection refused due to: " + error);
      },
      onSubscribe: (cancel: any) => {
        /* call cancel() to abort */
      }
    });
  }

  ngOnDestroy(): void {
    if (this.client) {
      this.client.close();
    }
  }

  route(value: string) : string {
    return String.fromCharCode(value.length) + value;
  }

  addMessage(newMessage: any) {
    console.log("Add message:" + JSON.stringify(newMessage))
    this.messages = [...this.messages, newMessage];
  }

}
