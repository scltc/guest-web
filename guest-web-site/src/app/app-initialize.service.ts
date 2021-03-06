import { Injectable } from '@angular/core';

import { Subject, Subscription, timer } from 'rxjs';
import { auditTime, takeWhile, tap } from 'rxjs/operators';

import { ControllerSocketService, JsonRpcService } from "core";

@Injectable({
    providedIn: 'root'
})
export class AppInitializeService {

    public static initialRoute = '/';

    private initializeDone: Subject<boolean> = new Subject<boolean>();

    private timeoutWaiter: Subscription;
    private connectWaiter: Subscription;

    constructor(private controller: ControllerSocketService, private rpc: JsonRpcService) {
    }

    initialize(): Subject<boolean> {
        console.log('initialize() {');

        // Show our "initializing" page for an extra second after connection
        // because it looks cool!
        this.connectWaiter = this.controller.connected.pipe(
            //auditTime(1000 * 2),
            tap(connected => console.log('tap 1 connected=' + connected)),
            takeWhile(connected => connected),
            tap(connected => console.log('tap 2 connected=' + connected))
        ).subscribe(connected => {

        // Using Angular 8, we don't need to delay the initializing page
        // display to prevent its annoying jump so we can remove it sooner.
//      this.connectWaiter = this.controller.connected.subscribe(connected => {
            console.log('initialize() : connected=' + connected);

            AppInitializeService.initialRoute = '/home';

            this.initializeDone.complete();
        });

        // Wait up to ten seconds for a connection.  The EV3 takes some time
        // to initialize the first time after a power-up and, while this is
        // likely not long enough that one time, any longer is quite annoying
        // when we really cannot connect.
        this.timeoutWaiter = timer(1000 * 10).subscribe(n => {
            console.log('initialize() : interval elapsed.');

            AppInitializeService.initialRoute = '/away';

            this.initializeDone.complete();
        });

        this.initializeDone.subscribe({ complete: () => {
            console.log('initialize() : initializeDone');

            this.connectWaiter.unsubscribe();
            this.timeoutWaiter.unsubscribe();
        }});

        console.log('initialize() }');

        return this.initializeDone;
    }

    public static isInitialRouteAway() {
        return AppInitializeService.initialRoute === "/away";
    }

    public static isInitialRouteHome() {
        return AppInitializeService.initialRoute === "/home";
    }

    public static init_app(appLoadService: AppInitializeService) {
        return () => appLoadService.initialize().toPromise();
    }
}