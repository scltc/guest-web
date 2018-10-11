import { AfterContentInit, Directive, ElementRef, Input, Output, EventEmitter } from '@angular/core';

import { FeaturePanelDirective } from './feature-panel.directive';

@Directive({
    //exportAs: 'feature-panel',
    selector: 'appFeaturePanelStep, [appFeaturePanelStep]'
})
export class FeaturePanelStepDirective implements AfterContentInit {

    @Input('appFeaturePanelStep')
    step: number;

    constructor(private element: ElementRef) {
        console.log('feature-panel-step: ' + this.element.nativeElement.tagName);
    }

    ngAfterContentInit() {
        console.log('feature-panel-step: ' + this.element.nativeElement.tagName + ', ' + this.step);
    }
}