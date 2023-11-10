import React, { useEffect, useState } from "react";
import Client from '../util/Client';

export default function TaskList() {
    const [answer, setAnswer] = useState([]);

    const getAnswer = async () => {
	const url = Client.geturl2("/gettasks")
	const settings = {
            method: 'POST',
        };
	const res = await fetch(url, settings);
	const data = await res.json();
	setAnswer(data);
    };

    useEffect(() => {
	const timer = setInterval(getAnswer, 60000);
	return () => clearInterval(timer);
    }, []);

    return <div>
	       {answer.map(txt => <p key={txt}>{txt}</p>)}
	   </div>;
}
