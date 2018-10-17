import React, { Component } from 'react';

const style = {
  padding: '.75rem 1.25rem',
  marginBottom: '1rem',
  border: '1px solid transparent',
  borderRadius: '.25rem',
  color: '#721c24',
  backgroundColor: '#f8d7da',
  borderColor: '#f5c6cb',
};

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
    };
  }
  componentDidCatch(error, info) {
    // you probably want to log it somewhere
    console.log(error, info);
    this.setState({
      hasError: true,
    });
  }
  render() {
    if (this.state.hasError) {
      return (
        <div style={style}>
          Something is going wrong!! This error is caught by ErrorBoundary feature in React 16 \o/
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
