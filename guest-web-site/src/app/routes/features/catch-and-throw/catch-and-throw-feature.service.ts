import { Injectable } from '@angular/core';

import { JsonRpcService, LoggerService } from 'core';

@Injectable({
  providedIn: 'root'
})
export class FeatureCatchAndThrowService {

  constructor(private rpc: JsonRpcService, private logger: LoggerService) {
  }

  public runCatchAndThrow(index: number) {
    this.rpc.call<number>("runCatchAndThrow", { index: index }).subscribe(this.logger.logMessage);
  }
}
