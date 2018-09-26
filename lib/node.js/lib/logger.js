module.exports = function (filename) {
    return {
        error: function (msg) {
            console.log(msg);
        },
        info: function (msg) {
            console.log(msg);
        },
        warn: function (msg) {
            console.log(msg);
        }
    }
}