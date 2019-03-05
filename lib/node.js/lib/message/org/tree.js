'use strict'

var config = require('../../config')()

/**
 * Get an instance of a message tree.
 *
 * @param {object} options Tree options for initialization.
 */
class Tree {
    constructor(options) {
        this.domain = options.domain || config.domain
        this.hostName = options.hostName || config.hostname
        this.ip = options.ip || config.ip
        this.groupName = options.groupName || config.groupName
        this.clusterId = options.clusterId || config.clusterId
        this.clusterName = options.clusterName || config.clusterName
        this.messageId = options.messageId // set later
        this.parentMessageId = options.parentMessageId || this.messageId
        this.rootMessageId = options.rootMessageId || this.messageId
        this.sessionToken = options.sessionToken || config.sessionToken

        this.root = options.root || undefined
        // if (this.root) {
        //     this.root.tree = this
        // }
    }

    addChild(message) {
        this.root.addChild(message)
    }
}

module.exports = Tree
