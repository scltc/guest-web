import {
    APP_INITIALIZER,
    NgModule
} from '@angular/core';
import {
    BrowserModule
} from '@angular/platform-browser';
import {
    FlexLayoutModule
} from '@angular/flex-layout';
import {
    MatBottomSheetModule,
    MatButtonModule,
    MatListModule,
    MatSidenavModule,
    MatTabsModule,
    MatToolbarModule
} from '@angular/material';
import {
    BrowserAnimationsModule
} from '@angular/platform-browser/animations';

// import { APP_BASE_HREF } from '@angular/common';
// import { OverlayContainer } from '@angular/cdk/overlay';

// Application root
import { AppComponent } from './app.component';
import { AppConnectionLostComponent } from './app-connection-lost.component';

// Application routing
import { AppRoutingModule } from './routes/routing.module';
import { AppInitializeService } from './app-initialize.service';

// Application controller communication services
import { ControllerModule } from 'core';

@NgModule({
    declarations: [
        AppComponent,
        AppConnectionLostComponent,
    ],
    entryComponents: [
        AppConnectionLostComponent,
    ],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FlexLayoutModule,
        MatBottomSheetModule,
        MatButtonModule,
        MatListModule,
        MatSidenavModule,
        MatTabsModule,
        MatToolbarModule,

        // If we are running on 'localhost', we are debugging. Use the web socket server from there too.
        ControllerModule.forRoot('wss://home.scltc.club'),
        // ControllerModule.forRoot('ws://localhost'),
        // ControllerModule.forRoot({ url: 'ws://192.168.2.201', reconnectAttempts: -1 }),
        // This *MUST* be last or some of the imports above may fail!
        AppRoutingModule,
    ],
    providers: [
        { provide: APP_INITIALIZER, useFactory: AppInitializeService.init_app, deps: [AppInitializeService], multi: true },
        // feat(SnackBar): allow to define a container in which to render the snackbar
        // https://github.com/angular/material2/issues/7764
        // https://material.angular.io/cdk/overlay/overview
        // { provide: OverlayContainer, useFactory: (): AppOverlayContainer => new AppOverlayContainer() }
        // { provide: APP_BASE_HREF, useValue: '/settings' } // AppRoutingModule.Routes[0] }
        // { provide: APP_BASE_HREF, useFactory: AppInitializeService.init_app, deps: [AppInitializeService], useValue: "/", multi: true },

        /*
        { provide: APP_INITIALIZER, useFactory: AppInitializeService.factory, multi: true, deps: [
    
        ]}
        */
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {
}