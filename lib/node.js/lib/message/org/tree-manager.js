'use strict'

var Tree = require('./tree')
var Event = require('../event')
var Transaction = require('../transaction')
var Heartbeat = require('../heartbeat')

class TreeManager {
    constructor(sender) {
        // 会出现多个tree的情况
        this.trees = []

        // 最近一个挂上去的transaction message
        this.lastNode = null

        this.sender = sender
    }

    /**
     * 添加一个message到tree中，
     * 如果是transaction ， 构建树结构
     * 如果是Event,挂到transaction下面或者直接发送
     */
    addMessage(message) {
        let lastNode = this._findLastNode()

        // Transaction
        if (message instanceof Transaction) {
            if (!lastNode) {
                // 没有树或者已经发送掉了
                this.createTree(message)
                this.lastNode = message
            } else {
                // 已经构建过 tree 了,加入到最后一个transaction的子节点
                lastNode.addChild(message)
                this.lastNode = message
            }
            message.begin()
        } else if (message instanceof Event || message instanceof Heartbeat) {
            // Event or Heartbeat
            if (!lastNode) {
                // 没有构建过树的时候，直接把消息发出去
                this.sendTree(new Tree({
                    root: message
                }))
            } else {
                lastNode.addChild(message)
            }
        }
    }

    /**
     * 某个transaction结束
     * 如果是叶子节点:
     *   修改状态
     *   通知父节点
     * 如果是父节点:
     *   判断子节点的transaction是否end，
     *   如果没结束，说明add的时候挂的节点是不对的，需要修改树结构
     *   如果全都结束,通知父节点
     */
    endMessage(message, maxTime) {
        // 先end自己
        message.end(maxTime)

        if (!(message instanceof Transaction)) {
            return
        }

        if (message.isAllEnd()) {
            // 如果整个子节点都结束了
            this.notifyParentEnd(message)
        } else {
            // 如果自己结束了，但是子节点没结束，说明子节点中有挂的不对的，不应该挂在自己下面，提到自己并列
            let unEndChildren = message.children.filter(child => !child.isAllEnd())
            message.removeChild.apply(message, unEndChildren)

            if (message.parent) {
                message.parent.addChild.apply(message.parent, unEndChildren)
            } else {
                // 根节点自己结束了，但是有子节点没结束的，为子节点单独创建树
                this.sendTree(message.tree)
                unEndChildren.forEach(msg => {
                    this.createTree(msg)
                })
            }
        }
    }

    notifyParentEnd(message) {
        if (message.parent) {
            if (message.parent.isEnd) {
                // 如果父节点自己已经结束，再end一次，让父节点判断是否全都结束
                this.endMessage(message.parent)
            } else {
                // 什么都不干，等父节点end
            }
        } else {
            // 自己就是根节点
            this.sendTree(message.tree)
        }
    }

    sendTree(tree) {
        if (!tree) {
            return
        }

        var index = this.trees.indexOf(tree)
        if (index > -1) {
            this.trees.splice(index, 1)
        }

        this.sender.sendTree(tree)
    }

    // 找到最后一个节点,如果this.lastNode是end的，就一直往父节点找
    _findLastNode() {
        if (!this.lastNode) {
            return null
        }

        var last = this.lastNode
        while (last && last.isEnd) {
            last = last.parent
        }
        return last
    }

    createTree(rootMessage) {
        var tree = new Tree({
            root: rootMessage
        })
        this.trees.push(tree)
        return tree
    }
}

module.exports = TreeManager
