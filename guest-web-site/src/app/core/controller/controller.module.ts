import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ControllerSocketService } from './controller-socket.service';

@NgModule({
  imports: [
    CommonModule,
  ],
  declarations: [],
  providers: [
    ControllerSocketService
  ]
})
export class ControllerModule {
  public static forRoot(url: string): ModuleWithProviders {
    return {
      ngModule: ControllerModule,
      providers: [ControllerSocketService, { provide: 'string', useValue: url }]
    };
  }
}
