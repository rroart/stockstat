export type mainType = {
  title: string,
  description: string,
  source: string,
}

class IclijServiceParam {
    iclijConfig: object;
    ids: string[];
    market: string;
    wantMaps: boolean;
    confList: string[];
    webpath: string;
    offset: int;
}

class ServiceParam {
    config: object;
    ids: string[];
    market: string;
    wantMaps: boolean;
    confList: string[];
    webpath: string;
    guiSize: GuiSize;
    neuralnetcommand: NeuralNetCommand;
}

class ServiceResult {
    config: object;
    markets: string[];
    stocks: object;
    list: string[];
    maps: object;
    error: string;
}

class IclijServiceResult {
    iclijConfig: object;
    markets: string[];
    stocks: object;
    list: string[];
    maps: object;
    error: string;
    lists: IclijServiceList[];
}

class IclijServiceList {
    title: string;
    list: object;
}

class MyConfig {
    configTreeMap: object;
    configValueMap: Map;
    text: Map;
    deflt: Map;
    type: Map;
    date: object;
    market: string;
}

class GuiSize {
    x: int;
    y: int;
}

class NeuralNetCommand {
    mllearn : boolean;
    mlclassify : boolean;
    mldynamic : boolean;
}

export { ServiceParam, ServiceResult, IclijServiceParam, IclijServiceResult, IclijServiceList, MyConfig, GuiSize, NeuralNetCommand }

