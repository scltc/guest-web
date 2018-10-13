import { Component, HostListener, OnInit, Output, EventEmitter } from '@angular/core';
import { Observable, Observer, PartialObserver, interval } from 'rxjs';
import { finalize, map, takeWhile } from 'rxjs/operators';

import { ControllerService } from 'core';

@Component({
  selector: 'app-feature-turning-heads',
  templateUrl: './feature-turning-heads.component.html',
  styleUrls: ['./features.scss']
})
export class FeatureTurningHeadsComponent implements OnInit {

  constructor(private controller: ControllerService) {
    this.display('introduction');
  }

  @Output('complete')
  complete = new EventEmitter<any>();

  displayIntroduction: string;
  displayWaiting: string;
  displayActive: string;

  cancel() {
    this.complete.emit(null);
  }

  display(page: string) {
    this.displayIntroduction
      = (page == 'introduction') ? 'block' : 'none';
    this.displayWaiting
      = (page == 'waiting') ? 'block' : 'none';
    this.displayActive
      = (page == 'active') ? 'block' : 'none';
  }
  
  private interval: number = 1000;
  private isCanceled: boolean = false;

  // @Input('wait-time')
  time: number = 60000;
  
  public waitTimer: Observable<number> = interval(this.interval).pipe(
    map(value => this.time - value * this.interval),
    takeWhile(value => !this.isCanceled && value >= 0),
    finalize(() => {
      console.log('finalize');
      // if (this.isCanceled) this.canceled.emit(null); else this.complete.emit(null);
    })
  );

  setHeadsDirection(direction: number) {
    this.controller.setHeadsDirection(0, direction);
  }

  ngOnInit() {
    console.log('FeaturesComponent ngOnInit()');
  }
}