import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

import { SettingsDefaults } from './catch-and-throw.model';

@Component({
  selector: 'app-catch-and-throw-settings',
  templateUrl: './catch-and-throw-settings.component.html',
  styleUrls: ['../features.scss']
})
export class CatchAndThrowSettingsComponent implements OnInit {

  settingForm = new FormGroup({
    enabled: new FormControl(''),

    controller: new FormControl(''),

    westPort: new FormControl(''),
    westMinIdle: new FormControl(''),
    westMaxIdle: new FormControl(''),

    mainPort: new FormControl(''),
    mainRunTime: new FormControl(''),

    eastPort: new FormControl(''),
    eastMinIdle: new FormControl(''),
    eastMaxIdle: new FormControl('')

  });

  onSubmit() {
  }

  ngOnInit() {
    this.settingForm.setValue(SettingsDefaults);
  }
}