import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

import { ButtonLetsDoThisComponent } from './button-lets-do-this.component';
import { ButtonNoThanksComponent } from './button-no-thanks.component';
import { ButtonDirectionComponent } from './button-direction.component';

@NgModule({
    imports: [
        CommonModule,
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