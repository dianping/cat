'use strict'
const CCatApi = require('../../build/Release/nodecat')
const Event = require('../message/event')

exports.init = (appKey) => {
    CCatApi.init(appKey)
    process.on('exit', () => {
        try {
            CCatApi.destroy()
        } catch (e) {
            // do nothing.
        }
    })
}

function countTree(message) {
    let count = 1
    message.children.forEach(child => (count += countTree(child)))
    return count
}

function position(count) {
    const array = [200, 500, 1000]
    for (let i = 0; i < array.length; i++) {
        if (count < array[i]) {
            return '<' + array[i]
        }
    }
    return '>' + array[array.length - 1]
}

exports.sendTree = (tree) => {
    if (tree.root.messageType === 'transaction') {
        let count = countTree(tree.root)
        let event = new Event({
            type: 'TreeCount',
            name: position(count),
            data: '' + count,
            status: '0'
        })
        tree.root.addChild(event)
    }
    CCatApi.sendTree(tree)
}
