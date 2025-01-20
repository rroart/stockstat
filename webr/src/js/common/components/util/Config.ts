import { ConfigMaps, ConfigData } from '../../types/main'
import { IclijServiceParam, ServiceResult } from '../../types/main'
import { MyMap } from '.';

function getConfigData(config, market, date) {
  const myconfig = new ConfigData();
  myconfig.configTreeMap = MyMap.myget(config, 'configTreeMap');
  myconfig.configValueMap = MyMap.myget(config, 'configValueMap');
  const configmaps = new ConfigMaps();
  myconfig.configMaps = configmaps;
  configmaps.text = MyMap.myget(config, 'text');
  configmaps.deflt = MyMap.myget(config, 'deflt');
  configmaps.type = MyMap.myget(config, 'map');
  myconfig.date = date;
  myconfig.market = market;
  return myconfig;
}

function getParam(config, webpath) {
  var serviceparam = new IclijServiceParam();
  const date = MyMap.myget(config, 'enddate');
  serviceparam.market = MyMap.myget(config, 'market');
  serviceparam.configData = getConfigData(config, serviceparam.market, date);
  serviceparam.webpath = webpath;
  return serviceparam;
}

const Config = { getConfigData, getParam };
export default Config;
