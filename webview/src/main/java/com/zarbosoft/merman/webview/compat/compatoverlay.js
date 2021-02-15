/** @returns {*} */
function getSymbol(/** @type {*} */ obj, /** @type {*} */ s) {
    return obj[s]();
}