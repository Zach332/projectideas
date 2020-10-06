import React, { useState, useEffect } from 'react';

export default function ServerTime() {
    const [message, setMessage] = useState("");

	useEffect(() => {
        fetch('/api/hello')
            .then(response => response.text())
            .then(message => {
                setMessage(message);
            });
    },[])

	const updateTime = () => {
		fetch('/api/hello')
            .then(response => response.text())
            .then(message => {
                setMessage(message);
            });
	};

	return (
		<div className="mt-4">
			Login <a href="/login">here</a>!
			<div className="card">
				<h5 className="card-header">Server Time</h5>
				<div className="card-body">
					<p className="card-text">{message}</p>
					<a className="btn btn-primary text-white" onClick={updateTime}>
						Update Time
					</a>
				</div>
			</div>
		</div>
	);
}
