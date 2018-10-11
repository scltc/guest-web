import { AfterContentInit, AfterViewInit, ContentChildren, Directive, ElementRef, EventEmitter, Input, HostBinding, Output, QueryList, ViewChildren } from '@angular/core';

import { FeaturesComponent } from './features.component';
import { FeaturePanelStepDirective } from './feature-panel-step.directive';

@Directive({
    //exportAs: 'feature-panel',
    selector: 'app-feature-panel, [app-feature-panel]'
})
export class FeaturePanelDirective implements AfterContentInit, AfterViewInit {

    @ContentChildren(FeaturePanelStepDirective/*'appFeaturePanelStep'*//*FeaturePanelStepDirective*/, { descendants: true })
    steps: QueryList<FeaturePanelStepDirective>;

    @ViewChildren(FeaturePanelStepDirective/*'appFeaturePaneStep'*/)
    viewSteps: QueryList<FeaturePanelStepDirective>;

    @Input("app-feature-panel")
    panel : number;

    @HostBinding('style.display')
    get displayStyle() {
        return this.component.display[this.panel];
    }

    constructor(private component: FeaturesComponent, private element: ElementRef) {
        console.log('feature-panel: ' + this.element.nativeElement.tagName);
    }

    ngAfterContentInit() {
        if (this.steps) {
            console.log('feature-panel: ' + this.steps.length);
        }
    }

    ngAfterViewInit() {
        if (this.viewSteps) {
            console.log('featue-panel: ' + this.viewSteps.length);
        }
    }
}