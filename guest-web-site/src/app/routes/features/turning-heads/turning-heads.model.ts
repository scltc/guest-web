export interface Settings {

    enabled: boolean;

    controller: string;

    port: string;
    leftDutyCycle: number;
    rightDutyCycle: number;
    motorRunTime: number;
}

export const SettingsDefaults: Settings = {

    enabled: true,

    controller: null,

    port: 'D',
    leftDutyCycle: +33,
    rightDutyCycle: -33,
    motorRunTime: 160
};