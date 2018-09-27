import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ControllerService } from './controller.service';

@NgModule({
  imports: [
    CommonModule,
    // ControllerService
  ],
  declarations: [],
  providers: [
    ControllerService
  ]
})
export class ControllerModule {
  public static forRoot(url: string): ModuleWithProviders {
    return {
      ngModule: ControllerModule,
      providers: [ ControllerService, { provide: 'string', useValue: url } ]
    };
  }
}
