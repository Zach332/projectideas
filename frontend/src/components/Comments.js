import React from 'react'

export default function Comments() {
    return (
        <div className="w-75 mt-5">
           <form>
                <div class="form-row align-items-center">
                    <div class="col w-100">
                        <textarea type="text" class="form-control mb-2" id="inlineFormInput" rows="1" placeholder="Write a comment "/>
                    </div>
                    <div class="col-auto">
                        <button type="submit" class="btn btn-primary mb-2">Submit</button>
                    </div>
                </div>
            </form> 
        </div>
    )
}
