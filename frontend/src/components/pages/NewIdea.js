import React from 'react'

export default function NewIdea() {
    const [idea, setIdea] = React.useState([{ title: '' , content: ''}])

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setIdea(
            idea => ({
                ...idea,
                [name]: target.value
            })
        );
    }

    const handleSubmit = (event) => {
        console.log(idea)
        event.preventDefault();
    }

    return (
        <div className="mx-auto">
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="title">Title</label>
                    <input type="text" className="form-control" id="title" onChange={handleInputChange}/>
                </div>
                <div className="form-group">
                    <label htmlFor="content">Details</label>
                    <textarea className="form-control" id="content" rows="3" onChange={handleInputChange}></textarea>
                </div>
                <button type="submit" className="btn btn-primary">Post Idea</button>
            </form>
        </div>
    )
}
