'use strict'
const os = require('os')
const SystemInfo = require('./SystemBaseInfo')
const Time = require('../message/util/time')
const Disk = require('./Disk')
const Mem = require('./Memory')

// let userName = ''
// if (os.userInfo) {
//     userName = os.userInfo().username
// } else {
//     userName = require('child_process').execSync('whoami', {encoding: 'utf8', timeout: 1000}).replace('\n', '')
// }

/**
 * @return {SystemInfo} base system info
 */
exports.SystemInfoCollector = function* () {
    let mem = yield Mem.usage()
    let rootDisk = yield Disk.usage('/')

    let status = new SystemInfo('status', {
        timestamp: Time.date2str(new Date())
    })

    // System Extension
    let systemExtension = new SystemInfo('extension', {
        id: 'System'
    })
    status.addChild(systemExtension)

    systemExtension.addChild(new SystemInfo('extensionDetail', {
        id: 'LoadAverage',
        value: os.loadavg()[0],
    }))
    systemExtension.addChild(new SystemInfo('extensionDetail', {
        id: 'FreePhysicalMemory',
        value: mem.freeMem,
    }))
    systemExtension.addChild(new SystemInfo('extensionDetail', {
        id: 'Cache/Buffer',
        value: mem.freeCache / 1024 / 1024
    }))
    systemExtension.addChild(new SystemInfo('extensionDetail', {
        id: 'FreeSwapSpaceSize',
        value: mem.freeSwap
    }))
    systemExtension.addChild(new SystemInfo('extensionDetail', {
        id: 'HeapUsage',
        value: process.memoryUsage().heapUsed / 1024 / 1024
    }))

    // Disk Extension
    let diskExtension = new SystemInfo('extension', {
        id: 'Disk'
    })
    status.addChild(diskExtension)
    if (rootDisk) {
        diskExtension.addChild(new SystemInfo('extensionDetail', {
            id: '/ Free',
            value: rootDisk.free
        }))
    }
    return status
}
