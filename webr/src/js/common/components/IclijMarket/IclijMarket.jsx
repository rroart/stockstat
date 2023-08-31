import React, {memo} from 'react';
import IclijMarketBar from './IclijMarketBar';

function IclijMarket( { props, callbackNewTab } ) {
  return (
    <div>
      <IclijMarketBar props = { props } callbackNewTab = { callbackNewTab } />
    </div>
  );
}


export default memo(IclijMarket);
