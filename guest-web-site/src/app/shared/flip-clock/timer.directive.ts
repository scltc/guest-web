// Original concept from:
// Angular 2 - Countdown timer
// https://stackoverflow.com/questions/44489580/angular-2-countdown-timer

// <ng-container [counter]="1200" [interval]="1000" (value)="value = $event">
//    <h3>{{ value }}</h3>
// </ng-container>

import { Directive, Input, Output, EventEmitter, OnChanges, OnDestroy } from '@angular/core';

import { Subject, Observable, SubscriptionLike, timer } from 'rxjs';
import { switchMap, take, tap } from 'rxjs/operators';

@Directive({
  selector: '[counter]'
})
export class TimerDirective implements OnChanges, OnDestroy {

  private counterSubject = new Subject<any>();
  private countSubscription: SubscriptionLike;

  @Input() counter: number;
  @Input() interval: number;
  @Output() value = new EventEmitter<number>();

  constructor() {
    console.log("TimerDirective");
    this.countSubscription = this.counterSubject.pipe(
      switchMap((options: any) =>
        timer(0, options.interval).pipe(
          take(options.count),
          tap(() => this.value.emit(--options.count))
        )
      )
    ).subscribe();
  }

  ngOnChanges() {
    console.log('ngOnChanges()');
    this.counterSubject.next({ count: this.counter, interval: this.interval });
  }

  ngOnDestroy() {
    console.log('ngOnDestroy()');
    this.countSubscription.unsubscribe();
  }
}