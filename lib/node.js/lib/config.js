'use strict'
const os = require('os')
const logger = require('./logger.js')('config')

const getLocalIP = () => {
    var ip = process.env.HOST_IP
    if (!ip) {
        var interfaces = os.networkInterfaces()
        var addresses = []
        for (var k in interfaces) {
            for (var k2 in interfaces[k]) {
                var address = interfaces[k][k2]
                if (address.family === 'IPv4' && !address.internal) {
                    addresses.push(address.address)
                }
            }
        }
        addresses.length && (ip = addresses[0])
    }
    logger.info('Get local ip ' + ip)
    return ip
}

// exports
var config = {
    maxMessageLength: 2000,
    hostname: os.hostname(),
    domain: 'node-cat',
    ip: getLocalIP()
}

module.exports = function () {
    return config
}

module.exports.setDomain = function (domain) {
    config.domain = domain
}
