'use strict'

const spawn = require('child_process').spawn

exports.exec = function (cmd) {
    return new Promise((resolve, reject) => {
        let shell = spawn('sh', ['-c', cmd])
        let result = []

        shell.stdout.on('data', (data) => {
            result.push(data.toString())
        })

        shell.on('error', e => {
            reject(e)
        })

        shell.on('close', code => {
            if (code) {
                reject(new Error('exec [' + cmd + '] fail'))
            } else {
                resolve(result.join())
            }
        })
    })
}
