// Original AngularJs version from:
// https://github.com/dmytroyarmak/angular-flip-clock
// Original jQuery version from:
// https://github.com/objectivehtml/FlipClock

import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'flip-clock',
  styleUrls: ['./flip-clock.scss'],
  templateUrl: './flip-clock.component.html'
})
export class FlipClockComponent {

  @Input('timer')
  timer: Observable<number>;

  constructor() {
  }

  private static MILISECONDS_IN_SECOND: number = 1000;
  private static MILISECONDS_IN_MINUTE: number = FlipClockComponent.MILISECONDS_IN_SECOND * 60;
  private static MILISECONDS_IN_HOUR: number = FlipClockComponent.MILISECONDS_IN_MINUTE * 60;
  private static MILISECONDS_IN_24_HOURS: number = FlipClockComponent.MILISECONDS_IN_HOUR * 24;

  public static getHours(value): number {
    return Math.floor((value % FlipClockComponent.MILISECONDS_IN_24_HOURS - FlipClockComponent.getMinutes(value)) / FlipClockComponent.MILISECONDS_IN_HOUR);
  }

  public static getMinutes(value): number {
    return Math.floor((value % FlipClockComponent.MILISECONDS_IN_HOUR - FlipClockComponent.getSeconds(value)) / FlipClockComponent.MILISECONDS_IN_MINUTE);
  }

  public static getSeconds(value): number {
    return Math.floor((value % FlipClockComponent.MILISECONDS_IN_MINUTE - FlipClockComponent.getMiliseconds(value)) / FlipClockComponent.MILISECONDS_IN_SECOND);
  }

  private static getMiliseconds(value): number {
    return Math.floor(value % FlipClockComponent.MILISECONDS_IN_SECOND);
  }

  private static getTensPlace(value): number {
    return (value % 100 - FlipClockComponent.getOnesPlace(value)) / 10;
  }

  private static getOnesPlace(value): number {
    return value % 10;
  }

  public getHoursTensPlace(value: number): number {
    return FlipClockComponent.getTensPlace(FlipClockComponent.getHours(value));
  }

  public getHoursOnesPlace(value: number): number {
    return FlipClockComponent.getOnesPlace(FlipClockComponent.getHours(value));
  }

  public getMinutesTensPlace(value: number): number {
    return FlipClockComponent.getTensPlace(FlipClockComponent.getMinutes(value));
  }

  public getMinutesOnesPlace(value: number): number {
    return FlipClockComponent.getOnesPlace(FlipClockComponent.getMinutes(value));
  }

  public getSecondsTensPlace(value: number): number {
    return FlipClockComponent.getTensPlace(FlipClockComponent.getSeconds(value));
  }

  public getSecondsOnesPlace(value: number): number {
    return FlipClockComponent.getOnesPlace(FlipClockComponent.getSeconds(value));
  }
}
