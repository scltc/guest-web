export interface Settings {

    enabled: boolean;

    controller: string;

    westPort: string;
    westMinIdle: number;
    westMaxIdle: number;

    mainPort: string;
    mainRunTime: number;

    eastPort: string;
    eastMinIdle: number;
    eastMaxIdle: number;
};

export const SettingsDefaults: Settings = {

    enabled: true,

    controller: null,

    westPort: 'A',
    westMinIdle: 1000 * 20,
    westMaxIdle: 1000 * 60,

    mainPort: 'B',
    mainRunTime: 1000 * 10,

    eastPort: 'C',
    eastMinIdle: 1000 * 20, // this.westMinIdle,
    eastMaxIdle: 1000 * 20  // this.westMaxIdle
};