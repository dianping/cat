'use strict'
const os = require('os')
const logger = require('./logger.js')('config')

const getLocalIP = () => {
    let ip = process.env.HOST_IP
    if (!ip) {
        let interfaces = os.networkInterfaces()
        let addresses = []

        for (let k of Object.values(interfaces)) {
            for (let k2 of k) {
                let address = k2
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
let config = {
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
