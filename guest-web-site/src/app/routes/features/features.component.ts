import { Component, OnInit } from '@angular/core';

import { ControllerService } from 'core';

@Component({
  selector: 'app-features',
  templateUrl: './features.component.html',
  styleUrls: ['./features.scss']
})
export class FeaturesComponent implements OnInit {

  direction : number = -1;
  westArrowIcon : string = 'chevron_left';
  eastArrowIcon : string = 'chevron_left';
  transform : string = 'scale(1,1)';

  setDirection(direction : number) {
      this.direction = direction;
      if (direction < 0) {
        this.westArrowIcon = 'chevron_left';
        this.eastArrowIcon = 'chevron_left';
        this.transform = 'scale(1,1)';
      }
      else {
        this.westArrowIcon = 'chevron_right';
        this.eastArrowIcon = 'chevron_right';
        this.transform = 'scale(-1,1)';
      }
  }

  constructor(private controller : ControllerService) {
    this.onClosed(-1);
  }

  display : string[] = [
    "block",
    "block",
    "block"
  ];
      
  onOpened(index : number) {
    console.log('opened');

    for (let i : number = 0; i < this.display.length; ++i) {
      this.display[i] = (i != index) ? "none" : "block";
    }
  }

  onClosed(index : number) {
    console.log('closed');

    for (let i : number = 0; i < this.display.length; ++i) {
      this.display[i] = "block";
    }
  }


  onHeadDirectionToggle() {
    console.log('turn');
    this.controller.setHeadsDirection(0, 0);
    this.setDirection(this.direction * -1);
  }

  onCatchAndThrowGo() {
    console.log('go');

    this.controller.runCatchAndThrow(0);
  }

  ngOnInit() {
    console.log('FeaturesComponent ngOnInit()');
  }
}