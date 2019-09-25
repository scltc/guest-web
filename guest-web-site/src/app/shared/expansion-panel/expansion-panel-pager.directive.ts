import { AfterContentInit, AfterViewInit, ContentChildren, Directive, ElementRef, EventEmitter, HostBinding, Input, OnDestroy, Output, ViewChildren, QueryList, Renderer2 } from '@angular/core';

import { MatExpansionPanel } from '@angular/material/expansion';

import { Subscription } from 'rxjs';

import { ExpansionPanelPageDirective } from './expansion-panel-page.directive';

@Directive({
    exportAs: 'appExpansionPanelPager',
    selector: '[app-expansion-panel-pager]'
})
export class ExpansionPanelPagerDirective implements AfterContentInit, AfterViewInit, OnDestroy {

    @Input('app-expansion-panel-pager')
    private initialPage: string = null;

    @Output('page-closed')
    private pageClosed: EventEmitter<string> = new EventEmitter<string>();

    @Output('page-opened')
    private pageOpened: EventEmitter<string> = new EventEmitter<string>();

    @ContentChildren(ExpansionPanelPageDirective, { read: ElementRef })
    private elementList: QueryList<ElementRef>;

    @ContentChildren(ExpansionPanelPageDirective)
    private pageList: QueryList<ExpansionPanelPageDirective>;

    private currentPage: string = null;

    private elements: Array<ElementRef> = new Array<ElementRef>();

    private closedPanelSubscription: Subscription = null;
    private openedPanelSubscription: Subscription = null;

    constructor(private panel: MatExpansionPanel, private renderer: Renderer2) {
    }

    public close() {
        this.display(null);
        this.panel.close();
        // this.panel.toggle();
    }

    public display(page: string) {

        console.log(page + '/' + this.currentPage);

        if (page != this.currentPage) {

            this.pageList.forEach((page, index) => {
                this.renderer.setStyle(this.elements[index].nativeElement, 'display', 'none');
                if (page.name == this.currentPage) {
                    page.pageClosed.emit(this.currentPage);
                    this.pageClosed.emit(this.currentPage);
                }
            });

            this.currentPage = page;

            this.pageList.forEach((page, index) => {
                if (page.name == this.currentPage) {
                    page.pageOpened.emit(this.currentPage);
                    this.pageOpened.emit(this.currentPage);
                    this.renderer.setStyle(this.elements[index].nativeElement, 'display', 'block');
                }
            });
        }
    }

    ngAfterViewInit() {
        if (this.panel && this.pageList) {

            this.elements = this.elementList.toArray();

            this.closedPanelSubscription = this.panel.closed.subscribe(() =>
                this.display(null)
            );

            this.openedPanelSubscription = this.panel.opened.subscribe(() =>
                this.display(this.initialPage || this.pageList.first.name)
            );
        }
    }

    ngAfterContentInit() {
        this.ngAfterViewInit();
    }

    ngOnDestroy() {
        this.closedPanelSubscription && this.closedPanelSubscription.unsubscribe();
        this.openedPanelSubscription && this.openedPanelSubscription.unsubscribe();
    }
}