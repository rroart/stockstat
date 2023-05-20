export type mainType = {
  title: string,
  description: string,
  source: string,
}

class IclijServiceParam {
    configData: object;
    ids: string[];
    market: string;
    wantMaps: boolean;
    confList: string[];
    webpath: string;
    offset: int;
    guiSize: GuiSize;
    neuralnetcommand: NeuralNetCommand;
}

class ServiceParam {
    configData: object;
    ids: string[];
    market: string;
    wantMaps: boolean;
    confList: string[];
    webpath: string;
    guiSize: GuiSize;
    neuralnetcommand: NeuralNetCommand;
}

class ServiceResult {
    configData: object;
    markets: string[];
    stocks: object;
    list: string[];
    maps: object;
    error: string;
}

class IclijServiceResult {
    configData: object;
    markets: string[];
    stocks: object;
    list: string[];
    maps: object;
    error: string;
    list: string[];
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

