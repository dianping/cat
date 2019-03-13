'use strict'

const exec = require('../util/shell').exec
const fs = require('fs')

if (process.platform === 'linux' ||
    process.platform === 'freebsd' ||
    process.platform === 'darwin' ||
    process.platform === 'sunos') {
    exports.usage = function* (drive) {
        if (!fs.existsSync(drive)) {
            return null
        }
        try {
            let res = yield exec('df -k \'' + drive.replace(/'/g, '\'\\\'\'') + '\'')
            let lines = res.trim().split('\n')

            let strDiskInfo = lines[lines.length - 1].replace(/[\s\n\r]+/g, ' ')
            let diskInfo = strDiskInfo.split(' ')

            return {
                available: diskInfo[3] * 1024,
                total: diskInfo[1] * 1024,
                free: diskInfo[3] * 1024
            }
        } catch (e) {
            return null
        }
    }
} else {
    exports.usage = function* () {
        return null
    }
}
