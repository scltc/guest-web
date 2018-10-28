import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material';

import { FeatureTurningHeadsService } from './feature-turning-heads.service';

@Component({
  selector: 'app-feature-turning-heads',
  templateUrl: './feature-turning-heads.component.html',
  styleUrls: ['../features.scss']
})
export class FeatureTurningHeadsComponent implements OnInit {

  constructor(private controller: FeatureTurningHeadsService, public snackBar: MatSnackBar) {
  }

  public waitTime: number = 0;
  public playTime: number = 0;
  public playSound: boolean = false;

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 10;
    }
    else if (page == 'playing') {
      this.playTime = 20;
      this.playSound = true;
    }
  }

  public pageClosed(page: string) {
    this.playSound = false;
    if (page == 'waiting') {
      this.waitTime = 0;
    }
    else if (page == 'playing') {
      this.playTime = 0;
    }
  }

  public show(message: string) {
    this.snackBar.open(message, null, { duration: 3000, panelClass: 'center-snackbar', verticalPosition: 'bottom' });
  }

  currentDirection: number =  +1;

  public get direction(): number {
    return this.currentDirection;
  }

  public set direction(direction: number) {
    console.log("turn!");
    if (direction != this.currentDirection) {
      this.currentDirection = direction;
      this.controller.setHeadsDirection(0, this.currentDirection);
    }
  }

  ngOnInit() {
    console.log('FeaturesComponent ngOnInit()');
  }
}