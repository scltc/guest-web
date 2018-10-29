import { Injectable, OnDestroy, OnInit } from '@angular/core';

import { Observable, Observer, Subject, Subscriber, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, takeWhile, tap } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

import { LoggerService } from '../logger.service';
import { JsonRpcService } from './json-rpc.service';

import { ControllerSocketService } from './controller-socket.service';

@Injectable({
    providedIn: 'root'
})
export class ControllerStatusService {

    public isConnected: boolean = false;

    // The connection status observable (true=connected, false=disconnectd).
    public get connected$() {
        return this.socket.connectedEvents;
    }

    private ping(value: number = 0, callback: (reply: number) => void = null): void {
        this.rpc.call<number>('ping', { value: value }).subscribe(message => {
            console.log('ping call back');
            if (callback) callback(message);
        }, error => {
            console.log('ping error: ' + error.message);
        });
    }

    /*
      public getControllerSettings(): void {
          this.call<ControllerSettings>('getSettings').subscribe(
              this.processSettings
          );
      }
  
      public setControllerSettings(settings: ControllerSettings): void {
          this.call<ControllerSettings>('setSettings', { settings: settings }).subscribe(this.logMessage);
      }
    */

    /*    this.connectedWatcher = this.connected$.subscribe(connected => {
 
                if (connected) {
                    this.getControllerSettings();
    
                    let settings: ControllerSettings = new ControllerSettings();
                    settings.catchAndThrow = [new CatchAndThrowSettings()];
                    this.setControllerSettings(settings);
                }
            });
    */

    private pingerSubscription;
    private statusSubscription;

    constructor(private socket: ControllerSocketService, private logger: LoggerService, private rpc: JsonRpcService) {

        this.pingerSubscription = interval(1000 * 5).subscribe(x => {
            this.ping(x, (reply) => this.logger.logMessage('ping received', reply));
        });

        this.statusSubscription = this.socket.connectedEvents.pipe(
            share(),
            distinctUntilChanged(),
        ).subscribe(status => {
            this.isConnected = status;
        });
    }
}
