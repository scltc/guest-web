import { Component, EventEmitter, Output } from '@angular/core';

@Component({
    selector: 'app-button-no-thanks',
    templateUrl: './button-no-thanks.component.html',
    styleUrls: ['./buttons.scss']
})
export class ButtonNoThanksComponent {

    @Output('click')
    click: EventEmitter<any> = new EventEmitter<any>();

    onClick(event: any) {
        this.click.emit(event);
    }
}