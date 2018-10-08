import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, interval } from 'rxjs';
import { finalize, map, takeWhile } from 'rxjs/operators';

@Component({
  selector: 'app-turn-queue',
  templateUrl: './turn-queue.component.html',
  styleUrls: ['./features.scss']
})
export class TurnQueueComponent implements OnDestroy {

  @Input('timer')
  timer : Observable<number>;

  @Output('canceled')
  canceled = new EventEmitter<any>();

  @Output('complete')
  complete = new EventEmitter<any>();

  cancel() {
    this.canceled.emit(null);
  }

  ngOnDestroy() {
    console.log('onDestroy');
  }

  constructor() {
  }
}
