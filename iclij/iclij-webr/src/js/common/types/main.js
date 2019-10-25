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
    guiSize: GuiSize,
}

export type ServiceResult = {
    config: object,
    markets: string[],
    stocks: object,
    list: string[],
    maps: object,
    error: string,
}

export type MyConfig = {
    configTreeMap: object,
    configValueMap: Map,
    text: Map,
    deflt: Map,
    type: Map,
    date: object,
    market: string,
}

export type GuiSize = {
    x: int;
    y: int;
}
