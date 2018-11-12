import { EventEmitter, Injectable, OnDestroy, OnInit } from '@angular/core';

import { Observable, Observer, Subject, Subscriber, Subscription, interval } from 'rxjs';
import { distinctUntilChanged, filter, map, share, takeWhile, tap } from 'rxjs/operators';
import { WebSocketSubject, WebSocketSubjectConfig, webSocket } from 'rxjs/websocket';

import { LoggerService } from '../logger.service';
import { JsonRpcService } from './json-rpc.service';

import { ControllerSocketService } from './controller-socket.service';

@Injectable({
    providedIn: 'root'
})
export class ControllerStatusService implements OnDestroy {

    public isConnected: boolean = false;

    // The connection status observable (true=connected, false=disconnectd).
    public get connected$(): EventEmitter<boolean> {
        return this.socket.connectedEvents;
    }

    private ping(value: number = 0, callback: (reply: number) => void = null): void {
        this.rpc.call<number>('ping', { value: value }).subscribe(message => {
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

    private pingerSubscription: Subscription;
    private statusSubscription: Subscription;
    private requestSubscription: Subscription;

    constructor(private socket: ControllerSocketService, private logger: LoggerService, private rpc: JsonRpcService) {

        this.requestSubscription = rpc.requestQueue.subscribe(request => {

            switch (request.method) {
                case 'ping':
                    logger.logMessage('got ping!');
                    rpc.reply(request, request.params);
                    break;
            }
        });

        this.pingerSubscription = interval(1000 * 5).subscribe(x => {
            // this.ping(x, (reply) => this.logger.logMessage('ping received', reply));
        });

        this.statusSubscription = this.socket.connectedEvents.pipe(
            share(),
            distinctUntilChanged(),
        ).subscribe(status => {
            this.isConnected = status;
        });
    }

    ngOnDestroy() {
        this.requestSubscription.unsubscribe();
        this.pingerSubscription.unsubscribe();
        this.statusSubscription.unsubscribe();
    }
}
