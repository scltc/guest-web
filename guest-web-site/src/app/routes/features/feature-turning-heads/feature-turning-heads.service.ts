import { Injectable } from '@angular/core';

import { JsonRpcService, LoggerService } from 'core';

@Injectable({
  providedIn: 'root'
})
export class FeatureTurningHeadsService {

  constructor(private rpc: JsonRpcService, private logger: LoggerService) {
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
