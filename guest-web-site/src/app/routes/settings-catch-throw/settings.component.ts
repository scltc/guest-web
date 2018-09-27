/*
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

import { ControllerPortComponent } from 'shared/controller-port';

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
    providers: [
        ControllerPortComponent
    ]
})
export class SettingsComponent implements OnInit {

    settingForm = new FormGroup({
        enabled: new FormControl(''),
        minStopTime: new FormControl(''),
        maxStopTime: new FormControl(''),
        runningTime: new FormControl('')
    });

    constructor() {
    }

    ngOnInit() {
    }

    onSubmit() {
        // TODO: Use EventEmitter with form value
        console.warn(this.settingForm.value);
    }
}
*/