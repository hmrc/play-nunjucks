function isObject(item) {
    return (item && typeof item === 'object' && !Array.isArray(item));
}

function mergeDeep(target, source) {
    for (var key in source) {
        if (isObject(source[key])) {
            if (!target[key]) {
                var obj = {};
                obj[key] = {};
                Object.assign(target, obj);
            }
            mergeDeep(target[key], source[key]);
        } else {
            var obj = {};
            obj[key] = source[key];
            Object.assign(target, obj);
        }
    }

    return target;
}

module.exports = {
    mergeDeep
}
