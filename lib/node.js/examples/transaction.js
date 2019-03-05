'use strict'

let cat = require('../lib')
cat.init({
    appkey: 'nodecat'
})

let t = cat.newTransaction('foo', 'bar')

cat.logEvent('EventInTransaction', 'T1')
cat.logError('ErrorInTransaction', new Error())

t.addData('key', 'val')
t.addData('context')
t.setStatus(cat.STATUS.SUCCESS)

t.logEvent('childEvent', '1')
t.logError('childError', new Error())

setTimeout(() => t.complete(), 1000)
