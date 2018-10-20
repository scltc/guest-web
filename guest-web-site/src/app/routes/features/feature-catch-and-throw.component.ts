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
  public playSound: boolean = false;

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 60;
    }
    else if (page == 'playing') {
      this.playTime = 30;
      this.playSound = true;
    }
  }

  public pageClosed(page: string) {
    this.playSound = false;
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