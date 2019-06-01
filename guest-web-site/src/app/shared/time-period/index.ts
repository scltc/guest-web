import {
  Component,
  Input,
} from '@angular/core';

import { MatFormFieldControl } from '@angular/material/form-field';

export class TimePeriod {
  constructor(public value : string) {
  }
}

@Component({
  selector: 'app-time-period',
  templateUrl: './time-period.component.html',
  styleUrls: ['./time-period.component.scss'],
  providers: [{provide: MatFormFieldControl, useExisting: TimePeriodComponent}]
})
export class TimePeriodComponent {

  @Input() formControlName: string;
  @Input() placeholder: string;

  constructor() {
  }
}
