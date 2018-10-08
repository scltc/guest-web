import { AfterContentInit, Directive, ElementRef, Input, Output, EventEmitter } from '@angular/core';

@Directive({
    //exportAs: 'feature-panel',
    selector: 'app-feature-panel-step, [app-feature-panel-step]'
})
export class FeaturePanelStepDirective implements AfterContentInit {

    @Input('app-feature-panel-step')
    step: number;

    constructor(private element: ElementRef) {
        console.log('feature-step: ' + this.element.nativeElement.tagName);
    }

    ngAfterContentInit() {
        console.log('feature-step: ' + this.element.nativeElement.tagName + ', ' + this.step);
    }
}