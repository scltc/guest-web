import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule, MatIconModule } from '@angular/material'

import { ButtonLetsDoThisComponent } from './button-lets-do-this.component';
import { ButtonNoThanksComponent } from './button-no-thanks.component';
import { ButtonDirectionComponent } from './button-direction.component';

@NgModule({
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule
    ],
    exports: [
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent,
        ButtonDirectionComponent,
        MatIconModule
    ],
    declarations: [
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent,
        ButtonDirectionComponent
    ],
})
export class ButtonsModule {
}