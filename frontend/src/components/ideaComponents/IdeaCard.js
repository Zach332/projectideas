import { useEffect, useRef, useState, useCallback } from "react";
import ReactMarkdown from "react-markdown";
import Upvotes from "./../postComponents/Upvotes";

function useWidth(elementRef) {
    const [width, setWidth] = useState(null);

    const updateWidth = useCallback(() => {
        if (elementRef && elementRef.current) {
            const { width } = elementRef.current.getBoundingClientRect();
            setWidth(width);
        }
    }, [elementRef]);

    useEffect(() => {
        updateWidth();
        window.addEventListener("resize", updateWidth);
        return () => {
            window.removeEventListener("resize", updateWidth);
        };
    }, [updateWidth]);

    return [width];
}

export default function IdeaCard({ idea }) {
    const ref = useRef(null);
    const [width] = useWidth(ref);

    const renderers = {
        image({ alt, src, title }) {
            return (
                <img
                    alt={alt}
                    src={src}
                    title={title}
                    style={{ maxWidth: width - 40 }}
                />
            );
        },
    };

    return (
        <div className="card">
            <div className="card-header d-flex flex-wrap">
                <h1 className="me-auto">{idea.title}</h1>
                <Upvotes post={idea} postType="idea"></Upvotes>
            </div>
            <div className="card-body" ref={ref}>
                <ReactMarkdown renderers={renderers}>
                    {idea.content}
                </ReactMarkdown>
            </div>
        </div>
    );
}
