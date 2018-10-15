import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material';

import { ControllerService } from 'core';

@Component({
  selector: 'app-feature-turning-heads',
  templateUrl: './feature-turning-heads.component.html',
  styleUrls: ['./features.scss']
})
export class FeatureTurningHeadsComponent implements OnInit {

  constructor(private controller: ControllerService, public snackBar: MatSnackBar) {
  }

  public waitTime: number = 0;
  public playTime: number = 0;

  public pageOpened(page: string) {
    if (page == 'waiting') {
      this.waitTime = 15;
    }
    else if (page == 'playing') {
      this.playTime = 20;
    }
  }

  public pageClosed(page: string) {
    if (page == 'waiting') {
      this.waitTime = 0;
    }
    else if (page == 'playing') {
      this.playTime = 0;
    }
  }

  public show(message: string) {
    this.snackBar.open(message, null, { duration: 2000, panelClass: 'center', verticalPosition: 'top' });
  }

  setHeadsDirection(direction: number) {
    this.controller.setHeadsDirection(0, direction);
  }

  ngOnInit() {
    console.log('FeaturesComponent ngOnInit()');
  }
}