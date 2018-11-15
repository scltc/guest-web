import { Component, ContentChild, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material';

import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { JsonRpcService, LoggerService } from 'core';

import { ExpansionPanelPagerDirective } from "../expansion-panel-pager.directive";

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
export class FeatureTurningHeadsComponent implements OnDestroy {

  @ViewChild(ExpansionPanelPagerDirective)
  pager: ExpansionPanelPagerDirective;

  //@ContentChild('cowPager')
  /*
  thePager: ExpansionPanelPagerDirective;

  set pager(value: ExpansionPanelPagerDirective) {
    console.log('set pager');
    console.log(value == null)
    this.thePager = value;
  }
  */

  public instance: number = 0;

  public waitTime: number = 0;
  public playTime: number = 0;
  public playSound: boolean = false;
  public currentDirection: number = +1;

  private update(status: FeatureTurningHeadsStatus) {
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

  constructor(private rpc: JsonRpcService, private snackBar: MatSnackBar, private logger: LoggerService) {
  }

  public get direction(): number {
    return this.currentDirection;
  }

  public set direction(direction: number) {
    console.log("turn!");
    if (direction != this.currentDirection) {
      this.rpc.call<FeatureTurningHeadsStatus>('headsOperate', { instance: this.instance, direction: this.currentDirection })
        .subscribe((status) => this.update(status));
    }
  }

  public abandon() {
    this.rpc.call<FeatureTurningHeadsStatus>("headsAbandon", { instance: this.instance })
      .subscribe((status) => this.update(status));
  }

  public reserve() {
    this.rpc.call<FeatureTurningHeadsStatus>("headsReserve", { instance: this.instance })
      .subscribe((status) => this.logger.logMessage("headsReserve result: ", status));
  }

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 10;
    }
    else if (page == 'playing') {
      this.playTime = 20;
      this.playSound = true;
    }
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

  public show(message: string) {
    this.snackBar.open(message, null, { duration: 3000, panelClass: 'center-snackbar', verticalPosition: 'bottom' });
  }

  private requestSubscription: Subscription;

  ngOnDestroy() {
    console.log('FeaturesComponent ngOnDestroy()');
    this.requestSubscription.unsubscribe();
  }

  ngOnInit() {
    this.requestSubscription = this.rpc.requestQueue.pipe(filter(request =>
      request.method == 'headsChanged'
    )).subscribe(request => {
      this.logger.logMessage('headsChanged', request);
      this.update(request.params as FeatureTurningHeadsStatus);
    });
  }
}