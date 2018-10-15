import { Component, OnInit } from '@angular/core';

import { ControllerService } from 'core';

@Component({
  selector: 'app-feature-catch-and-throw',
  templateUrl: './feature-catch-and-throw.component.html',
  styleUrls: ['./features.scss']
})
export class FeatureCatchAndThrowComponent implements OnInit {

  constructor(private controller : ControllerService) {
  }

  public waitTime: number = 0;
  public playTime: number = 0;

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 20;
    }
    else if (page == 'playing') {
      this.playTime = 30;
    }
  }

  public pageClosed(page: string) {
    if (page == "waiting") {
      this.waitTime = 0;
    }
    else if (page == 'playing') {
      this.playTime = 0;
    }
  }

  onCatchAndThrowGo() {
    console.log('go');

    this.controller.runCatchAndThrow(0);
  }

  ngOnInit() {
  }
}