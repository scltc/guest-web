import { Component, OnInit } from '@angular/core';

import { ControllerSocketService } from 'core';

@Component({
  selector: 'app-feature-pick-a-train',
  templateUrl: './feature-pick-a-train.component.html',
  styleUrls: ['../features.scss']
})
export class FeaturePickATrainComponent implements OnInit {

  constructor(private controller : ControllerSocketService) {
  }

  ngOnInit() {
  }
}