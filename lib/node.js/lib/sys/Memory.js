/**
 * 获取系统的memory swap  buffer/cache 使用情况
 * 单位为byte
 * */
'use strict'
var exec = require('../util/shell').exec
var os = require('os')

const DEFAULT_RESULT = {
    totalMem: 0,
    freeMem: 0,
    totalSwap: 0,
    freeSwap: 0,
    totalCache: 0,
    freeCache: 0
}
/**
 * return Object
 * {
 *  totalMem: 1,
 *  freeMem:1,
 *  totalSwap:1,
 *  freeSwap:1,
 *  totalCache: 0,
 *  freeCache: 0
 * }
 * */
exports.usage = function* () {
    function split(line) {
        return line.split(/[\s\n\r]+/)
    }

    if (process.platform === 'linux' ||
        process.platform === 'freebsd' ||
        process.platform === 'sunos') {
        try {
            var res = yield exec('free -m')
            var lines = res.trim().split('\n')
            var usage = Object.assign({}, DEFAULT_RESULT)

            var mem = split(lines[1])
            usage.totalMem = mem[1] * 1024 * 1024
            usage.freeMem = mem[3] * 1024 * 1024
            var swap = split(lines[3])
            usage.totalSwap = swap[1] * 1024 * 1024
            usage.freeSwap = swap[3] * 1024 * 1024
            var cache = split(lines[2])
            usage.totalCache = (+cache[2] + cache[2]) * 1024 * 1024
            usage.freeCache = cache[3] * 1024 * 1024
            return usage
        } catch (e) {
            // 不支持free 命令
            return {
                totalMem: os.totalmem(),
                freeMem: os.freemem(),
                totalSwap: 0,
                freeSwap: 0,
                totalCache: 0,
                freeCache: 0
            }
        }
    } else if (process.platform === 'darwin') {
        // mac
        return {
            totalMem: os.totalmem(),
            freeMem: os.freemem(),
            totalSwap: 0,
            freeSwap: 0,
            totalCache: 0,
            freeCache: 0
        }
    } else {
        return {}
    }
}
