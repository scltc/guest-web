import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FeatureTurningHeadsComponent } from './feature-turning-heads.component';

@Component({
    selector: 'app-feature-turning-heads-button',
    templateUrl: './feature-turning-heads-button.component.html',
    styleUrls: ['../features.scss']
})
export class FeatureTurningHeadsButtonComponent {

    public currentDirection: number = -1;
    public westArrowIcon: string = 'chevron_left';
    public eastArrowIcon: string = 'chevron_left';
    public transform: string = 'scale(1,1)';

    @Output()
    directionChange = new EventEmitter<number>();

    @Input('direction')
    get direction() {
        return this.currentDirection;
    }
    set direction(direction) {
        if (direction != this.currentDirection) {
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
            this.currentDirection = direction;
            this.directionChange.emit(direction);
        }
    }
}