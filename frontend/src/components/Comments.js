import React from 'react'

export default function Comments() {
    const [rows, setRows] = React.useState(1)
    const [showButton, setshowButton] = React.useState('hidden')

    const onFocus = () => {
        setRows(5)
        setshowButton('visible')
    }

    return (
        <div className="w-75 mt-5">
           <form>
                <div class="form-row align-items-top">
                    <div class="col w-100">
                        <textarea type="text" class="form-control mb-2" id="inlineFormInput" rows={rows} onFocus={onFocus} placeholder="Write a comment "/>
                    </div>
                    <div class="col-auto align-top">
                        <button type="submit" class="btn btn-primary mb-2" style={{visibility:showButton}}>Submit</button>
                    </div>
                </div>
            </form> 
        </div>
    )
}
