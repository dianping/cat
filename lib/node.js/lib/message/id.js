'use strict'
let HOUR = 3600 * 1000
let cluster = require('cluster')
let config = require('../config')()
let assert = require('assert')
let os = require('os')

let cpuCount = os.cpus().length

let seq = initialSeq()
let hourTS

let defaultIpHex

if (config.ip) {
    let ips = config.ip.split('.')
    assert.equal(ips.length, 4, 'ip must contains 4 groups')

    let buffer = new Buffer(4)
    for (let i = 0; i < 4; ++i) {
        buffer.writeUInt8(parseInt(ips[i]), i)
    }

    defaultIpHex = ''
    for (let j = 0; j < buffer.length; j++) {
        let b = buffer.readUInt8(j)
        defaultIpHex += ((b >> 4) & 0x0f).toString(16)
        defaultIpHex += (b & 0x0f).toString(16)
    }
}

module.exports.nextId = function (domain) {
    let ts = Math.floor(Date.now() / HOUR)

    if (ts !== hourTS) {
        seq = initialSeq()
        hourTS = ts
    }

    seq += cpuCount
    // first character is clusterId, will be different between cluster processes
    return [domain || config.domain, defaultIpHex, ts, '' + seq].join('-')
}

module.exports.MessageId = MessageId

function MessageId(domain, hexIp, timestamp, index) {
    this.domain = domain
    this.hexIp = hexIp
    this.timestamp = timestamp
    this.index = index
}

module.exports.parse = function (messageId) {
    if (!messageId) return null
    let list = messageId.split('-')
    let len = list.length

    if (len >= 4) {
        let ipAddressInHex = list[len - 3]
        let timestamp = parseInt(list[len - 2])
        let index = parseInt(list[len - 1])
        let domain = list.splice(0, len - 3).join('-')
        return new MessageId(domain, ipAddressInHex, timestamp, index)
    }

    return null
}

module.exports.getIpAddress = function () {
    let local = this.hexIp
    let ips = []

    for (let i = 0, len = local.length; i < len; i += 2) {
        let first = local.charAt(i)
        let next = local.charAt(i + 1)
        let temp = 0

        if (first >= '0' && first <= '9') {
            temp += (first - '0') << 4
        } else {
            temp += ((first - 'a') + 10) << 4
        }

        if (next >= '0' && next <= '9') {
            temp += next - '0'
        } else {
            temp += (next - 'a') + 10
        }

        ips.push(temp)
    }

    return ips.join('.')
}

function initialSeq() {
    let seq = 0
    if (process.env && process.env.pm_id) {
        seq = process.env.pm_id % cpuCount
    } else {
        if (cluster.isWorker) {
            seq = cluster.worker.id % cpuCount
        }
    }

    return seq
}
