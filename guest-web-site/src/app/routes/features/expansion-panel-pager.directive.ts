import { AfterViewInit, Directive, OnDestroy, ViewChildren, QueryList } from '@angular/core';

import { MatExpansionPanel } from '@angular/material';

import { Subscription } from 'rxjs';

import { ExpansionPanelPageDirective } from './expansion-panel-page.directive';

@Directive({
    exportAs: 'appExpansionPanelPager',
    selector: '[app-expansion-panel-pager]'
})
export class ExpansionPanelPagerDirective implements AfterViewInit, OnDestroy {

    @ViewChildren('app-extension-panel-page')
    pages: QueryList<ExpansionPanelPageDirective>

    closedSubscription : Subscription = null;
    openedSubscription : Subscription = null;

    constructor (private panel: MatExpansionPanel) {
        if (this.panel) {
            this.closedSubscription = panel.closed.subscribe(() =>
                console.log('FeatureStepperDirective : Closed')
            );
            this.openedSubscription = panel.opened.subscribe(() =>
                console.log('FeatureStepperDirective : Opened')
            );
        }
    }

    close(): void {
        this.panel.close();
    }

    display(page: string): void {
    }

    ngAfterViewInit() {
        if (this.pages) {
            console.log(this.pages.length);
        }
    }

    ngOnDestroy() {
        if (this.closedSubscription) {
            this.closedSubscription.unsubscribe();
        }
        if (this.openedSubscription) {
            this.openedSubscription.unsubscribe();
        }
    }
}