import { Component, EventEmitter, OnDestroy, OnInit, Input } from '@angular/core';
import { Observable, PartialObserver, Subject, interval, timer } from 'rxjs';
import { finalize, map, share, switchMap, take, takeWhile, tap } from 'rxjs/operators';

@Component({
  selector: 'app-turn-queue',
  templateUrl: './turn-queue.component.html',
  styleUrls: ['./features.scss']
})
export class TurnQueueComponent implements OnDestroy {

  private interval: number = 1000;
  private canceled: boolean = false;

  @Input('wait-time')
  time: number = 60000;

  @Input('observer')
  observer: PartialObserver<number> = null;

  public timer: Observable<number> = interval(this.interval).pipe(
    map(value => this.time - value * this.interval),
    takeWhile(value => !this.canceled && value >= 0),
    finalize(() => console.log('finalize: ' + this.canceled)),
    share()
  );

  onCancel() {
    this.canceled = true;
  }
  
  ngOnDestroy() {
    console.log('onDestroy');
  }

  /*
  public value = new Subject<number>();
  private counterSubject = new Subject<any>();
  public xtimer: Observable<number> = timer(0, this.interval).pipe(
    take(this.count),
    tap(() => this.value.next(--this.count * 1000))
  );
*/
  constructor() {
  }
}
