import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { ButtonDirectionComponent } from './button-direction.component';
import { ButtonLetsDoThisComponent } from './button-lets-do-this.component';
import { ButtonNoThanksComponent } from './button-no-thanks.component';
import { ButtonWheelsComponent } from './button-wheels.component';

import { SpinnersModule } from '../spinners/spinners.module';

@NgModule({
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        SpinnersModule,
    ],
    exports: [
        ButtonDirectionComponent,
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent,
        ButtonWheelsComponent,
        MatIconModule
    ],
    declarations: [
        ButtonDirectionComponent,
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent,
        ButtonWheelsComponent
    ]
})
export class ButtonsModule {
}