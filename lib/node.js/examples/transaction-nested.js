let cat = require('../lib')

cat.init({
    appkey: 'nodecat'
})

let a = cat.newTransaction('Trans', 'A')
let b = cat.newTransaction('Trans', 'B')
let c = cat.newTransaction('Trans', 'C')

setTimeout(function() {
    a.complete()
}, 1000)

setTimeout(function() {
    b.complete()
}, 2000)

setTimeout(function() {
    c.complete()
}, 3000)
