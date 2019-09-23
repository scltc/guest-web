import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { SpinnerTracksComponent } from './spinner-tracks.component';
import { SpinnerWheelComponent } from './spinner-wheel.component';

@NgModule({
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule
    ],
    exports: [
        SpinnerTracksComponent,
        SpinnerWheelComponent,
        MatIconModule
    ],
    declarations: [
        SpinnerTracksComponent,
        SpinnerWheelComponent
    ]
})
export class SpinnersModule {
}