import { NgModule, APP_INITIALIZER } from '@angular/core';
import { Route, Router } from "@angular/router";
import { APP_BASE_HREF } from '@angular/common';

import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FlexLayoutModule } from '@angular/flex-layout';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { HttpModule } from '@angular/http';
import {
  MatButtonModule,
  MatCardModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatToolbarModule,
} from '@angular/material';

//import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
//import { library } from '@fortawesome/fontawesome-svg-core';
//import { faBars } from '@fortawesome/free-solid-svg-icons';

// Add an icon to the library for convenient access in other components
//library.add(faBars);

// Application root
import { AppComponent } from './app.component';
//import { AppInitializeModule } from './app-initialize.module';

// Application routing
import { AppRoutingModule } from './app-routing.module';
import { AppInitializeService } from './app-initialize.service';

import { ControllerModule } from './core/controller/controller.module';
//import { ControllerPortComponent } from 'shared/controller-port';
//import { TimePeriodComponent } from 'shared/time-period';

@NgModule({
  declarations: [
    AppComponent,
    // ControllerPortComponent,
    // TimePeriodComponent
  ],
  imports: [
 //   AppInitializeModule,
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
//    FontAwesomeModule,
    HttpModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatSelectModule,
    MatSidenavModule,
    MatSnackBarModule,
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