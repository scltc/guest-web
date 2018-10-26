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

export interface MultiEndpointWebSocketMessage {
    endpoint: number;
    payload: any;
}

export interface MultiEndpointWebSocketConfiguration {
    url: string;
    pingInterval?: number;
    reconnectionAttempts?: number;
    reconnectionInterval?: number;
}

export class MultiEndpointWebSocket {

    private websocket$: WebSocketSubject<MultiEndpointWebSocketMessage>
        = null;
    private websocketconfiguration: WebSocketSubjectConfig<any>
    private reconnectionAttempts: number;
    private reconnectionInterval: number;

    public logMessage(message: any, data?: any) {
        if (data) {
            console.log(message, JSON.stringify(data));
        }
        else {
            console.log(message);
        }
    }

    protected logError(message: any, data?: any) {
        if (data) {
            console.error(message, JSON.stringify(data));
        }
        else {
            console.error(message);
        }
    }

    private incoming$: Subject<MultiEndpointWebSocketMessage>
        = new Subject<MultiEndpointWebSocketMessage>();
    private outgoing$: Subject<MultiEndpointWebSocketMessage>
        = new Subject<MultiEndpointWebSocketMessage>();

    private connectedSubscriber: Subscriber<boolean>;
    private connectedSubscription: Subscription;
    private reconnector$: Observable<number>;

    public isConnected: boolean = false;

    // The connection status observable (true=connected, false=disconnectd).
    public connected$: Observable<boolean> = new Observable<boolean>((subscriber) => {
        this.connectedSubscriber = subscriber;
    }).pipe(share(), distinctUntilChanged());

    private connect() {
        this.websocket$ = webSocket<MultiEndpointWebSocketMessage>(this.websocketconfiguration);

        this.websocket$.subscribe(
            (message) => {
/*
                if (isJsonRpcRequest(message)) {
                    this.logMessage('request', message);
                }
                else if (isJsonRpcResponse(message)) {
                    this.logMessage('response', message);
                    this.incoming$.next(message);
                }
                else {
                    this.logMessage('error, ', message);
                }
*/
            },
            (error: Event) => {
                this.logError('WebSocket error!', error);
                /* if (!this.websocket$) */ {
                    // run reconnect if errors
                    this.reconnect();
                }
            },
            () => {
                this.logMessage('WebSocket complete.');
            }
        );
    }

    private reconnect(): void {
        this.reconnector$ = interval(this.reconnectionInterval)
            .pipe(
                takeWhile((v, index) => (this.reconnectionAttempts < 0 || index < this.reconnectionAttempts) && !this.websocket$)
            );

        this.reconnector$.subscribe(
            () => {
                this.logMessage('Reconnect attempt. reconectAtempts: ' + this.reconnectionAttempts)
                this.connect();
            },
            null,
            () => {
                this.logError('reconnect failed.');
                // Subject complete if reconnect attempts ending
                this.reconnector$ = null;

                if (!this.websocket$) {
                    // this.wsMessages$.complete();
                    // this.connection$.complete();
                }
            }
        );
    }

    constructor(configuration: MultiEndpointWebSocketConfiguration) {

        // Create the WebSocketSubject configuration.
        this.websocketconfiguration = {
            url: configuration.url,
            closeObserver: {
                next: (event: CloseEvent) => {
                    this.logMessage('WebSocket.closeObserver()', event);
                    this.websocket$ = null;
                    this.connectedSubscriber.next(false);
                }
            },
            openObserver: {
                next: (event: Event) => {
                    this.logMessage('WebSocket.openObserver()', event);
                    this.logMessage('WebSocket connected!');
                    this.connectedSubscriber.next(true);
                }
            },
            deserializer: (event: MessageEvent) => {
                console.log('endpoint: ' + event.data.substring(0, event.data.indexOf(':')));
                return JSON.parse(event.data.substring(event.data.indexOf(':') + 1));
            },
            serializer: (value: any) => {
                return '1:' + JSON.stringify(value);
            }

        };

        this.reconnectionAttempts = configuration.reconnectionAttempts || 10; // number of connection attempts
        this.reconnectionInterval = configuration.reconnectionInterval || 5000; // pause between connections

        // run reconnect if not connection
        this.connectedSubscription = this.connected$.subscribe((isConnected) => {

            this.isConnected = isConnected;

            if (!this.reconnector$ && !isConnected) {
                this.reconnect();
            }
        });

        this.connect();
    }
}
