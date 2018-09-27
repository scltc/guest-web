import { Injectable } from '@angular/core';

import { Subject, Subscription, timer } from 'rxjs';
import { auditTime, takeWhile } from 'rxjs/operators';

import { ControllerService } from "core";

@Injectable()
export class AppInitializeService {

    public static initialRoute = '/';

    private initializeDone: Subject<boolean> = new Subject<boolean>();

    private timeoutWaiter: Subscription;
    private connectWaiter: Subscription;

    constructor(
        private controller: ControllerService,
    ) {
    }

    initialize(): Promise<any> {
        console.log('initialize() {');

        this.connectWaiter = this.controller.connected$.pipe(auditTime(1000 * 2), takeWhile(connected => connected)).subscribe(connected => {
            console.log('initialize() : status=' + connected);

            AppInitializeService.initialRoute = '/home';

            this.initializeDone.complete();
        })

        this.timeoutWaiter = timer(1000 * 10).subscribe(n => {
            console.log('initialize() : interval elapsed.');

            AppInitializeService.initialRoute = '/away';

            this.initializeDone.complete();
        });

        this.initializeDone.subscribe(() => {}, () => {}, () => {
            console.log('initialize() : initializeDone');

            this.connectWaiter.unsubscribe();
            this.timeoutWaiter.unsubscribe();
            //this.initializeDone.unsubscribe();
        })


        console.log('initialize() }');

        return this.initializeDone.toPromise();
    }

    public static isInitialRouteAway() {
        return AppInitializeService.initialRoute === "/away";
    }

    public static isInitialRouteHome() {
        return AppInitializeService.initialRoute === "/home";
    }

    public static init_app(appLoadService: AppInitializeService) {
        return () => appLoadService.initialize();
    }
}