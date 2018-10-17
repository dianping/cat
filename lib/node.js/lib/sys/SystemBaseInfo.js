'use strict'

/**
 * Base Class for System info
 * */
class SystemBaseInfo {
    constructor(name, properties, content) {
        this.name = name
        this.children = []
        this.attrs = properties || {}
        this.content = content
    }

    toString() {
        // to xml item
        var tag = '<' + this.name
        var attrKeys = Object.keys(this.attrs)
        if (attrKeys.length) {
            tag += (' ' + attrKeys.map(key => key + '=' + '"' + this.attrs[key] + '"').join(' '))
        }

        if (this.children.length) {
            tag += '>\n'
            this.children.forEach(child => {
                tag += child.toString()
            })
            tag += ('</' + this.name + '>\n')
        } else if (this.content) {
            tag += ('>' + this.content + '</' + this.name + '>\n')
        } else {
            tag += '/>\n'
        }
        return tag
    }

    addChild(childInfo) {
        if (childInfo && childInfo instanceof SystemBaseInfo) {
            this.children.push(childInfo)
        }
    }
}

module.exports = SystemBaseInfo
