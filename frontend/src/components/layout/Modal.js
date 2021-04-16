export default function Modal({
    id,
    title,
    body,
    submit,
    onClick,
    customFooter,
}) {
    return (
        <div
            className="modal fade"
            id={id}
            tabIndex="-1"
            role="dialog"
            aria-labelledby="modalLabel"
            aria-hidden="true"
        >
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h4 className="modal-title" id="modalLabel">
                            {title}
                        </h4>
                        <button
                            type="button"
                            id={id + "close"}
                            className="btn btn-close"
                            data-bs-dismiss="modal"
                        >
                            <span className="visually-hidden">Close</span>
                        </button>
                    </div>
                    <div className="modal-body">{body}</div>
                    {customFooter || (
                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-default"
                                data-bs-dismiss="modal"
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                className="btn btn-primary"
                                data-bs-dismiss="modal"
                                onClick={onClick}
                            >
                                {submit}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
