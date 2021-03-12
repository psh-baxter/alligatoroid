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

if (Intl.Segmenter === undefined) {
    Intl.Segmenter = class {
        constructor(lang, opts) {
            this.mode = opts["granularity"];
        }
        segment(text) {
            const out = new Array();
            if (this.mode == "word") {
                out.push(0);
                for (let i = 0; i < text.length; ++i) {
                    if (text[i] == ' ' || text[i] == '\t') {
                        if (out[out.length - 1] != i) {
                            out.push(i);
                        }
                        out.push(i + 1);
                    }
                }
            } else if (this.mode == "grapheme") {
                let off = 0;
                for (let g of Array.from(text)) {
                    out.push(off);
                    off += g.length;
                }
            }
            if (out[out.length - 1] != text.length) {
                out.push(text.length);
            }
            return out
        }
    }
}