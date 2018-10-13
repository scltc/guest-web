import { AfterContentInit, Directive, ElementRef, Input, Output, EventEmitter } from '@angular/core';

import { ExpansionPanelPagerDirective } from './expansion-panel-pager.directive';

@Directive({
    //exportAs: 'feature-panel',
    selector: '[app-expansion-panel-page]'
})
export class ExpansionPanelPageDirective implements AfterContentInit {

    @Input('app-expansion-panel-page')
    name: number;

    constructor(/* private panel: FeaturePanelDirective, */ private element: ElementRef) {
        console.log('feature-panel-step: ' + this.element.nativeElement.tagName);
    }

    ngAfterContentInit() {
        console.log('feature-panel-step: ' + this.element.nativeElement.tagName + ', ' + this.name);
    }
}