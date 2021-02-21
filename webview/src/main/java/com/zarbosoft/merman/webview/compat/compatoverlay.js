/** @returns {*} */
function getSymbol(/** @type {*} */ obj, /** @type {*} */ s) {
    return obj[s]();
}

function mmCopy(/** @type {string} */ mime, /** @type {string} */ data) {
/*
    const blob = new Blob([data], {type: mime});
    const blob2 = new Blob([data], {type: 'text/plain'});
    navigator.clipboard.write([new ClipboardItem({[mime]: blob, 'text/plain': blob2})]);
    */
    const blob2 = new Blob([data], {type: 'text/plain'});
    navigator.clipboard.write([new ClipboardItem({'text/plain': blob2})]);
}

function mmCopyText(/** @type {string} */ data) {
    navigator.clipboard.writeText(data);
}

/** @returns {Promise<DataTransfer>} */
function mmUncopy() {
    return navigator.clipboard.read();
}

/** @returns {Promise<string>} */
function mmUncopyText() {
    return navigator.clipboard.readText();
}