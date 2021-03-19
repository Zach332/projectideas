export function toQuery(params, delimiter = "&") {
    const keys = Object.keys(params);

    return encodeURI(
        keys.reduce((str, key, index) => {
            let query = `${str}${key}=${params[key]}`;

            if (index < keys.length - 1) {
                query += delimiter;
            }

            return query;
        }, "")
    ).replace(/#/g, "%23");
}

export function toParams(query) {
    query = decodeURIComponent(query);
    const q = query.replace(/^\??\//, "");

    return q.split("&").reduce((values, param) => {
        const [key, value] = param.split("=");

        values[key] = value;

        return values;
    }, {});
}
