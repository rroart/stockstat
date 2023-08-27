import React, { PureComponent } from 'react';
import TaskList from './TaskList';
import Client from '../util/Client';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';

class ControlPanel extends PureComponent {
    constructor(props) {
	super(props);
    }

    actionbutton(path) {
	const url = Client.geturl(path);
	const settings = {
            method: 'POST',
        };
	const res = fetch(url, settings);
	//const data = await res.json();
    }

    invalidatecache(event, props) {
        this.actionbutton([ '/cache/invalidate' ]);
    }

    dbupdatestart(event, props) {
        this.actionbutton([ '/db/update/start' ]);
    }

    dbupdateend(event, props) {
        this.actionbutton([ '/db/update/end' ]);
    }

    eventpause(event, props) {
        this.actionbutton([ '/event/pause' ]);
    }

    eventcontinue(event, props) {
        this.actionbutton([ '/event/continue' ]);
    }


    render() {
	return(
	    <div>
		<p>Empty</p>
		<TaskList {...this.props}/>
		<Navbar>
			<Navbar.Brand>
			    Brand
			</Navbar.Brand>
		    <Nav>
			<NavItem eventKey={1} href="#">
			    <Button
				bsStyle="primary"
				onClick={ (e) => this.invalidatecache(e, this.props) }
			    >
				Invalidate cache
			    </Button>
			</NavItem>
			<NavItem eventKey={2} href="#">
			    <Button
				bsStyle="primary"
				onClick={ (e) => this.dbupdatestart(e, this.props) }
			    >
				Db update start
			    </Button>
			</NavItem>
			<NavItem eventKey={3} href="#">
			    <Button
				bsStyle="primary"
				onClick={ (e) => this.dbupdateend(e, this.props) }
			    >
				Db update end
			    </Button>
			</NavItem>
			<NavItem eventKey={4} href="#">
			    <Button
				bsStyle="primary"
				onClick={ (e) => this.eventpause(e, this.props) }
			    >
				Event pause
			    </Button>
			</NavItem>
			<NavItem eventKey={5} href="#">
			    <Button
				bsStyle="primary"
				onClick={ (e) => this.eventcontinue(e, this.props) }
			    >
				Event continue
			    </Button>
			</NavItem>
		    </Nav>
		</Navbar>
	    </div>);
    }
}

export default ControlPanel;
