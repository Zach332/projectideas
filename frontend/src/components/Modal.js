import React from "react";

export default function Modal({ title, body, submit, onClick }) {
    return (
        <div
            className="modal fade"
            id="deleteConfirmation"
            tabIndex="-1"
            role="dialog"
            aria-labelledby="deleteConfirmationLabel"
            aria-hidden="true"
        >
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h4
                            className="modal-title"
                            id="deleteConfirmationLabel"
                        >
                            {title}
                        </h4>
                        <button
                            type="button"
                            className="close"
                            data-dismiss="modal"
                        >
                            <span aria-hidden="true">&times;</span>
                            <span className="sr-only">Close</span>
                        </button>
                    </div>
                    <div className="modal-body">{body}</div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-default"
                            data-dismiss="modal"
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="btn btn-danger"
                            data-dismiss="modal"
                            onClick={onClick}
                        >
                            {submit}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
