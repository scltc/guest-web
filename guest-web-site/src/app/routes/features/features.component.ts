import { AfterContentInit, AfterViewInit, Component, ContentChildren, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MatExpansionPanel } from '@angular/material/expansion';

// import { FeaturePanelDirective } from './feature-panel.directive';

@Component({
  selector: 'app-features',
  templateUrl: './features.component.html',
  styleUrls: ['./features.scss']
})
export class FeaturesComponent implements AfterViewInit {

  @ViewChildren(MatExpansionPanel)
  panels: QueryList<MatExpansionPanel>;
/*
  display: string[] = [
    "block",
    "block",
    "block"
  ];

  expanded: boolean[] = [
    false,
    false,
    false
  ]

  onOpened(index: number) {
    console.log('opened');

    let panels:MatExpansionPanel[]  = this.panels.toArray();

    for (let i: number = 0; i < this.display.length; ++i) {
      this.display[i] = (i != index) ? "none" : "block";
      this.expanded[i] = (i == index);
    }
  }

  onClosed() {
    console.log('closed');

    for (let i: number = 0; i < this.display.length; ++i) {
      this.display[i] = "block";
      this.expanded[i] = false;
    }
  }

  constructor() {
    this.onClosed();
  }
  */

  ngAfterViewInit() {
    console.log('ngAfterViewInit(): ' + this.panels.length)
  }
}