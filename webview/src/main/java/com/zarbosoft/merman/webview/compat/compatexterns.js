/**
 * @externs
 */

Intl.Segmenter = class {
    constructor(/** !string */ lang, /** !Object.<string, *> */ opts) {}
    /** @return {*} */
    segment(/** string */ text) {}
}

class ClipboardItem {
    constructor(/** !Object.<string, *> */ data) {}
}

Clipboard.prototype.write = function(/** !Array<!ClipboardItem> */ items) {};
/** @return {!Promise<!DataTransfer>} */
Clipboard.prototype.read = function() {};

/** @type {!number} */
TextMetrics.prototype.fontBoundingBoxAscent;
/** @type {!number} */
TextMetrics.prototype.fontBoundingBoxDescent;
