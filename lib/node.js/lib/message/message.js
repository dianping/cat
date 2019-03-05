'use strict'

/**
 * Get an instance of a basic message.
 *
 * @param {object} options Message options for initialization.
 */
class Message {
    constructor(options) {
        options = options || {}
        this.type = options.type || undefined
        this.name = options.name || undefined
        this.status = options.status || '0'
        this.beginTime = options.beginTime || new Date()
        this.beginTimestamp = +this.beginTime
        this.endTime = options.endTime || new Date()
        this.endTimestamp = +this.endTime
        this.data = options.data || ''

        this.children = options.children || []
        // this.parent = null
        //    this.uid = options.uid || undefined;
        //    this.uid = rand.generate();
        this.isBegin = options.isBegin || false
        this.isEnd = options.isEnd || false
        this.allEnd = false
        // this.tree = null // 如果作为tree的根节点，这个属性作为索引
        //    this.puid = options.puid || undefined;
        this.messageType = 'message' // 子类复写
    }

    addOptions(options) {
        Object.keys(options).forEach(prop => {
            if (prop === 'data') {
                if (this[prop] === undefined || this[prop] === null) {
                    this[prop] = options[prop]
                } else {
                    this[prop] = this[prop] + options[prop]
                }
            } else {
                this[prop] = options[prop]
            }
        })
    }

    begin() {
        if (this.isBegin) {
            return
        }

        this.isBegin = true
        this.beginTime = new Date()
        this.beginTimestamp = +this.beginTime
    }

    end() {
        if (this.isEnd) {
            return
        }

        this.isEnd = true
        this.endTime = new Date()
        this.endTimestamp = +this.endTime
    }

    addChild(...args) {
        args.forEach(message => {
            this.children.push(message)
            // 如果当前的已经结束,但是加进来的节点没有end
            if (this.allEnd && !message.isAllEnd()) {
                this.allEnd = false
            }
        })
    }

    removeChild(...args) {
        args.forEach(message => {
            let index = this.children.indexOf(message)
            if (index > -1) {
                this.children.splice(index, 1)
                message.parent = null
            }
        })
        this.isAllEnd()
    }

    /**
     * 是否自己和子节点全部都已经End
     */
    isAllEnd() {
        if (this.allEnd) {
            return true
        }
        if (!this.children.length) {
            return this.allEnd = this.isEnd
        }
        this.allEnd = (this.isEnd && this.children.every(child => child.isAllEnd()))
        return this.allEnd
    }
}

module.exports = Message
