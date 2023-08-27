import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Loadable from 'react-loadable';

import LazyLoading from '../../common/components/LazyLoading/LazyLoading'
import { actions as mainActions } from '../../redux/modules/main';
import { mainSelector } from '../../redux/selectors/mainSelector';
import { MainWithError } from '../../common/components/Main';
import { ErrorBoundary } from '../../common/components/Utilities';

require('../../../style/index.css');

const LazyMain = Loadable({
  loader: () => import('../../common/components/Main/Main'),
  loading: LazyLoading,
})

const mapStateToProps = state => ({
  main: mainSelector(state),
});

const mapDispatchToProps = {
  ...mainActions,
};

@connect(mapStateToProps, mapDispatchToProps)
class MainView extends Component {
  static propTypes = {
    main: PropTypes.object.isRequired,
  }

  componentDidMount() {
    this.props.getAwesomeCode();
    this.props.getAwesomeR3();
    this.props.getAwesomeR4();
    this.props.getCount();
    this.props.getMarkets();
    this.props.getConfig();
      const value = new Date().toISOString();
      this.props.setenddate(value);

  }

  render() {
    console.log("here" + Object.keys(this.props));
    console.log("here" + Object.keys({...this.props}));
    const { main } = this.props;
    console.log("here" +  main);
    console.log("here" +  Object.keys(main));
     return (
      <Fragment>
        <LazyMain props = { this.props } />
        <ErrorBoundary>
          <MainWithError {...this.props} />
        </ErrorBoundary>
      </Fragment>
    )
  }
}

export default MainView;
