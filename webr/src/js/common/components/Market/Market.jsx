import React, {memo} from 'react';
import MarketBar from './MarketBar';

function Market( { props, callbackNewTab } ) {
  return (
    <div>
      <MarketBar props = { props } callbackNewTab = { callbackNewTab } />
    </div>
  );
}

export default memo(Market);
