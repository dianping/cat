var cat = require('../lib')

cat.init({
    appkey: 'nodecat'
})

for (var i = 0; i < 10; i++) {
    // Log a event with success status and empty data.
    cat.logEvent("Event", "E1")

    // The third parameter (status) is optional, default by "0".
    // It can be any of string value.
    // The event will be treated as "problem" unless the given status == cat.STATUS.SUCCESS ("0")
    // which will be recorded in our problem report.
    cat.logEvent("Event", "E2", cat.STATUS.FAIL)
    cat.logEvent("Event", "E3", "failed")

    // The fourth parameter (data) is optional, default by "".
    // It can be any of string value.
    cat.logEvent("Event", "E4", "failed", "some debug info")

    // The fourth parameter (data) can also be an object
    // In this case, the object will be dumped into json.
    cat.logEvent("Event", "E5", "failed", {a: 1, b: 2})
}
