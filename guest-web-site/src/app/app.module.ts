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
  MatButtonModule,
  MatListModule,
  MatSidenavModule,
  MatToolbarModule
} from '@angular/material';
import {
  BrowserAnimationsModule
} from '@angular/platform-browser/animations';

// import { APP_BASE_HREF } from '@angular/common';
// import { OverlayContainer } from '@angular/cdk/overlay';

// Application root
import { AppComponent } from './app.component';

// Application routing
import { AppRoutingModule } from './routes/routing.module';
import { AppInitializeService } from './app-initialize.service';

// Application controller communication services
import { ControllerModule } from './core/controller/controller.module';

@NgModule({
  declarations: [
    AppComponent,
    // ControllerPortComponent,
    // TimePeriodComponent
  ],
  imports: [
    // AppInitializeModule,
    // AppInitializeService,
    BrowserAnimationsModule,
    BrowserModule,
    FlexLayoutModule,
    MatButtonModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,

    // If we are running on 'localhost', we are debugging. Use the web socket server from there too.
    ControllerModule.forRoot(/* (window.location.hostname.toLowerCase() === 'localhost') ? 'ws://localhost' : */ 'wss://home.scltc.club'), //'ws://localhost'),
    //  ControllerModule.forRoot({ url: 'ws://192.168.2.201', reconnectAttempts: -1 }),

    // This must be last or some of the imports above may fail!
    AppRoutingModule,
  ],
  providers: [
    AppInitializeService,
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