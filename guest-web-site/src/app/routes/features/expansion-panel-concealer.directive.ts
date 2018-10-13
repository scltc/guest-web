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

  elements: Array<ElementRef>;

  @ContentChildren(MatExpansionPanel, { read: ElementRef })
  elementList: QueryList<ElementRef>;

  @ContentChildren(MatExpansionPanel)
  panels: QueryList<MatExpansionPanel>;

  constructor(private renderer: Renderer2) {
  }

  closed() {
    this.panels.forEach((panel, index) => {
      panel.expanded = false;
      this.renderer.setStyle(this.elements[index].nativeElement, 'display', 'block');
    });
  }

  opened() {
    this.panels.forEach((panel, index) => {
      this.renderer.setStyle(this.elements[index].nativeElement, 'display', (panel.expanded) ? 'block' : 'none');
    });
  }

  closedSubscription: Subscription = null;
  openedSubscription: Subscription = null;

  ngAfterContentInit() {

    if (this.panels) {

      this.elements = this.elementList.toArray();

      let closedObservables = new Array<EventEmitter<void>>();
      let openedObservables = new Array<EventEmitter<void>>();
      this.panels.forEach(panel => {
        closedObservables.push(panel.closed);
        openedObservables.push(panel.opened);
      });

      this.closedSubscription = merge(...closedObservables).subscribe(() => this.closed());
      this.openedSubscription = merge(...openedObservables).subscribe(() => this.opened());
    }

    if (this.elements) {
      console.log(this.elements.length);
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