'use strict'

var moment = require('moment')

function date2str(date) {
    return moment(date).format('YYYY-MM-DD HH:mm:ss.SSS')
}

function durationInMillis(start, end) {
    return (end - start)
}

function durationInMicros(start, end) {
    return (end - start) * 1000
}

module.exports = {
    date2str: date2str,
    durationInMillis: durationInMillis,
    durationInMicros: durationInMicros
}
