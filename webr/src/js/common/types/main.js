export type mainType = {
  title: string,
  description: string,
  source: string,
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

export { ServiceParam, ServiceResult, MyConfig, GuiSize, NeuralNetCommand }
