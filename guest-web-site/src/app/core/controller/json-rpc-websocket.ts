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

  WebSocketSubject does not reconnect with beta10 (change behaviour from beta7)
  https://github.com/ReactiveX/rxjs/issues/1863

  https://www.learnrxjs.io/operators/error_handling/retrywhen.html
  https://stackoverflow.com/questions/44911251/how-to-create-an-rxjs-retrywhen-with-delay-and-limit-on-tries
*/

import { Observable, Observer, Subject, Subscription } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig } from 'rxjs/websocket';

import { retryAfterDelay } from './retryAfterDelay.operator';

export class MultiEndpointMessage {
    constructor(public endpoint: number, public payload: string) { }
}

export interface JsonRpcChannel {
    send(message:string): void;
    incoming: Observable<string>;
}

export interface JsonRpcError {
    code: number;
    message: string;
    data?: any;
}

export interface JsonRpcRequest {
    jsonrpc: string;
    method: string;
    params?: any;
    id?: number;
}

export interface JsonRpcResponse {
    jsonrpc: string;
    result?: any;
    error?: JsonRpcError;
    // 'id' isn't really optional here but must match the JsonRpcRequest's
    // specification to create the 'JsonRpcMessage' union below.
    id?: number;
}

export type JsonRpcMessage
    = JsonRpcError
    | JsonRpcRequest
    | JsonRpcResponse;

export function isJsonRpcError(message: JsonRpcError | JsonRpcRequest | JsonRpcResponse): message is JsonRpcError {
    return (message as JsonRpcError).code !== undefined;
}

export function isJsonRpcRequest(message: JsonRpcError | JsonRpcRequest | JsonRpcResponse): message is JsonRpcRequest {
    return (message as JsonRpcRequest).method !== undefined;
}

export function isJsonRpcResponse(message: JsonRpcError | JsonRpcRequest | JsonRpcResponse): message is JsonRpcResponse {
    return (message as JsonRpcError).code === undefined && (message as JsonRpcRequest).method === undefined;
}

export class JsonRpcWebSocket {

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


    private webSocketSubject: WebSocketSubject<MultiEndpointMessage>;

    private endpoint:number = 1;
    private incoming$: Subject<JsonRpcMessage>
        = new Subject<JsonRpcResponse>();
    private webSocketSubscription: Subscription;


    constructor(configuration: WebSocketSubjectConfig<MultiEndpointMessage>) {

        let config = {
            ...configuration,
            deserializer: (event: MessageEvent) => {
                // console.log('response: ' + event.data);
                return new MultiEndpointMessage(parseInt(event.data.substring(0, event.data.indexOf(':'))),
                    event.data.substring(event.data.indexOf(':') + 1));
            },
            serializer: (message: MultiEndpointMessage) => {
                let serialized = message.endpoint + ':' + message.payload;
                // console.log('request: ' + serialized);
                return serialized;
            }
        };

        this.webSocketSubject = new WebSocketSubject<MultiEndpointMessage>(config);

        // Attempt the connection!
        //        this.websocketSubscription = this.websocketObservable.subscribe(
        this.webSocketSubscription = this.webSocketSubject.multiplex(
            () => new MultiEndpointMessage(this.endpoint, '{"jsonrpc":"2.0","method":"connect"}'),
            () => new MultiEndpointMessage(this.endpoint, '{"jsonrpc":"2.0","method":"disconnect"}'),
            (message) => {
                // console.log('endpoint: ' + message.endpoint);
                return message.endpoint == this.endpoint;
            }
        ).pipe(
            retryAfterDelay(5 * 1000, -1),
            map(message => message.payload)
        ).subscribe(
            (message) => {
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
            },
            (error: Event) => {
                this.logError('WebSocket error!', error);
            },
            () => {
                this.logMessage('WebSocket complete.');
            }
        );
    }

    private send<TRequest>(request: TRequest): void {
        this.webSocketSubject.next(new MultiEndpointMessage(this.endpoint, JSON.stringify(request)));
    }

    private id: number = 0;
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
    public call<TResponse>(method: string, parameters?: any): Observable<TResponse> {
        return Observable.create((observer: Observer<TResponse>) => {
            let id = ++this.id;
            const result = this.incoming$
                .pipe(
                    filter(response => isJsonRpcResponse(response) && response.id === id),
                    take(1),
                    map(response => (response as JsonRpcResponse).result))
                /*
            .pipe(
                filter(response => response.id === id && response.error),
                take(1),
                map(response => response.error)
            )
            */
                .subscribe(observer);
            this.send({
                jsonrpc: '2.0',
                method: method,
                params: parameters,
                id: id
            });
            return result;
        });
    }
}
