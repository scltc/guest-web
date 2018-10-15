import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import {
    MatButtonModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatFormFieldControl,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
} from '@angular/material';

import { AppInitializeService } from './app-initialize.service';

import { AwayComponent } from './routes/away/away.component';
import { HomeComponent } from './routes/home/home.component';

import {
    ExpansionPanelConcealerDirective,
    ExpansionPanelPageDirective,
    ExpansionPanelPagerDirective,
    FeatureCatchAndThrowComponent,
    FeaturePickATrainComponent,
    FeatureTurningHeadsComponent,
    FeatureTurningHeadsButtonComponent,
    FeaturesComponent,
} from './routes/features';

import { ScheduleComponent } from './routes/schedule/schedule.component';
import { SettingsComponent } from './routes/settings/settings.component';
import { TroubleshootingComponent } from './routes/troubleshooting/troubleshooting.component';

import { ControllerPortComponent } from 'shared/controller-port';


import { ButtonsModule } from './shared/buttons/buttons.module';
import {
    FlipClockComponent,
    FlipClockDigitComponent,
    TimerDirective
} from 'shared/flip-clock';
import { TimePeriodComponent } from 'shared/time-period';
import { TurnQueueComponent } from './routes/features/turn-queue.component';

// Must use only exported functions in the routing table!

export function isInitialRouteAway(): boolean {
    return AppInitializeService.isInitialRouteAway();
}

export function isInitialRouteHome(): boolean {
    return AppInitializeService.isInitialRouteHome();
}

export const appRoutes: Routes = [
    {
        path: 'away',
        component: AwayComponent,
        data: {
            title: "Home",
            showBackground: true,
            showMain: isInitialRouteAway,
            showSide: isInitialRouteAway
        }
    },
    {
        path: 'home',
        component: HomeComponent,
        data: {
            title: "Home",
            showBackground: true,
            showMain: isInitialRouteHome,
            showSide: isInitialRouteHome
        }
    },
    {
        path: 'features',
        component: FeaturesComponent,
        data: {
            title: "Features",
            showBackground: false,
            showMain: true,
            showSide: true
        }
    },
    {
        path: 'schedule',
        component: ScheduleComponent,
        data: {
            title: "Schedule",
            showBackground: false,
            showMain: false,
            showSide: false
        }
    },
    {
        path: 'settings',
        component: SettingsComponent,
        data: {
            title: "Settings",
            showBackground: false,
            showMain: true,
            showSide: true
        }
    },
    {
        path: 'troubleshooting',
        component: TroubleshootingComponent,
        data: {
            title: "Settings",
            showBackground: false,
            showMain: false,
            showSide: false
        }
    },
    {
        path: '**',
        redirectTo: 'home',
    }
];

@NgModule({
    imports: [
        CommonModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatExpansionModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        // AppInitializeService,
        ReactiveFormsModule,
        RouterModule.forRoot(appRoutes, { useHash: true }),
        ButtonsModule
    ],
    exports: [
        ButtonsModule,
        RouterModule
        // FeaturePanelDirective,
        // FeaturePanelStepDirective
    ],
    declarations: [
        AwayComponent,
        HomeComponent,
        ExpansionPanelConcealerDirective,
        ExpansionPanelPagerDirective,
        ExpansionPanelPageDirective,
        FeaturesComponent,
        FeatureCatchAndThrowComponent,
        FeaturePickATrainComponent,
        FeatureTurningHeadsComponent,
        FeatureTurningHeadsButtonComponent,
        ScheduleComponent,
        SettingsComponent,
        TroubleshootingComponent,
        ControllerPortComponent,
        FlipClockComponent,
        FlipClockDigitComponent,
        TimerDirective,
        TimePeriodComponent,
        TurnQueueComponent
    ]
})
export class AppRoutingModule {
    public static Routes: Routes = appRoutes;
}