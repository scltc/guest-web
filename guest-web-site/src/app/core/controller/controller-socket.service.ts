// Original ideas from:
//   Using WebSockets in Angular with RxJs WebSocketSubject


import { EventEmitter, Injectable, OnDestroy, Inject } from '@angular/core';

import { WebSocketSubject, WebSocketSubjectConfig } from 'rxjs/websocket';

import { LoggerService } from '../logger.service';

/*
export class CatchAndThrowSettings {
    public westMinIdle: number = 0;
    public westMaxIdle: number = 0;

    public mainRunTime: number = 0;

    public eastMinIdle: number = 0;
    public eastMaxIdle: number = 0;
}

export class HeadTurnerSettings {
    public controller: string = null;
    public port: string = null;
    public leftDutyCycle: number = 0;
    public rightDutyCycle: number = 0;
    public motorRunTime: number = 0;
}

export class ControllerSettings {

    public catchAndThrow: CatchAndThrowSettings[];
    public headTurner: HeadTurnerSettings[];
}

public processSettings(settings: ControllerSettings) {
    this.logger.logMessage('settings: ', settings);
}
*/

export class MultiEndpointMessage {
    constructor(public endpoint: number, public payload: string) { }
}

@Injectable({
    providedIn: 'root'
})
export class ControllerSocketService implements OnDestroy {

    /* This can give access to the raw WebSocket.
    private socket : WebSocket;

    private HookedWebSocket = function(url: string, protocols?: string | string[]) {
        return (this.socket = new WebSocket(url,protocols));
    } as any;
    */

    public connected = new EventEmitter<boolean>();

    public webSocketSubject: WebSocketSubject<MultiEndpointMessage>;

    constructor(@Inject('string') private url: string, private logger: LoggerService) {

        this.logger.logMessage('ControllerService.constructor(' + url + ')');

        let configuration: WebSocketSubjectConfig<MultiEndpointMessage> = {
            url: url,
            closeObserver: {
                next: (event: CloseEvent) => {
                    logger.logError('WebSocket disconnected.', event);
                    this.connected.next(false);
                }
            },
            openObserver: {
                next: (event: Event) => {
                    logger.logMessage('WebSocket connected!');
                    this.connected.next(true);
                }
            },
            deserializer: (event: MessageEvent) => {
                console.log('received: ' + event.data);
                return new MultiEndpointMessage(parseInt(event.data.substring(0, event.data.indexOf(':'))),
                    event.data.substring(event.data.indexOf(':') + 1));
            },
            serializer: (message: MultiEndpointMessage) => {
                let serialized = message.endpoint + ':' + message.payload;
                console.log('sending: ' + serialized);
                return serialized;
            },
            // Uncomment this and the code above to gain access to the raw WebSocket.
            // WebSocketCtor: this.HookedWebSocket
        };

        this.webSocketSubject = new WebSocketSubject<MultiEndpointMessage>(configuration);
    }

    ngOnDestroy() {
        this.webSocketSubject.unsubscribe();
    }
}
