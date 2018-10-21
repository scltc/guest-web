import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-button-direction',
  templateUrl: './button-direction.component.html',
  styleUrls: ['./buttons.scss']
})
export class ButtonDirectionComponent {

  private currentDirection: number = -1;

  public get arrow(): string {
    return (this.currentDirection < 0) ? 'chevron_left' : 'chevron_right';
  }

  public get icon(): string {
    return (this.running) ? (this.currentDirection < 0 ? 'cached' :  'autorenew')
      : 'swap_horiz';
  }

  public get transform(): string {
    return (this.running) ? null
      : ((this.currentDirection < 0) ? 'scale(1,1)' : 'scale(-1,1)');
  };

  @Output()
  directionChange = new EventEmitter<number>();

  @Input()
  get direction() {
    return this.currentDirection;
  }
  set direction(direction) {
    if (direction != this.currentDirection) {
      console.log('changed');
      this.currentDirection = direction;
      this.directionChange.emit(direction);
    }
  }

  @Input()
  running: boolean = false;
}
