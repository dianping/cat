'use strict'
const HeartBeat = require('./message/heartbeat')
const os = require('os')
const co = require('co')

const collector = require('./sys/collector').SystemInfoCollector

let config = require('./config')()

exports.collectStart = function (cat) {
    if (process.env && process.env.pm_id) {
        // start exact one worker to collect cpu info in pm2.
        try {
            if (process.env.pm_id % os.cpus().length !== 0) {
                return
            }
        } catch (e) {
            return
        }
    }

    cat.logEvent('Reboot', '' + config.ip)

    function sys() {
        co(function* () {
            let t = cat.newTransaction('System', 'Status')
            let status = yield collector()

            cat.treeManager.addMessage(new HeartBeat({
                data: '<?xml version="1.0" encoding="utf-8"?>\n' + status.toString()
            }))

            t.setStatus(cat.STATUS.SUCCESS)
            t.complete()
        })
    }

    sys()
    setInterval(sys, 60000)
}
