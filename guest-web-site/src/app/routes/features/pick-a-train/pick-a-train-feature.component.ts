import { Component, OnInit } from '@angular/core';

import { ControllerSocketService } from 'core';

@Component({
  selector: 'app-pick-a-train-feature',
  templateUrl: './pick-a-train-feature.component.html',
  styleUrls: ['../features.scss']
})
export class PickATrainFeatureComponent implements OnInit {

  constructor(private controller : ControllerSocketService) {
  }

  ngOnInit() {
  }
}