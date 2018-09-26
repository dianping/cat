'use strict'
var HOUR = 3600 * 1000
var cluster = require('cluster')
var config = require('../config')()
var assert = require('assert')
var os = require('os')

var cpuCount = os.cpus().length

var seq = initialSeq()
var hourTS

var defaultIpHex
if (config.ip) {
    var ips = config.ip.split('.')
    assert.equal(ips.length, 4, 'ip must contains 4 groups')
    var buffer = new Buffer(4)
    for (var i = 0; i < 4; ++i) {
        buffer.writeUInt8(parseInt(ips[i]), i)
    }

    defaultIpHex = ''
    for (var j = 0; j < buffer.length; j++) {
        var b = buffer.readUInt8(j)
        defaultIpHex += ((b >> 4) & 0x0f).toString(16)
        defaultIpHex += (b & 0x0f).toString(16)
    }
}

module.exports.nextId = function (domain) {
    var ts = Math.floor(Date.now() / HOUR)

    if (ts != hourTS) {
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
    var list = messageId.split('-')
    var len = list.length

    if (len >= 4) {
        var ipAddressInHex = list[len - 3]
        var timestamp = parseInt(list[len - 2])
        var index = parseInt(list[len - 1])
        var domain = list.splice(0, len - 3).join('-')
        return new MessageId(domain, ipAddressInHex, timestamp, index)
    }

    return null
}

module.exports.getIpAddress = function () {
    var local = this.hexIp
    var i = []
    for (var i = 0, len = local.length; i < len; i += 2) {
        var first = local.charAt(i)
        var next = local.charAt(i + 1)
        var temp = 0

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

        i.push(temp)
    }

    return i.join('.')
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
