/***
 * A basic implementation of JSON-RPC 2.0 over WebSocket.
 */
/*
References:
  JSON-RPC 2.0 Specification
  https://www.jsonrpc.org/specification

  Auto-reconnecting and address-tracking WebSocket client
  https://jonaschapuis.com/2018/02/auto-reconnecting-and-address-tracking-websocket-client/

  Using WebSockets in Angular with RxJs WebSocketSubject
  https://medium.com/@alexdasoul/%D0%B8%D1%81%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5-websockets-%D0%B2-angular-c-rxjs-websocketsubject-5018ecc20ee5
  https://github.com/Angular-RU/angular-websocket-starter

  rxjs/webSocket
  https://rxjs-dev.firebaseapp.com/api/webSocket/webSocket

  https://github.com/ReactiveX/rxjs/blob/master/src/internal/observable/dom/WebSocketSubject.ts
*/

import { Observable, Observer, Subject, Subscriber, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, take, takeWhile } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

import { ReconnectingWebSocket, ReconnectingWebSocketConfiguration } from "./reconnecting-websocket"

export class MultiEndpointWebSocketMessage {
    constructor(public endpoint: number, public payload: string) { }
}

export interface MultiEndpointWebSocketConfiguration extends ReconnectingWebSocketConfiguration<MultiEndpointWebSocketMessage> {
}

export interface MultiEndpointWebSocketEndpoint {

}

/*
export class MultiEndpointWebSocket extends ReconnectingWebSocket<MultiEndpointWebSocketMessage> {

    private endpoints = new Array<MultiEndpointWebSocket

    constructor(configuration: MultiEndpointWebSocketConfiguration) {
        super({
            ...configuration,
            deserializer: (event: MessageEvent) => {
                console.log('endpoint: ' + event.data.substring(0, event.data.indexOf(':')));
                return new MultiEndpointWebSocketMessage(
                    parseInt(event.data.substring(0, event.data.indexOf(':'))),
                    event.data.substring(event.data.indexOf(':') + 1)
                );
            },
            serializer: (value: any) => {
                return '1:' + JSON.stringify(value);
            }
        });

}
*/