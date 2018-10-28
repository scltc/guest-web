import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  public logMessage(message: any, data?: any) {
    if (data) {
      console.log(message, JSON.stringify(data));
    }
    else {
      console.log(message);
    }
  }

  public logError(message: any, data?: any) {
    if (data) {
      console.error(message, JSON.stringify(data));
    }
    else {
      console.error(message);
    }
  }
}
