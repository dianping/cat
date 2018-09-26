'use strict'

var Message = require('./message')

/**
 * Get an instance of a transaction.
 * Transaction is inherited from the message.
 *
 * @param {object} options.
 */
class Transaction extends Message {
    constructor(options) {
        super(options)
        this.messageType = 'transaction'
    }
}

module.exports = Transaction
