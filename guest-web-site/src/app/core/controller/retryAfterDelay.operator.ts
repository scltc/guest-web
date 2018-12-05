/**
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

import { MonoTypeOperatorFunction, Observable, SchedulerLike, iif, of, throwError } from 'rxjs';
import { concatMap, delay, mergeMap, retryWhen } from 'rxjs/operators';

export function retryAfterDelay<T>(
    // The inteval (milliseconds) between reconnection attempts.
    reconnectionInterval: number = 500,
    // The maximum number of times to attempt reconnection (-1 = retry forever).
    reconnectionAttempts: number = 5,
    scheduler?: SchedulerLike
): MonoTypeOperatorFunction<T> {
    return (source: Observable<T>) => source.pipe(
        retryWhen(errors => {
            return errors.pipe(
                concatMap((error, index) =>
                    iif(() => reconnectionAttempts > 0 && index >= reconnectionAttempts,
                        throwError(error),
                        of(error).pipe(delay(reconnectionInterval, scheduler))
                    )
                )
            )
        })
    );
}
