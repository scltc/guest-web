import {
  AfterContentInit,
  ContentChildren,
  Directive,
  ElementRef,
  EventEmitter,
  OnDestroy,
  QueryList,
  Renderer2
} from '@angular/core';

import { MatExpansionPanel } from '@angular/material';

import { Subscription, merge } from 'rxjs';

@Directive({
  // exportAs: 'appExclusiveOpen',
  selector: '[app-expansion-panel-concealer]'
})
/***
 * When used to decorate a <mat-accordion>, causes all other <mat-expansion-panel>
 * children to be hidden when a sibling is opened.  Useful for small screen devices.
 */
export class ExpansionPanelConcealerDirective implements AfterContentInit, OnDestroy {

  private elements: Array<ElementRef>;

  @ContentChildren(MatExpansionPanel, { read: ElementRef })
  private elementList: QueryList<ElementRef>;

  @ContentChildren(MatExpansionPanel)
  private panelList: QueryList<MatExpansionPanel>;

  constructor(private renderer: Renderer2) {
  }

  private closed() {
    this.panelList.forEach((panel, index) => {
      panel.expanded = false;
      this.renderer.setStyle(this.elements[index].nativeElement, 'display', 'block');
    });
  }

  private opened() {
    this.panelList.forEach((panel, index) => {
      this.renderer.setStyle(this.elements[index].nativeElement, 'display', (panel.expanded) ? 'block' : 'none');
    });
  }

  private closedSubscription: Subscription = null;
  private openedSubscription: Subscription = null;

  ngAfterContentInit() {

    if (this.panelList) {

      this.elements = this.elementList.toArray();

      let closedObservables = new Array<EventEmitter<void>>();
      let openedObservables = new Array<EventEmitter<void>>();
      this.panelList.forEach(panel => {
        closedObservables.push(panel.closed);
        openedObservables.push(panel.opened);
      });

      this.closedSubscription = merge(...closedObservables).subscribe(() => this.closed());
      this.openedSubscription = merge(...openedObservables).subscribe(() => this.opened());
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