/*
 * jQuery Input Limiter plugin 1.3.1
 * http://rustyjeans.com/jquery-plugins/input-limiter/
 *
 * Copyright (c) 2009 Russel Fones <russel@rustyjeans.com>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

(function ($) {
	$.fn.inputlimiter = function (options) {
		var opts = $.extend({}, $.fn.inputlimiter.defaults, options),
			$elements = $(this);
		if (opts.boxAttach && !$('#' + opts.boxId).length) {
			$('<div/>').appendTo("body").attr({id: opts.boxId, 'class': opts.boxClass}).css({'position': 'absolute'}).hide();
			// apply bgiframe if available
			if ($.fn.bgiframe) {
				$('#' + opts.boxId).bgiframe();
			}
		}

		var inputlimiterKeyup = function (e) {
			var $this = $(this),
				count = counter($this.val());
			if (!opts.allowExceed && count > opts.limit) {
				$this.val(truncater($this.val()));
			}
			if (opts.boxAttach) {
				$('#' + opts.boxId).css({
					'width': $this.outerWidth() - ($('#' + opts.boxId).outerWidth() - $('#' + opts.boxId).width()) + 'px',
					'left': $this.offset().left + 'px',
					'top': ($this.offset().top + $this.outerHeight()) - 1 + 'px',
					'z-index': 2000
				});
			}
			var charsRemaining = (opts.limit - count > 0 ? opts.limit - count : 0),
				remText = opts.remTextFilter(opts, charsRemaining),
				limitText = opts.limitTextFilter(opts);

			if (opts.limitTextShow) {
				$('#' + opts.boxId).html(remText + ' ' + limitText);
				// Check to see if the text is wrapping in the box
				// If it is lets break it between the remaining test and the limit test
				var textWidth = $("<span/>").appendTo("body").attr({id: '19cc9195583bfae1fad88e19d443be7a', 'class': opts.boxClass}).html(remText + ' ' + limitText).innerWidth();
				$("#19cc9195583bfae1fad88e19d443be7a").remove();
				if (textWidth > $('#' + opts.boxId).innerWidth()) {
					$('#' + opts.boxId).html(remText + '<br />' + limitText);
				}
				// Show the limiter box
				$('#' + opts.boxId).show();
			} else {
				$('#' + opts.boxId).html(remText).show();
			}
		},

		inputlimiterKeypress = function (e) {
			var count = counter($(this).val());
			if (!opts.allowExceed && count > opts.limit) {
				var modifierKeyPressed = e.ctrlKey || e.altKey || e.metaKey;
				if (!modifierKeyPressed && (e.which >= 32 && e.which <= 122) && this.selectionStart === this.selectionEnd) {
					return false;
				}
			}
		},

		inputlimiterBlur = function () {
			var $this = $(this);
				count = counter($this.val());
			if (!opts.allowExceed && count > opts.limit) {
				$this.val(truncater($this.val()));
			}
			if (opts.boxAttach) {
				$('#' + opts.boxId).fadeOut('fast');
			} else if (opts.remTextHideOnBlur) {
				var limitText = opts.limitText;
				limitText = limitText.replace(/\%n/g, opts.limit);
				limitText = limitText.replace(/\%s/g, (opts.limit === 1 ? '' : 's'));
				$('#' + opts.boxId).html(limitText);
			}
		},

		counter = function (value) {
			if (opts.limitBy.toLowerCase() === "words") {
				return (value.length > 0 ? $.trim(value).replace(/\ +(?= )/g, '').split(' ').length : 0);
			}
			var count = value.length,
				newlines = value.match(/\n/g);
			if (newlines && opts.lineReturnCount > 1) {
				count += newlines.length * (opts.lineReturnCount - 1);
			}
			return count;
		},

		truncater = function (value) {
			if (opts.limitBy.toLowerCase() === "words") {
				return $.trim(value).replace(/\ +(?= )/g, '').split(' ').splice(0, opts.limit).join(' ') + ' ';
			}
			return value.substring(0, opts.limit);
		};

		$(this).each(function (i) {
			var $this = $(this);
			if ((!options || !options.limit) && opts.useMaxlength && parseInt($this.attr('maxlength')) > 0 && parseInt($this.attr('maxlength')) != opts.limit) {
				$this.inputlimiter($.extend({}, opts, { limit: parseInt($this.attr('maxlength')) }));
			} else {
				if (!opts.allowExceed && opts.useMaxlength && opts.limitBy.toLowerCase() === "characters") {
					$this.attr('maxlength', opts.limit);
				}
				$this.unbind('.inputlimiter');
				$this.bind('keyup.inputlimiter', inputlimiterKeyup);
				$this.bind('keypress.inputlimiter', inputlimiterKeypress);
				$this.bind('blur.inputlimiter', inputlimiterBlur);
			}
		});
	};

	$.fn.inputlimiter.remtextfilter = function (opts, charsRemaining) {
		var remText = opts.remText;
		if (charsRemaining === 0 && opts.remFullText !== null) {
			remText = opts.remFullText;
		}
		remText = remText.replace(/\%n/g, charsRemaining);
		remText = remText.replace(/\%s/g, (opts.zeroPlural ? (charsRemaining === 1 ? '' : 's') : (charsRemaining <= 1 ? '' : 's')));
		return remText;
	};

	$.fn.inputlimiter.limittextfilter = function (opts) {
		var limitText = opts.limitText;
		limitText = limitText.replace(/\%n/g, opts.limit);
		limitText = limitText.replace(/\%s/g, (opts.limit <= 1 ? '' : 's'));
		return limitText;
	};

	$.fn.inputlimiter.defaults = {
		limit: 255,
		boxAttach: true,
		boxId: 'limiterBox',
		boxClass: 'limiterBox',
		remText: '%n character%s remaining.',
		remTextFilter: $.fn.inputlimiter.remtextfilter,
		remTextHideOnBlur: true,
		remFullText: null,
		limitTextShow: true,
		limitText: 'Field limited to %n character%s.',
		limitTextFilter: $.fn.inputlimiter.limittextfilter,
		zeroPlural: true,
		allowExceed: false,
		useMaxlength: true,
		limitBy: 'characters',
		lineReturnCount: 1
	};

})(jQuery);
