// Original concept from:
// Angular 2 - Countdown timer
// https://stackoverflow.com/questions/44489580/angular-2-countdown-timer

// <ng-container [counter]="1200" [interval]="1000" (value)="value = $event">
//    <h3>{{ value }}</h3>
// </ng-container>

import { Directive, Input, Output, EventEmitter, OnChanges, OnDestroy } from '@angular/core';

import { Subject, Observable, SubscriptionLike, timer } from 'rxjs';
import { switchMap, take, takeWhile, tap, share, finalize } from 'rxjs/operators';

interface TimerDirectiveParameters {
  aborted: boolean;
  duration: number;
  period: number;
}

@Directive({
  exportAs: 'countdownTimer',
  selector: '[countdown-timer]'
})
export class TimerDirective implements OnChanges, OnDestroy {

  private parameters: TimerDirectiveParameters = null;
  private counterSubject = new Subject<any>();
  private countSubscription: SubscriptionLike;

  @Input('countdown-timer')
  counter: number = 0;
  @Input()
  interval: number = 1000;
  @Output()
  value = new EventEmitter<number>();
  @Output()
  complete = new EventEmitter<void>();

  /*
  constructor() {
    console.log("TimerDirective");
    this.countSubscription = this.counterSubject.pipe(
      switchMap((options: any) =>
        timer(0, options.interval).pipe(
          take(options.count),
          tap((value) => {
            console.log(value);
            this.value.emit((options.count - value) * options.interval)
          }),
          finalize(() => this.complete.emit()),
          share()
        )
      )
    ).subscribe();
  }
  */
  constructor() {
    console.log("TimerDirective");
    this.countSubscription = this.counterSubject.pipe(
      switchMap((options: TimerDirectiveParameters) =>
        timer(0, options.period).pipe(
          takeWhile(value => !options.aborted && value < options.duration),
          tap(value => {
            console.log(value);
            this.value.emit((options.duration - value) * options.period)
          }),
          finalize(() => {
            if (options.aborted) {
              this.value.emit(0);
            }
            else {
              this.complete.emit();
            }
          }),
          share()
        )
      )
    ).subscribe();
  }

  ngOnChanges() {
    console.log('ngOnChanges()');
    if (this.counter <= 0 || this.interval <= 0) {
      this.parameters && (this.parameters.aborted = true);
    }
    else {
      this.parameters = { aborted: false, duration: this.counter, period: this.interval }
      this.counterSubject.next(this.parameters);
    }
  }

  ngOnDestroy() {
    console.log('ngOnDestroy()');
    this.countSubscription.unsubscribe();
  }
}