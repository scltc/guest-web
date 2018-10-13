import { Component, Input, OnInit } from '@angular/core';

import { FlipClockComponent } from './flip-clock.component';

@Component({
  selector: 'flip-clock-digit',
  styleUrls: ['./flip-clock.scss'],
  templateUrl: './flip-clock-digit.component.html'
})
export class FlipClockDigitComponent implements OnInit {

  @Input() digit: (value: number) => number;

  public numbers: number[] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];

  private currentValue: number = 0;
  private previousValue: number = 0;

  constructor(private clock: FlipClockComponent) {
  }

  ngOnInit() {
    this.clock.timer.subscribe(value => {

      this.previousValue = this.currentValue;
      try {
        this.currentValue = this.digit(value);
      }
      catch
      {
        this.currentValue = 9;
      }
    });
  }

  isActive(number: number): boolean {
    return this.currentValue === number;
  }

  isBefore(number: number): boolean {
    return this.previousValue !== this.currentValue && number === this.previousValue;
  }

  isAnimated(): boolean {
    return this.previousValue !== this.currentValue;
  }
}
