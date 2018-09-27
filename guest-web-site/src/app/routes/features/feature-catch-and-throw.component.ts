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

  onCatchAndThrowGo() {
    console.log('go');

    this.controller.runCatchAndThrow(0);
  }

  ngOnInit() {
  }
}