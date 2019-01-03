'use strict'

let Tree = require('./tree')
let Event = require('../event')
let Transaction = require('../transaction')
let Heartbeat = require('../heartbeat')

class TreeManager {
    constructor(sender, threadMode) {
        // 会出现多个tree的情况
        // this.trees = []
        this.tree = null

        // 最近一个挂上去的transaction message
        // this.lastNode = null

        this.sender = sender
        this.threadMode = threadMode
    }

    /**
     * 添加一个message到tree中，
     * 如果是transaction ， 构建树结构
     * 如果是Event,挂到transaction下面或者直接发送
     */
    addMessage(message) {
        // Transaction
        if (message instanceof Transaction) {
            // 非线程模式下，不用将transaction添加到树
            if (this.threadMode) {
                if (this.tree) {
                    this.tree.addChild(message)
                } else {
                    this.tree = this.createTree(message)
                }
            }
        } else if (message instanceof Event || message instanceof Heartbeat) {
            // Event or Heartbeat
            if (!this.threadMode || !this.tree) {
                // 非线程模式，直接把消息发出去
                // 线程模式，如果还没有第一个transaction，直接把消息发出去
                this.sendTree(new Tree({
                    root: message
                }))
            } else {
                this.tree.addChild(message)
            }
        }
    }

    /**
     * 非线程模式，直接发送消息
     */
    endMessage(message) {
        // 先end自己
        message.end()

        if (!(message instanceof Transaction)) {
            return
        }

        if (!this.threadMode) {
            this.sendTree(this.createTree(message))
        } else if (this.tree && message === this.tree.root) {
            // 线程模式，如果消息为tree的根节点，则发送消息
            this.complete()
        }
    }

    complete() {
        if (this.tree) {
            for (let child of this.tree.root.children) {
                child.end()
            }
            this.sendTree(this.tree)
            this.tree = null
        }
    }

    sendTree(tree) {
        if (!tree) {
            return
        }

        this.sender.sendTree(tree)
    }

    createTree(rootMessage) {
        return new Tree({
            root: rootMessage
        })
    }
}

module.exports = TreeManager
