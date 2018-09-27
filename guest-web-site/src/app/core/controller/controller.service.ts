// Original ideas from:
//   Using WebSockets in Angular with RxJs WebSocketSubject


import { Injectable, OnDestroy, Inject } from '@angular/core';

import { Observable, Observer, Subject, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, takeWhile } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

import { JsonRpcWebSocket, JsonRpcWebSocketConfiguration } from '../json-rpc/JsonRpcWebSocket';

export class CatchAndThrowSettings {
    public westMinIdle: number = 0;
    public westMaxIdle: number = 0;

    public mainRunTime: number = 0;

    public eastMinIdle: number = 0;
    public eastMaxIdle: number = 0;
}

export class HeadTurnerSettings {
    public controller : string = null;
    public port : string = null;
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
        this.logMessage(settings);
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

    public runCatchAndThrow(index : number) {
        this.call<number>("runCatchAndThrow", { index: index }).subscribe(this.logMessage);
    }

    constructor(@Inject('string') private url: string) {
        super({ url: url, reconnectionAttempts: -1 });

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
