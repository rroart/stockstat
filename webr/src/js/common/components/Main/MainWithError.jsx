import React, { PureComponent } from 'react';

import './Main.css';

class MainWithError extends PureComponent {
  render() {
    const { main } = this.props;
    const result = main && main.result ? main.result : null;

    if (result && result.size && result.size > 0) {
      return (
        <div className="mainOutput">
          <h1>This should catch by ErrorBoundary</h1>
          {result.something_not_existed.get('something_not_existed')}
        </div>
      );
    }
    return <div />;
  }
}

export default MainWithError;
