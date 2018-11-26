import { Component, OnInit } from '@angular/core';

import { FeatureCatchAndThrowService } from './feature-catch-and-throw.service';

@Component({
  selector: 'app-feature-catch-and-throw',
  templateUrl: './feature-catch-and-throw.component.html',
  styleUrls: ['../features.scss']
})
export class FeatureCatchAndThrowComponent implements OnInit {

  constructor(private controller : FeatureCatchAndThrowService) {
  }

  public waitTime: number = 0;
  public playTime: number = 0;
  public playSound: boolean = false;

  private currentDirection: number =  +1;
  
  private count: number = 0;

  public get direction(): number {
    return this.currentDirection;
  }

  public set direction(direction: number) {
    /*
    console.log("turn!");
    if (direction != this.currentDirection) {
      this.currentDirection = direction;
      this.count = (this.count + 1) % 4;
      this.controller.runCatchAndThrow(direction);
    }
    */
  }

  public get running(): boolean {
    // console.log('running: ' + (this.currentDirection != +1) + ', currentDirection: ' + this.currentDirection);
    return this.count > 1;
  }

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 2;
    }
    else if (page == 'playing') {
      this.playTime = 120;
      // this.playSound = true;
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

  ngOnInit() {
  }
}