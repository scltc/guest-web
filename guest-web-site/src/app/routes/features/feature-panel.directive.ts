import { AfterContentInit, AfterViewInit, ContentChildren, Directive, ElementRef, EventEmitter, Input, HostBinding, OnDestroy, OnInit, Output, QueryList, ViewChildren } from '@angular/core';

import { MatExpansionPanel } from '@angular/material/expansion';

import { FeaturesComponent } from './features.component';
import { FeaturePanelStepDirective } from './feature-panel-step.directive';

@Directive({
    //exportAs: 'feature-panel',
    selector: 'app-feature-panel, [app-feature-panel]'
})
export class FeaturePanelDirective implements AfterContentInit, AfterViewInit, OnInit, OnDestroy {

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

    constructor(private component: FeaturesComponent, private self: MatExpansionPanel, private element: ElementRef) {
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

    ngOnInit() {
        this.self.opened.subscribe(() => {
            this.component.onOpened(this.panel);
        });

        //this.self.expanded.

        this.self.closed.subscribe(() => {
            this.component.onClosed();
        })
    }

    ngOnDestroy() {

    }
}