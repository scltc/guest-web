import { Component, HostBinding, HostListener, OnDestroy, OnInit } from '@angular/core';
import { MatBottomSheet } from '@angular/material';
import { Route, Router, RoutesRecognized } from '@angular/router';

import { Subscription } from 'rxjs';
import { distinctUntilChanged, filter, map } from 'rxjs/operators'

import { ControllerStatusService } from 'core';
import { AppConnectionLostComponent } from './app-connection-lost.component';
import { AppInitializeService } from './app-initialize.service';
import { AppRoutingModule } from './routes/routing.module';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnDestroy, OnInit {
    logo = './assets/SCLTC-Logo-50x50.png';
    title = "app";
    year = new Date().getFullYear();
    routes = AppRoutingModule.Routes;
    showBackground = false;
    footerContent = "";

    constructor(private controllerStatus: ControllerStatusService, private router: Router, private bottomSheet: MatBottomSheet) {
        console.log("AppComponent.constructor()");
        router.navigateByUrl(AppInitializeService.initialRoute);
    }

    public showMain(route: Route): boolean {
        if (route.data) {
            // console.log(typeof(route.data.showMain));
        }

        return route.data && route.data.showMain && ((typeof (route.data.showMain) === 'function') ? route.data.showMain() : route.data.showMain);
    }

    public showSide(route: Route): boolean {
        return route.data && route.data.showSide && ((typeof (route.data.showSide) === 'function') ? route.data.showSide() : route.data.showSide)
    }

    @HostListener('window:blur')
    windowBlur() {
        console.log('blur');
    }

    @HostListener('window:focus')
    windowFocus() {
        console.log('focus');
    }

    private controllerConnectedSubscription: Subscription;
    private controllerConnectedRoute: string;
    private routerSubscription: Subscription;

    ngOnDestroy() {
        this.controllerConnectedSubscription.unsubscribe();
        this.routerSubscription.unsubscribe();
    }

    ngOnInit() {

        // Change route (if required) after connection established or lost.
        this.controllerConnectedSubscription = this.controllerStatus.connected$.pipe(
            distinctUntilChanged()
        ).subscribe(connected => {
            if (connected) {
                console.log('AppComponent: connected!');
                console.log('AppComponent: route=' + this.controllerConnectedRoute);
                if (this.controllerConnectedRoute && this.controllerConnectedRoute != '') {
                    this.router.navigateByUrl(this.controllerConnectedRoute);
                }
                else {
                    this.bottomSheet.dismiss();
                }
            }
            else {
                console.log('AppComponent: disconnected!');
                if (!(this.controllerConnectedRoute && this.controllerConnectedRoute != '')) {
                    this.bottomSheet.open(AppConnectionLostComponent, {
                        disableClose: true,
                        panelClass: 'centered-sheet'
                    });
                }
            }
        });

        // Enable/disable background wallpaper as specified for route.
        this.routerSubscription = this.router.events.pipe(
            filter(e => e instanceof RoutesRecognized),
            map(e => <RoutesRecognized>e)
        ).subscribe((e) => {
            console.log(e.state.root.firstChild.data);
            this.controllerConnectedRoute = e.state.root.firstChild.data.routeWhenConnected;
            this.showBackground = e.state.root.firstChild.data.showBackground === true;

            this.footerContent
                = (this.showBackground)
                    ? 'LEGO<sup>®</sup> is a trademark of the LEGO Group which does not sponsor, authorize, or endorse SCLTC nor this site.'
                    : '&#169; 2018 - Southern California LEGO Train Club';
        });
    }
}