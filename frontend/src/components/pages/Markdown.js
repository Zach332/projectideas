import React from "react";
import ReactMarkdown from "react-markdown";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";

export default function Markdown() {
    const [tryIt, setTryIt] = React.useState("");

    const handleInputChange = (event) => {
        const target = event.target;
        setTryIt(target.value);
    };

    const onCLick = () => {
        const anchor = document.querySelector("#tryIt");
        anchor.scrollIntoView({ behavior: "smooth", block: "center" });
    };

    return (
        <div>
            <Helmet>
                <title>Markdown | {Globals.Title}</title>
            </Helmet>
            <h1 className="pb-3">Guide to basic Markdown syntax</h1>
            <button onClick={onCLick} className="btn btn-link btn-lg mb-4">
                Try it
                <svg
                    width="1em"
                    height="1em"
                    viewBox="0 0 16 16"
                    className="bi bi-arrow-down-short"
                    fill="currentColor"
                    xmlns="http://www.w3.org/2000/svg"
                >
                    <path
                        fillRule="evenodd"
                        d="M8 4a.5.5 0 0 1 .5.5v5.793l2.146-2.147a.5.5 0 0 1 .708.708l-3 3a.5.5 0 0 1-.708 0l-3-3a.5.5 0 1 1 .708-.708L7.5 10.293V4.5A.5.5 0 0 1 8 4z"
                    />
                </svg>
            </button>
            <div className="table-responsive-sm">
                <table className="table">
                    <thead className="thead-dark">
                        <tr>
                            <th scope="col">Markdown</th>
                            <th scope="col">Output</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tableRow(paragraphs)}
                        {tableRow(headings)}
                        {tableRow(bold)}
                        {tableRow(italicized)}
                        {tableRow(orderedList)}
                        {tableRow(unorderedList)}
                        {tableRow(code)}
                        {tableRow(horizontalRule)}
                        {tableRow(link)}
                        {tableRow(image)}
                    </tbody>
                </table>
            </div>
            <h1>Try it</h1>
            <div id="tryIt" className="container-fluid pt-4">
                <div className="row">
                    <div className="col-6">
                        <h6>Markdown</h6>
                        <form>
                            <textarea
                                className="form-control"
                                rows="20"
                                onChange={handleInputChange}
                            ></textarea>
                        </form>
                    </div>
                    <div className="col-6">
                        <h6>Output</h6>
                        <div className="card mh-50">
                            <div className="card-body">
                                <ReactMarkdown>{tryIt}</ReactMarkdown>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

function tableRow(stringToRender) {
    return (
        <tr>
            <td>
                <pre>{stringToRender}</pre>
            </td>
            <td>
                <ReactMarkdown>{stringToRender}</ReactMarkdown>
            </td>
        </tr>
    );
}

const paragraphs = `This is the start of a paragraph.
This is not a new paragraph.

This is a new paragraph.`;
const headings = `# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6
#Not a heading`;
const bold = "**bold text**";
const italicized = "*italicized text*";
const orderedList = `1. First list item
2. Second list item`;
const unorderedList = `- First list item
- Second list item`;
const code = "`code`";
const horizontalRule = `A horizontal rule is below

---

Another section`;
const link = "[a link](https://projectideas.herokuapp.com)";
const image = "![cool logo](https://bit.ly/3lO0Oqr)";
