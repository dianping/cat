'use strict'

const implement = require('./adapter').implement
const EventMessage = require('./message/event')
const TransactionMessage = require('./message/transaction')
const TreeManager = require('./message/org/tree-manager')
const STATUS = require('./constant').STATUS
const system = require('./system')

let isInitialized = false

class TransactionHandler {
    constructor(transactionMessage, catInstance, threadMode) {
        this.message = transactionMessage
        this.cat = catInstance
        this.treeManager = catInstance.treeManager
        this.type = transactionMessage.type
        this.name = transactionMessage.name
        this.threadMode = threadMode
    }

    /**
     * 设置transaction状态
     * @param {string} status , STATUS
     */
    setStatus(status) {
        this.message.status = '' + status
    }

    /**
     * Add data to a transaction.
     * 序列化成 query 的形式 &key=value
     * 允许多次addData
     * @param {string} key , 如果value存在则作为key，否则作为完整的data
     * @param {string} [value]
     */
    addData(key, value) {
        let data = stringifyData(key)
        if (value !== undefined) {
            data = data + '=' + stringifyData(value)
        }
        if (this.message.data) {
            this.message.data += '&' + data
        } else {
            this.message.data += data
        }
    }

    /**
     * end a transaction.
     */
    complete() {
        this.treeManager.endMessage(this.message)
    }

    /**
     * logEvent , 同cat.logEvent , 但确保挂在此transaction下
     */
    logEvent(type, name, status, data) {
        let message = this.message
        if (message.isEnd) {
            return this.cat.logEvent(type, name, status, data)
        } else {
            message.addChild(createEvent(type, name, status, data))
        }
    }

    newTransaction(type, name) {
        type = '' + type
        name = '' + name
        let message = new TransactionMessage({
            type: type,
            name: name
        })
        if (this.threadMode) {
            this.message.addChild(message)
        }
        return new TransactionHandler(message, this.cat, this.threadMode)
    }

    /**
     * logError , 同 cat.logError , 但确保挂在此transaction下
     */
    logError(name, error) {
        let message = this.message
        if (message.isEnd) {
            return this.cat.logError(name, error)
        } else {
            message.addChild(createError(name, error))
        }
    }
}

/**
 * Class Cat
 * 暴露给用户的API在这边，以这里的参数说明为准
 */
class Cat {
    constructor(threadMode) {
        this.STATUS = STATUS
        this.treeManager = new TreeManager(implement, threadMode)
        this.threadMode = threadMode
    }

    /**
     * @param {object} options
     *        {string} options.appkey
     */
    init(options) {
        if (isInitialized) {
            return
        }
        isInitialized = true
        let logger = require('./logger')('index')
        options = options || {}
        logger.info('Cat Version : ' + require('../package').version)

        let appkey = options.appkey
        if (!appkey) {
            logger.info('Appkey is required')
            return
        }
        logger.info('Appkey has been set to ' + appkey)

        implement.init(appkey)

        system.collectStart(this)
    }

    /**
     * @param {string} type 一级名称
     * @param {string} name  二级名称
     * @param {string} [status]  状态, 参见 STATUS
     * @param {string} [data]  数据
     */
    logEvent(type, name, status, data) {
        this.treeManager.addMessage(createEvent(type, name, status, data))
    }

    /**
     * @param {string} [name]
     * @param {Error} error
     */
    logError(name, error) {
        this.treeManager.addMessage(createError(name, error))
    }

    /**
     * 同CatInterface
     * @param {string} type
     * @param {string} name
     */
    newTransaction(type, name) {
        type = '' + type
        name = '' + name
        let message = new TransactionMessage({
            type: type,
            name: name
        })
        this.treeManager.addMessage(message)
        return new TransactionHandler(message, this, this.threadMode)
    }

    complete() {
        if (this.threadMode) {
            this.treeManager.complete()
        }
    }
}

function createEvent(type, name, status, data) {
    type = '' + type
    name = '' + name
    data = stringifyData(data)
    status = status ? ('' + status) : STATUS.SUCCESS
    return new EventMessage({
        type: type,
        name: name,
        status: status,
        data: data
    })
}

function createError(name, error) {
    if (name instanceof Error) {
        error = name
        name = null
    }
    name = name || (error && error.name) || 'Error'
    let stack = error ? error.stack : ''
    let errStr = name ? (name + ' ' + stack) : stack
    return createEvent('Error', name, 'ERROR', errStr)
}

function stringifyData(data) {
    if (data === undefined) {
        data = ''
    }
    if (typeof data !== 'string') {
        // 有data参数,但不是字符串
        try {
            data = JSON.stringify(data)
        } catch (e) {
            data = data.toString ? data.toString() : ''
        }
    }
    return data
}

module.exports = Cat
