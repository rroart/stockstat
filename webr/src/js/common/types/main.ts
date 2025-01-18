/*
export type mainType = {
  title: string,
  description: string,
  source: string,
}
*/

class IclijServiceParam {
    configData: object;
    ids: string[];
    market: string;
    wantMaps: boolean;
    confList: string[];
    webpath: string;
    offset: number;
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
    lists: IclijServiceList[];
}

class IclijServiceList {
    title: string;
    list: object;
}

class ConfigMaps {
    text: Map<string, object>;
    deflt: Map<string, object>;
    type: Map<string, object>;
}

class ConfigData {
    configMaps: ConfigMaps;
    configTreeMap: object;
    configValueMap: Map<string, object>;
    date: object;
    market: string;
}

class GuiSize {
    x: number;
    y: number;
}

class NeuralNetCommand {
    mllearn : boolean;
    mlclassify : boolean;
    mldynamic : boolean;
}

export { ServiceParam, ServiceResult, IclijServiceParam, IclijServiceResult, IclijServiceList, ConfigMaps, ConfigData, GuiSize, NeuralNetCommand }

