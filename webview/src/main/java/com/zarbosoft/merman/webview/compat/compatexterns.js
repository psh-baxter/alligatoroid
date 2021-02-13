/**
 * @externs
 */

class Segment {
    constructor() {
        /** @type {string} */
        this.segment;
        /** @type {number} */
        this.index;
        /** @type {boolean} */
        this.wordLike;
    }
}

Intl.Segmenter = class {
    constructor(/** string */ lang, /** Object<string,*>|null */ opts) {}
    /** @return {!Array<Segment>} */
    segment(/** string */ text) {}
}

class ClipboardItem {
    constructor(/** !Object.<string, *> */ data) {}
}

class Clipboard2 {
    write(/** !Array<ClipboardItem> */ items) {}

    writeText(/** !string */ text) {}

    /** @return {!Promise<DataTransfer>} */
    read() {}

    /** @return {!Promise<String>} */
    readText() {}
}

/** @type {Clipboard2} */
Navigator.clipboard;

class TextMetrics2 {
    constructor() {
        /** @type number */
        this.width;
        /** @type number */
        this.fontBoundingBoxAscent;
        /** @type number */
        this.fontBoundingBoxDescent;
    }
}
