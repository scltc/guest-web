import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { JsonRpcService, LoggerService } from 'core';

import { ExpansionPanelPagerDirective } from '../../../shared'; // '../../../shared/expansion-panel/expansion-panel-pager.directive';

export class CatchAndThrowFeatureStatus {
  status: number;     // -1=Waiting, 0=Canceled, +1=Playing
  timer: number;
  direction: number;  // -1=left, +1=right
  running: boolean;
}

@Component({
  selector: 'app-catch-and-throw-feature',
  templateUrl: './catch-and-throw-feature.component.html',
  styleUrls: ['../features.scss']
})
export class CatchAndThrowFeatureComponent implements OnInit {

  @ViewChild(ExpansionPanelPagerDirective, { static: true })
  pager: ExpansionPanelPagerDirective;

  public instance: number = 0;

  public waitTime: number = 0;
  public playTime: number = 0;
  public playSound: boolean = false;
  private currentDirection: number = 0;

  private count: number = 0;

  constructor(private rpc: JsonRpcService, private snackBar: MatSnackBar, private logger: LoggerService) {
  }

  public abandon() {
    this.rpc.call<CatchAndThrowFeatureStatus>('catcherAbandon', { instance: this.instance })
      .subscribe((status) => this.logger.logMessage('catcherAbandon result: ', status)); //this.update(status));
  }

  public change(value: number) {
    console.log('catcher set current: ' + this.currentDirection + ', requested: ' + value);
    if (value !== this.currentDirection) {
      console.log('catcher set changed!')
      this.rpc.call<CatchAndThrowFeatureStatus>('catcherOperate', { instance: this.instance, direction: value })
        .subscribe((status) => /* this.logger.logMessage('headsDirection result: ', status)); // */ this.update(status));
    }
  }

  public get direction(): number {
    return this.currentDirection;
  }

  public pageClosed(page: string) {
    this.playSound = false;
    if (page == "waiting") {
      this.waitTime = 0;
    }
    else if (page == 'playing') {
      this.playTime = 0;
    }
  }

  public pageOpened(page: string) {
    if (page == 'waiting') {
    }
    else if (page == 'playing') {
      this.playSound = true;
    }
  }

  public reserve() {
    this.rpc.call<CatchAndThrowFeatureStatus>('catcherReserve', { instance: this.instance })
      .subscribe((status) => this.logger.logMessage('catcherReserve result: ', status));
  }

  public get running(): boolean {
    // console.log('running: ' + (this.currentDirection != +1) + ', currentDirection: ' + this.currentDirection);
    return this.count > 1;
  }

  public show(message: string) {
    this.snackBar.open(message, null, { duration: 3000, panelClass: 'center-snackbar', verticalPosition: 'bottom' });
  }

  private update(status: CatchAndThrowFeatureStatus) {
    this.logger.logMessage('FeatureCatchAndThrow.update(' + status.status + ', ' + status.direction + ', ' + status.timer + ')');
    if (status.status < 0) {
      // Waiting.
      this.playTime = 0;
      this.waitTime = status.timer;
      this.pager.display('waiting');
    } else if (status.status > 0) {
      // Playing.
      this.currentDirection = status.direction;
      this.playTime = status.timer;
      this.waitTime = 0;
      this.pager.display('playing');
    } else {
      // Canceled.
      this.playTime = 0;
      this.waitTime = 0;
      this.pager.close();
    }
  }

  private requestSubscription: Subscription;

  ngOnDestroy() {
    console.log('FeatureCatchAndThrow.ngOnDestroy()');
    this.requestSubscription.unsubscribe();
  }

  ngOnInit() {
    this.currentDirection = 0;
    this.requestSubscription = this.rpc.requestQueue.pipe(filter(request =>
      request.method == 'catcherChanged'
    )).subscribe(request => {
      this.logger.logMessage('catcherChanged', request);
      this.update(request.params as CatchAndThrowFeatureStatus);
    });
  }
}