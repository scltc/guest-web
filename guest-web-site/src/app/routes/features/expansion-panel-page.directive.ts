import { Directive, EventEmitter, Input, Output } from '@angular/core';

@Directive({
    selector: '[app-expansion-panel-page]'
})
export class ExpansionPanelPageDirective {

    @Input('app-expansion-panel-page')
    name: string;
    
    @Output('page-closed')
    pageClosed: EventEmitter<string> = new EventEmitter<string>();

    @Output('page-opened')
    pageOpened: EventEmitter<string> = new EventEmitter<string>();

    constructor() {
    }
}