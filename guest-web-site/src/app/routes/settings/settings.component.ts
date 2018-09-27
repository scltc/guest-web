import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FormControl, FormGroup } from '@angular/forms';

import { ControllerPortComponent } from 'shared/controller-port';

/*
import { WebsocketService } from '../../websocket.service';
import { ChatService } from '../../chat.service';
*/

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
    providers: [
        ReactiveFormsModule
    ]
    /*
    providers: [WebsocketService, ChatService]
    */
})
export class SettingsComponent implements OnInit {

    settingForm = new FormGroup({
        enabled: new FormControl(''),
        minStopTime: new FormControl(''),
        maxStopTime: new FormControl(''),
        runningTime: new FormControl('')
    });

    constructor() {
        /*
                chatService.messages.subscribe(msg => {
                    console.log("Response from websocket: " + msg);
                });
        */
    }
    /*
    
        private message = {
            author: 'tutorialedge',
            message: 'this is a test message'
        }
    
        sendMsg() {
            console.log('new message from client to websocket: ', this.message);
            this.chatService.messages.next(this.message);
            this.message.message = '';
        }
    */
    ngOnInit() {
    }

    onSubmit() {
        // TODO: Use EventEmitter with form value
        console.warn(this.settingForm.value);
    }
}
