export function toQuery(params, delimiter = "&") {
    const keys = Object.keys(params);

    return keys.reduce((str, key, index) => {
        let query = `${str}${key}=${encodeURIComponent(params[key])}`;

        if (index < keys.length - 1) {
            query += delimiter;
        }

        return query;
    }, "");
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

export function toRedirect(params, delimiter = "&") {
    let query = toQuery(params, delimiter);
    //Preserve equal signs by replacing them with colons
    return query.replace("%3D", "%3A");
}

export function toParamsRedirect(query) {
    let parsedQuery = toParams(query);
    //Get original equal signs by replacing the placeholder colons
    parsedQuery["state"] = parsedQuery["state"].replace(":", "=");
    return parsedQuery;
}
