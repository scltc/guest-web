/***
 * Add reconnection capabilities to a RxJS WebSocketSubject.
 */

/*
  References:

  The RxJS WebSocketSubject source code:
  https://github.com/ReactiveX/rxjs/blob/master/src/internal/observable/dom/WebSocketSubject.ts

  WebSocketSubject does not reconnect with beta10 (change behaviour from beta7)
  https://github.com/ReactiveX/rxjs/issues/1863

  How to create an RXjs RetryWhen with delay and limit on tries?
  https://stackoverflow.com/questions/44911251/how-to-create-an-rxjs-retrywhen-with-delay-and-limit-on-tries
*/

import { Observable, iif, of, throwError, Subscription } from 'rxjs';
import { concatMap, delay, retryWhen } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

export interface ReconnectingWebSocketConfiguration<TMessage> extends WebSocketSubjectConfig<TMessage> {
    // The maximum number of times to attempt reconnection (-1 = retry forever).
    reconnectionAttempts?: number;
    // The inteval (milliseconds) between reconnection attempts.
    reconnectionInterval?: number;
}

export class ReconnectingWebSocket<TMessage> {

    public websocket$: WebSocketSubject<TMessage> = null;
    public websocketObservable: Observable<TMessage>;
    public websocketReconnector: Subscription;

    protected logMessage(message: any, data?: any) {
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

    constructor(configuration: ReconnectingWebSocketConfiguration<TMessage>) {

        // Resolve our reconnection parameters.
        let reconnectionAttempts = configuration.reconnectionAttempts || 10;
        let reconnectionInterval = configuration.reconnectionInterval || 500;

        // Create the socket.
        this.websocket$ = webSocket<TMessage>(configuration);

        // Wire up the reconnection magic!
        this.websocketObservable = this.websocket$.pipe(
            retryWhen(errors => {
                this.logError('retryWhen');
                return errors.pipe(
                    concatMap((error, index) =>
                        iif(() => reconnectionAttempts > 0 && index >= reconnectionAttempts,
                            throwError(error),
                            of(error).pipe(delay(reconnectionInterval))
                        )
                    )
                )
            })
        );

        this.websocketReconnector = this.websocketObservable.subscribe();
    }
    
    multiplex(subMsg: () => any, unsubMsg: () => any, messageFilter: (value: TMessage) => boolean) {
        this.websocket$.multiplex(subMsg, unsubMsg, messageFilter);
    }
}
