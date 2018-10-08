import { AfterContentInit, ContentChildren, Directive, ElementRef, EventEmitter, Input, Output, QueryList } from '@angular/core';

import { FeaturePanelStepDirective } from './feature-panel-step.directive';

@Directive({
    //exportAs: 'feature-panel',
    selector: 'app-feature-panel, [app-feature-panel]'
})
export class FeaturePanelDirective implements AfterContentInit {

    @ContentChildren(FeaturePanelStepDirective)
    steps: QueryList<FeaturePanelDirective>;
   // @Input('feature-id')
   // id : number;

    constructor(private element: ElementRef) {
        console.log('feature-panel-step: ' + this.element.nativeElement.tagName);
    }

    ngAfterContentInit() {
        console.log('feature-panel-step: ' + this.steps.length);
    }
}