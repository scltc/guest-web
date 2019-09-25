import { Component } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-catch-and-throw-settings',
  templateUrl: './catch-and-throw-settings.component.html',
  styleUrls: ['../features.scss']
})
export class CatchAndThrowSettingsComponent {

  settingForm = new FormGroup({
    enabled: new FormControl(''),
    minStopTime: new FormControl(''),
    maxStopTime: new FormControl(''),
    runningTime: new FormControl('')
  });

}