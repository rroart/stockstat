export type mainType = {
  title: string,
  description: string,
  source: string,
}

export type IclijServiceParam = {
    iclijConfig: object,
    ids: string[],
    market: string,
    wantMaps: boolean,
    confList: string[],
    webpath: string,
    offset: int,
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

export type IclijServiceResult = {
    iclijConfig: object,
    markets: string[],
    stocks: object,
    list: string[],
    maps: object,
    error: string,
    lists: IclijServiceList[],
}

export type IclijServiceList = {
    title: string,
    list: object,
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
