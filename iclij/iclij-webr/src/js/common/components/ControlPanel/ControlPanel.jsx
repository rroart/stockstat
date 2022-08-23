import React, { PureComponent } from 'react';
import TaskList from './TaskList';

class ControlPanel extends PureComponent {
    constructor(props) {
	super(props);
    }

    render() {
	return(
	    <div>
		<p>Empty</p>
		<TaskList {...this.props}/>
	    </div>);
    }
}

export default ControlPanel;
