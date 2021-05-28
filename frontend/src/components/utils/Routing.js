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
    //Preserve equal/and signs by replacing them with colons/plusses
    return query.replaceAll("%3D", "%3A").replaceAll("%26", "%2B");
}

export function toParamsRedirect(query) {
    let parsedQuery = toParams(query);
    //Get original equal/and signs by replacing the placeholder colons/plusses
    parsedQuery["state"] = parsedQuery["state"]
        .replaceAll(":", "=")
        .replaceAll("+", "&");
    return parsedQuery;
}
