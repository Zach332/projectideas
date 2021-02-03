import React from "react";
import ReactMarkdown from "react-markdown";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";

export default function Home() {
    return (
        <div>
            <Helmet>
                <title>About | {Globals.Title}</title>
            </Helmet>
            <h1>About</h1>
            <ReactMarkdown>{markdown}</ReactMarkdown>
        </div>
    );
}

const markdown = `
Have you ever wished there was an app for something? If you're a developer, do you want to create apps that serve a real purpose and are built with the end user in mind? 

If so, projectideas is for you.

## Connecting aspiring developers to real needs
projectideas is a platform that brings together software engineers with the people their products serve.

### For everyone
Often, people have needs that software could resolve, but are unable to communicate those needs or develop the software themselves. Often, software intended to serve a certain purpose loses track of the end user.

projectideas is a solution that enables everyone to post ideas for software that would improve their lives in some way. Programmers can message idea proposers to learn more about their needs, and are invited to post updates on projectideas if they decide to work on an idea. Since we encourage open-source projects on projectideas, we hope many idea proposers will ultimately be able to see their idea come to fruition.

### For programmers, engineers, and entrepreneurs
Often, programmers looking to improve their skills create replicas when they would prefer to learn by creating a project with real impact. Often, programmers fruitlessly brainstorm ways to create an application that would improve someone's life, without having access to the solvable problems other people regularly face.

projectideas is a solution that enables developers to work on projects that solve a real need. It lets engineers to talk to the people their product is designed for, and to center their efforts on the actual consumer. It also provides a way for engineers to post information about the ideas they are working on so that their solutions can reach their intended audience.

`;
