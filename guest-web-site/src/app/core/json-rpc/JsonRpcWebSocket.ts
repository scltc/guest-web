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
*/

import { Observable, Observer, Subject, Subscriber, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, take, takeWhile } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

export interface JsonRpcRequest {
    jsonrpc: string;
    method: string;
    params?: any;
    id?: number;
}

export interface JsonRpcError {
    code: number;
    message: string;
    data?: any;
}

export interface JsonRpcResponse {
    jsonrpc: string;
    result?: any;
    error?: JsonRpcError;
    // 'id' isn't really optional, but must be marked that way to create
    // the 'JsonRpcMessage' union below.
    id?: number;
}

export type JsonRpcMessage
    = JsonRpcRequest
    | JsonRpcResponse;

export interface JsonRpcWebSocketConfiguration {
    url: string;
    pingInterval?: number;
    reconnectionAttempts?: number;
    reconnectionInterval?: number;
}

export class JsonRpcWebSocket {

    private websocket$: WebSocketSubject<JsonRpcMessage>
        = null;
    private websocketconfiguration: WebSocketSubjectConfig<any>
    private reconnectionAttempts: number;
    private reconnectionInterval: number;

    protected logMessage(message: any, data?: any) {
        if (data) {
            console.error(message, JSON.stringify(data));
        }
        else {
            console.error(message);
        }
    }

    private incoming$: Subject<JsonRpcResponse>
        = new Subject<JsonRpcResponse>();
    private outgoing$: Subject<JsonRpcRequest>
        = new Subject<JsonRpcRequest>();

    private connectedSubscriber: Subscriber<boolean>;
    private connectedSubscription: Subscription;
    private reconnector$: Observable<number>;

    public isConnected: boolean = false;

    // The connection status observable (true=connected, false=disconnectd).
    public connected$: Observable<boolean> = new Observable<boolean>((subscriber) => {
        this.connectedSubscriber = subscriber;
    }).pipe(share(), distinctUntilChanged());

    private connect() {
        this.websocket$ = webSocket<JsonRpcMessage>(this.websocketconfiguration);

        this.websocket$.subscribe(
            (message) => {
                this.logMessage('receive', message);
                this.incoming$.next(message);
            },
            (error: Event) => {
                this.logMessage('WebSocket error!', error);
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
                this.logMessage('reconnect failed.');
                // Subject complete if reconnect attempts ending
                this.reconnector$ = null;

                if (!this.websocket$) {
                    // this.wsMessages$.complete();
                    // this.connection$.complete();
                }
            }
        );
    }

    constructor(configuration: JsonRpcWebSocketConfiguration) {

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

    private id : number = 0;
/*
    public call<TResponse, TRequest>(method: string, parameters: TRequest): Observable<TResponse> {
        return Observable.create((observer: Observer<TResponse>) => {
            let id = ++this.id;
            const result = this.incoming$
                .pipe(
                    filter(response => response.id === id && response.result),
                    take(1),
                    map(response => response.result))
                .pipe(
                    filter(response => response.id === id && response.error),
                    take(1),
                    map(response => response.error)
                )
                .subscribe(observer);
            this.websocket$.next({
                jsonrpc: '2.0',
                method: method,
                params: parameters,
                id: id
            });
            return result;
        })
    }
*/
    public call<TResponse>(method : string, parameters? : any): Observable<TResponse> {
        return Observable.create((observer: Observer<TResponse>) => {
            let id = ++this.id;
            const result = this.incoming$
                .pipe(
                    filter(response => response.id === id),
                    take(1),
                    map(response => response.result))
                    /*
                .pipe(
                    filter(response => response.id === id && response.error),
                    take(1),
                    map(response => response.error)
                )
                */
                .subscribe(observer);
            this.websocket$.next({
                jsonrpc: '2.0',
                method: method,
                params: parameters,
                id: id
            });
            return result;
        });
    }
}
