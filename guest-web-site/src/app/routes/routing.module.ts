import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    FlexLayoutModule
  } from '@angular/flex-layout';
  import {
    FormsModule,
    ReactiveFormsModule
  } from '@angular/forms';
  /*
  import {
    HttpModule
  } from '@angular/http';
  */
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldControl, MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Route, Router } from '@angular/router';
import { Routes, RouterModule } from '@angular/router';

import { AppInitializeService } from '../app-initialize.service';
import { AppModule } from '../app.module'

import { ButtonsModule } from 'shared/buttons/buttons.module';
import { ControllerPortComponent } from 'shared/controller-port';
import {
    FlipClockComponent,
    FlipClockDigitComponent,
    TimerDirective
} from 'shared/flip-clock';
import { TimePeriodComponent } from 'shared/time-period';

import { AwayComponent } from './away/away.component';
import { HomeComponent } from './home/home.component';
import {
    ExpansionPanelConcealerDirective,
    ExpansionPanelPageDirective,
    ExpansionPanelPagerDirective,
    FeatureCatchAndThrowComponent,
    FeaturePickATrainComponent,
    FeatureTurningHeadsComponent,
    FeaturesComponent,
    TurnQueueComponent
} from './features';
import { ScheduleComponent } from './schedule/schedule.component';
import { SettingsComponent } from './settings/settings.component';
import { TroubleshootingComponent } from './troubleshooting/troubleshooting.component';

// Must use only exported functions (no lambdas) in the routing table!

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
            title: 'Home',
            routeWhenConnected: '/home',
            showBackground: true,
            showMain: isInitialRouteAway,
            showSide: isInitialRouteAway
        }
    },
    {
        path: 'home',
        component: HomeComponent,
        data: {
            title: 'Home',
            routeWhenConnected: null,
            showBackground: true,
            showMain: isInitialRouteHome,
            showSide: isInitialRouteHome
        }
    },
    {
        path: 'features',
        component: FeaturesComponent,
        data: {
            title: 'Features',
            routeWhenConnected: null,
            showBackground: false,
            showMain: true,
            showSide: true
        }
    },
    {
        path: 'schedule',
        component: ScheduleComponent,
        data: {
            title: 'Schedule',
            routeWhenConnected: null,
            showBackground: false,
            showMain: false,
            showSide: false
        }
    },
    {
        path: 'about',
        component: ScheduleComponent,
        data: {
            title: 'About',
            routeWhenConnected: null,
            showBackground: false,
            showMain: true,
            showSide: true
        }
    },    
    {
        path: 'settings',
        component: SettingsComponent,
        data: {
            title: 'Settings',
            routeWhenConnected: null,
            showBackground: false,
            showMain: true,
            showSide: true
        }
    },
    {
        path: 'troubleshooting',
        component: TroubleshootingComponent,
        data: {
            title: 'Connect',
            routeWhenConnected: '/home',
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
        ButtonsModule,
        //   AppInitializeModule,
        // AppInitializeService,

        FormsModule,
        ReactiveFormsModule,
        // FlexLayoutModule,
        //    FontAwesomeModule,
//      HttpModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatExpansionModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        // MatListModule,
        MatMenuModule,
        MatSelectModule,
        MatSidenavModule,
        MatSnackBarModule,
        // MatToolbarModule,
        ReactiveFormsModule,
        RouterModule.forRoot(appRoutes, { useHash: true })
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