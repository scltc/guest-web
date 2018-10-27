import { Component, HostBinding, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Route, Router, RoutesRecognized } from '@angular/router';

import { filter, map } from 'rxjs/operators'

// import { ControllerService } from "core";

import { AppRoutingModule } from './routes/routing.module';
import { AppInitializeService } from './app-initialize.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  logo = './assets/SCLTC-Logo-50x50.png';
  title = "app";
  year = new Date().getFullYear();
  routes = AppRoutingModule.Routes;
  showBackground = false;
  footerContent = "";

  constructor(private router: Router) {
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

  ngOnInit() {
    // Enable/disable background wallpaper as specified for route.
    this.router.events.pipe(
      filter(e => e instanceof RoutesRecognized),
      map(e => <RoutesRecognized>e))
      .subscribe((e) => {
        console.log(e.state.root.firstChild.data);
        this.showBackground = e.state.root.firstChild.data.showBackground === true;

        this.footerContent
          = (this.showBackground)
            ? 'LEGO<sup>®</sup> is a trademark of the LEGO Group which does not sponsor, authorize, or endorse SCLTC nor this site.'
            : '&#169; 2018 - Southern California LEGO Train Club';
      });
  }
}