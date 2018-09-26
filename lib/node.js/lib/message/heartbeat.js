'use strict'
var Message = require('./message')
var config = require('../config')()

class Heartbeat extends Message {
    constructor(options) {
        super(options)
        this.type = 'Heartbeat'
        this.name = config.ip
        this.status = '0'
        this.messageType = 'heartbeat'
        this.begin()
        this.end()
    }
}

module.exports = Heartbeat
