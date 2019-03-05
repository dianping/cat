'use strict'
let Message = require('./message')

/**
 * Get an instance of an  event
 * CatEvent is inherited from the Message.
 *
 * @param {object} event options.
 */
class CatEvent extends Message {
    constructor(options) {
        super(options)
        this.messageType = 'event'
        this.begin()
        this.end()
    }
}

module.exports = CatEvent
