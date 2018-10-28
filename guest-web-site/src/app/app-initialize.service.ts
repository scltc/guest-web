import { Injectable } from '@angular/core';

import { Subject, Subscription, timer } from 'rxjs';
import { auditTime, takeWhile } from 'rxjs/operators';

import { ControllerStatusService } from "core";

@Injectable({
    providedIn: 'root'
})
export class AppInitializeService {

    public static initialRoute = '/';

    private initializeDone: Subject<boolean> = new Subject<boolean>();

    private timeoutWaiter: Subscription;
    private connectWaiter: Subscription;

    constructor(private status: ControllerStatusService) {
    }

    initialize(): Subject<boolean> {
        console.log('initialize() {');

        // Show our "initializing" page for two seconds, even when connected
        // because it looks cool!
        this.connectWaiter = this.status.connected$.pipe(
            auditTime(1000 * 2),
            takeWhile(connected => connected)
        ).subscribe(connected => {
            console.log('initialize() : status=' + connected);

            AppInitializeService.initialRoute = '/home';

            this.initializeDone.complete();
        })

        // Wait up to ten seconds for a connection.  The EV3 takes some time
        // to initialize the first time after power-up and this is likely
        // not long enough that one time but longer is annoying when really
        // not connected.
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