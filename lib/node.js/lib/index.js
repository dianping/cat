const Cat = require('./cat')

/**
 * exports 一个默认Cat的实例 , 方便直接使用
 */
module.exports = new Cat()

module.exports.Cat = Cat
