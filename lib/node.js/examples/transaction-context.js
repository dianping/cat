var cat = require('../lib')

cat.init({
    appkey: 'nodecat'
})

cat = new cat.Cat(true)

let a = cat.newTransaction('Context', 'A')
let b = cat.newTransaction('Context', 'B')
let c = cat.newTransaction('Context', 'C')

setTimeout(function() {
    b.complete()
}, 1000)

setTimeout(function() {
    c.complete()
}, 1500)

setTimeout(function() {
    a.complete()
    console.log('a complete')
}, 2000)
