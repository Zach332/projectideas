import React from 'react'
import ReactMarkdown from 'react-markdown'

export default function Markdown() {
    const [ tryIt, setTryIt ] = React.useState('')

    const handleInputChange = (event) => {
        const target = event.target;
        setTryIt(target.value);
    }

    return (
        <div>
            <h1>Guide to basic Markdown syntax</h1>
            <table class="table">
                <thead class="thead-dark">
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
            <h1>Try it</h1>
            <div class="container pt-4">
                <div class="row">
                    <div>
                    <h6>Markdown</h6></div>
                    <div class="col">
                        <form>
                            <div className="form-group">
                                <textarea className="form-control" id="content" rows="20" onChange={handleInputChange}></textarea>
                            </div>
                        </form>
                    </div>
                    <h6>Output</h6>
                    <div class="col">
                        <div className="card">
                            <div className="card-body" >
                                <ReactMarkdown>{tryIt}</ReactMarkdown>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
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
)}

const paragraphs =
`This is the start of a paragraph.
This is not a new paragraph.

This is a new paragraph.`
const headings = 
`# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6
#Not a heading`
const bold = "**bold text**"
const italicized = "*italicized text*"
const orderedList =
`1. First list item
2. Second list item`
const unorderedList =
`- First list item
- Second list item`
const code = "\`code\`"
const horizontalRule = 
`A horizontal rule is below

---

Another section`
const link = "[a link](https://projectideas.herokuapp.com)"
const image = "![cool logo](https://bit.ly/3lO0Oqr)"