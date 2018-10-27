// Original ideas from:
//   Using WebSockets in Angular with RxJs WebSocketSubject


import { Injectable, OnDestroy, Inject } from '@angular/core';

import { Observable, Observer, Subject, Subscriber, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, takeWhile, tap } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

import { JsonRpcWebSocket } from './json-rpc-websocket';

export class CatchAndThrowSettings {
    public westMinIdle: number = 0;
    public westMaxIdle: number = 0;

    public mainRunTime: number = 0;

    public eastMinIdle: number = 0;
    public eastMaxIdle: number = 0;
}

export class HeadTurnerSettings {
    public controller: string = null;
    public port: string = null;
    public leftDutyCycle: number = 0;
    public rightDutyCycle: number = 0;
    public motorRunTime: number = 0;
}

export class ControllerSettings {

    public catchAndThrow: CatchAndThrowSettings[];
    public headTurner: HeadTurnerSettings[];
}

@Injectable({
    providedIn: 'root'
})
export class ControllerService extends JsonRpcWebSocket implements OnDestroy {

    private pinger: Observable<number> = interval(1000 * 5);
    private pingerSubscription: Subscription;
    private connectedWatcher: Subscription;

    public status: Observable<boolean>;

    public processSettings(settings: ControllerSettings) {
        super.logMessage('settings: ', settings);
    }

    public getControllerSettings(): void {
        this.call<ControllerSettings>('getSettings').subscribe(
            this.processSettings
        );
    }

    public setControllerSettings(settings: ControllerSettings): void {
        this.call<ControllerSettings>('setSettings', { settings: settings }).subscribe(this.logMessage);
    }

    public getHeadsDirection(index: number) {
        this.call<number>("getHeadsDirection", { index: index }).subscribe(this.logMessage);
    }

    public setHeadsDirection(index: number, direction: number) {
        this.call<number>("setHeadsDirection", { index: index, direction: direction }).subscribe(this.logMessage);
    }

    public runCatchAndThrow(index: number) {
        this.call<number>("runCatchAndThrow", { index: index }).subscribe(this.logMessage);
    }

    private connectedSubscriber: Subscriber<boolean>;

    public isConnected: boolean = false;

    // The connection status observable (true=connected, false=disconnectd).
    public connected$: Observable<boolean> = new Observable<boolean>((subscriber) => {
        this.connectedSubscriber = subscriber;
    }).pipe(
        share(),
        distinctUntilChanged(),
        tap(state => this.isConnected = state)
    );

    constructor(@Inject('string') private url: string) {
        super({
            url: url,
            closeObserver: {
                next: (event: CloseEvent) => {
                    this.logError('WebSocket disconnected.', event);
                    this.connectedSubscriber.next(false);
                }
            },
            openObserver: {
                next: (event: Event) => {
                    this.logMessage('WebSocket connected!');
                    this.connectedSubscriber.next(true);
                }
            }
        });

        this.logMessage('WebSocket.constructor(' + url + ')');

        this.connectedWatcher = this.connected$.subscribe(connected => {
            if (connected) {
                this.getControllerSettings();

                let settings: ControllerSettings = new ControllerSettings();
                settings.catchAndThrow = [new CatchAndThrowSettings()];
                this.setControllerSettings(settings);
            }
        });

        this.pingerSubscription = this.pinger.subscribe(x => {
            this.logMessage('pinger', this.isConnected);
            if (this.isConnected) {
                this.call<string>('ping', { value: x })
                    .subscribe((message) => this.logMessage('ping received', message));
            }
        });
    }

    ngOnDestroy() {
        this.pingerSubscription.unsubscribe();
    }
}
