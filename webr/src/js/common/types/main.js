export type mainType = {
  title: string,
  description: string,
  source: string,
}

export type ServiceParam = {
    config: object,
    ids: string[],
    market: string,
    wantMaps: boolean,
    confList: string[],
    webpath: string,
}

export type ServiceResult = {
    config: object,
    markets: string[], 
    stocks: object,
    list: string[],
    maps: object,
    error: string,
}
