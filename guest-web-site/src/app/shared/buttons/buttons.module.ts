import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

import { ButtonLetsDoThisComponent } from './button-lets-do-this.component';
import { ButtonNoThanksComponent } from './button-no-thanks.component';

@NgModule({
    imports: [
        CommonModule,
        MatIconModule
    ],
    exports: [
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent,
        MatIconModule
    ],
    declarations: [
        ButtonLetsDoThisComponent,
        ButtonNoThanksComponent
    ],
})
export class ButtonsModule {
}