import { Component, ContentChild, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { JsonRpcService, LoggerService } from 'core';

import { ExpansionPanelPagerDirective } from '../expansion-panel-pager.directive';

export class FeatureTurningHeadsStatus {
  status: number;     // -1=Waiting, 0=Canceled, +1=Playing
  timer: number;
  direction: number;  // -1=left, +1=right;
}

@Component({
  selector: 'app-feature-turning-heads',
  templateUrl: './feature-turning-heads.component.html',
  styleUrls: ['../features.scss']
})
export class FeatureTurningHeadsComponent implements OnDestroy, OnInit {

  @ViewChild(ExpansionPanelPagerDirective, { static: true })
  pager: ExpansionPanelPagerDirective;

  public instance: number = 0;

  public waitTime: number = 0;
  public playTime: number = 0;
  public playSound: boolean = false;
  public currentDirection: number = 0;
  
  constructor(private rpc: JsonRpcService, private snackBar: MatSnackBar, private logger: LoggerService) {
  }

  public abandon() {
    this.rpc.call<FeatureTurningHeadsStatus>('headsAbandon', { instance: this.instance })
      .subscribe((status) => this.logger.logMessage('headsAbandon result: ', status)); //this.update(status));
  }

  public change(value: number) {
    console.log('heads set current: ' + this.currentDirection + ', requested: ' + value);
    if (value !== this.currentDirection) {
      console.log('heads set changed!')
      this.rpc.call<FeatureTurningHeadsStatus>('headsOperate', { instance: this.instance, direction: value })
        .subscribe((status) => /* this.logger.logMessage('headsDirection result: ', status)); // */ this.update(status));
    }
  }

  public get direction(): number {
    console.log('heads get: ' + this.currentDirection);
    return this.currentDirection;
  }

  public pageClosed(page: string) {
    this.playSound = false;
    if (page == 'waiting') {
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
    this.rpc.call<FeatureTurningHeadsStatus>('headsReserve', { instance: this.instance })
      .subscribe((status) => this.logger.logMessage('headsReserve result: ', status));
  }

  public show(message: string) {
    this.snackBar.open(message, null, { duration: 3000, panelClass: 'center-snackbar', verticalPosition: 'bottom' });
  }

  private update(status: FeatureTurningHeadsStatus) {
    this.logger.logMessage('FeatureTurningHeads.update(' + status.status + ', ' + status.direction + ', ' + status.timer + ')');
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
    console.log('FeatureTurningHeads.ngOnDestroy()');
    this.requestSubscription.unsubscribe();
  }

  ngOnInit() {
    this.currentDirection = 0;
    this.requestSubscription = this.rpc.requestQueue.pipe(filter(request =>
      request.method == 'headsChanged'
    )).subscribe(request => {
      this.logger.logMessage('headsChanged', request);
      this.update(request.params as FeatureTurningHeadsStatus);
    });
  }
}