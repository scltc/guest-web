import { Component } from '@angular/core';

import { FeatureTurningHeadsComponent } from './feature-turning-heads.component';

@Component({
    selector: 'app-feature-turning-heads-button',
    templateUrl: './feature-turning-heads-button.component.html',
    styleUrls: ['./features.scss']
})
export class FeatureTurningHeadsButtonComponent {

    private direction: number = -1;
    private westArrowIcon: string = 'chevron_left';
    private eastArrowIcon: string = 'chevron_left';
    private transform: string = 'scale(1,1)';

    public constructor(private controller: FeatureTurningHeadsComponent) {
    }

    private setDirection(direction: number) {
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

    public onHeadDirectionToggle() {
        console.log('turn');
        this.controller.setHeadsDirection(0);
        this.setDirection(this.direction * -1);
    }
}