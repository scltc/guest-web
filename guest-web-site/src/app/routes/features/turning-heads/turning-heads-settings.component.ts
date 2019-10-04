import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

import { SettingsDefaults } from './turning-heads.model';

@Component({
    selector: 'app-turning-heads-settings',
    templateUrl: './turning-heads-settings.component.html',
    styleUrls: ['../features.scss']
})
export class TurningHeadsSettingsComponent implements OnInit {

    settingForm = new FormGroup({

        enabled: new FormControl(''),

        controller: new FormControl(''),

        port: new FormControl(''),
        leftDutyCycle: new FormControl(''),
        rightDutyCycle: new FormControl(''),
        motorRunTime: new FormControl('')
    });

    onSubmit() {
    }

    ngOnInit() {
        this.settingForm.setValue(SettingsDefaults);
    }
}