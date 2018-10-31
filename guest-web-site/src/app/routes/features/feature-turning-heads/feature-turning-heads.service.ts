import { Injectable } from '@angular/core';

import { Subscription } from 'rxjs';

import { JsonRpcService, LoggerService } from 'core';

@Injectable({
    providedIn: 'root'
})
export class FeatureTurningHeadsService {

    private requestSubscription: Subscription;

    constructor(private rpc: JsonRpcService, private logger: LoggerService) {

        this.requestSubscription = rpc.requestQueue.subscribe(request => {

            switch (request.method) {
                case 'ping':
                    logger.logMessage("got ping!")
                    rpc.reply(request, request.params);
                    break;
            }
        });
    }

    public getHeadsReservation(index: number) {
        this.rpc.call<number>("getHeadsReservation", { index: index }).subscribe(this.logger.logMessage)
    }

    public getHeadsDirection(index: number) {
        this.rpc.call<number>("getHeadsDirection", { index: index }).subscribe(this.logger.logMessage);
    }

    public setHeadsDirection(index: number, direction: number) {
        this.rpc.call<number>("setHeadsDirection", { index: index, direction: direction }).subscribe(this.logger.logMessage);
    }
}
