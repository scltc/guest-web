import { Directive, Input } from '@angular/core';

@Directive({
    selector: '[app-expansion-panel-page]'
})
export class ExpansionPanelPageDirective {

    @Input('app-expansion-panel-page')
    name: string;

    constructor() {
    }
}