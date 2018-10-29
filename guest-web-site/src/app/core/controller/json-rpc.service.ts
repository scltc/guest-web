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

import { Injectable } from '@angular/core';
import { Observable, Observer, Subject, Subscription, iif, of, throwError } from 'rxjs';
import { concatMap, first, map, switchMap } from 'rxjs/operators';

import { retryAfterDelay } from './retryAfterDelay.operator';

import { ControllerSocketService, MultiEndpointMessage } from './controller-socket.service';
import { LoggerService } from '../logger.service';

export interface JsonRpcError {
    code: number;
    message: string;
    data?: any;
}

export interface JsonRpcRequest {
    jsonrpc: string;
    id?: number;
    method: string;
    params?: any;
}

export interface JsonRpcResponse {
    jsonrpc: string;
    id?: number;
    result?: any;
    error?: JsonRpcError;
    // 'id' isn't really optional here but must match the JsonRpcRequest's
    // specification to create the 'JsonRpcMessage' union below.
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

@Injectable({
    providedIn: 'root'
})
export class JsonRpcService {

    private serverEndpoint: number = 1;
    private clientEndpoint: number = 2;
    private requestQueue: Subject<JsonRpcRequest>
        = new Subject<JsonRpcRequest>();
    private requestSubscription: Subscription;
    private responseQueue: Subject<JsonRpcResponse>
        = new Subject<JsonRpcResponse>();
    private webSocketSubscription: Subscription;

    constructor(private controller: ControllerSocketService, private logger: LoggerService) {

        this.webSocketSubscription = controller.webSocketSubject.multiplex(
            () => new MultiEndpointMessage(this.serverEndpoint, '{"jsonrpc":"2.0","method":"connect"}'),
            () => new MultiEndpointMessage(this.serverEndpoint, '{"jsonrpc":"2.0","method":"disconnect"}'),
            (message) => {
                // console.log('endpoint: ' + message.endpoint);
                return message.endpoint === this.serverEndpoint || message.endpoint === this.clientEndpoint;
            }
        ).pipe(
            retryAfterDelay(5 * 1000, -1),
            map(message => JSON.parse(message.payload))
        ).subscribe(
            (message) => {
                if (isJsonRpcRequest(message)) {
                    this.logger.logMessage('request', message);
                    this.requestQueue.next(message);
                }
                else if (isJsonRpcResponse(message)) {
                    // this.logger.logMessage('response', message);
                    this.responseQueue.next(message);
                }
                else {
                    this.logger.logMessage('error, ', message);
                }
            },
            (error: Event) => {
                this.logger.logError('WebSocket error!', error);
            },
            () => {
                this.logger.logMessage('WebSocket complete.');
            }
        );

        this.requestSubscription = this.requestQueue.subscribe(request => {
            this.send(this.clientEndpoint, {
                jsonrpc: '2.0',
                id: request.id,
                result: request.params
            });
        })
    }

    private send<TRequest>(endpoint: number, request: TRequest): void {
        this.controller.webSocketSubject.next(new MultiEndpointMessage(endpoint, JSON.stringify(request)));
    }

    private id: number = 0;

    public call<TResponse>(method: string, parameters?: any): Observable<TResponse> {
        return Observable.create((observer: Observer<TResponse>) => {
            let id = ++this.id;
            const result = this.responseQueue
                .pipe(
                    first(response => response.id === id),
                    concatMap(response =>
                        response.error ? throwError(response.error) : of(response.result))
                )
                .subscribe(observer);
            this.send(this.serverEndpoint, {
                jsonrpc: '2.0',
                id: id,
                method: method,
                params: parameters
            });
            return result;
        });
    }
}
