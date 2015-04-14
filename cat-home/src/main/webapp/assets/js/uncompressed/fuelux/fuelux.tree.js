/*
 * Fuel UX Tree
 * https://github.com/ExactTarget/fuelux
 *
 * Copyright (c) 2012 ExactTarget
 * Licensed under the MIT license.
 */

//define(['require','jquery'],function(require) {
(function($ , undefined) {//ACE

	//var $   = require('jquery');
	var old = $.fn.tree;

	// TREE CONSTRUCTOR AND PROTOTYPE

	var Tree = function (element, options) {
		this.$element = $(element);
		this.options = $.extend({}, $.fn.tree.defaults, options);

		this.$element.on('click', '.tree-item', $.proxy( function(ev) { this.selectItem(ev.currentTarget); } ,this));
		this.$element.on('click', '.tree-folder-header', $.proxy( function(ev) { this.selectFolder(ev.currentTarget); }, this));

		this.render();
	};

	Tree.prototype = {
		constructor: Tree,

		render: function () {
			this.populate(this.$element);
		},

		populate: function ($el) {
			var self = this;
			var $parent = $el.parent();
			var loader = $parent.find('.tree-loader:eq(0)');

			loader.show();
			this.options.dataSource.data($el.data(), function (items) {
				loader.hide();

				$.each( items.data, function(index, value) {
					var $entity;

					if(value.type === "folder") {
						$entity = self.$element.find('.tree-folder:eq(0)').clone().show();
						$entity.find('.tree-folder-name').html(value.name);
						$entity.find('.tree-loader').html(self.options.loadingHTML);
						//$entity.find('.tree-folder-header').data(value);
						//ACE
						var header = $entity.find('.tree-folder-header');
						header.data(value);

						if('icon-class' in value)
							header.find('i').addClass(value['icon-class']);
						
						if('additionalParameters' in value
							&& 'item-selected' in value.additionalParameters 
								&& value.additionalParameters['item-selected'] == true) {
								setTimeout(function(){header.trigger('click')}, 0);
						}
					} else if (value.type === "item") {
						$entity = self.$element.find('.tree-item:eq(0)').clone().show();
						$entity.find('.tree-item-name').html(value.name);
						$entity.data(value);
						
						//ACE
						if('additionalParameters' in value
							&& 'item-selected' in value.additionalParameters 
								&& value.additionalParameters['item-selected'] == true) {
								$entity.addClass ('tree-selected');
								$entity.find('i').removeClass(self.options['unselected-icon']).addClass(self.options['selected-icon']);
								//$entity.closest('.tree-folder-content').show();
						}
					}

					// Decorate $entity with data making the element
					// easily accessable with libraries like jQuery.
					//
					// Values are contained within the object returned
					// for folders and items as dataAttributes:
					//
					// {
					//     name: "An Item",
					//     type: 'item',
					//     dataAttributes = {
					//         'classes': 'required-item red-text',
					//         'data-parent': parentId,
					//         'guid': guid
					//     }
					// };

					var dataAttributes = value.dataAttributes || [];
					$.each(dataAttributes, function(key, value) {
						switch (key) {
						case 'class':
						case 'classes':
						case 'className':
							$entity.addClass(value);
							break;

						// id, style, data-*
						default:
							$entity.attr(key, value);
							break;
						}
					});

					if($el.hasClass('tree-folder-header')) {
						$parent.find('.tree-folder-content:eq(0)').append($entity);
					} else {
						$el.append($entity);
					}
				});

				// return newly populated folder
				self.$element.trigger('loaded', $parent);
			});
		},

		selectItem: function (el) {
			if(this.options['selectable'] == false) return;//ACE
			var $el = $(el);
			var $all = this.$element.find('.tree-selected');
			var data = [];

			if (this.options.multiSelect) {
				$.each($all, function(index, value) {
					var $val = $(value);
					if($val[0] !== $el[0]) {
						data.push( $(value).data() );
					}
				});
			} else if ($all[0] !== $el[0]) {
				$all.removeClass('tree-selected')
					.find('i').removeClass('icon-ok').addClass('tree-dot');
				data.push($el.data());
			}

			var eventType = 'selected';
			if($el.hasClass('tree-selected')) {
				eventType = 'unselected';
				$el.removeClass('tree-selected');
				$el.find('i').removeClass('icon-ok').addClass('tree-dot');
			} else {
				$el.addClass ('tree-selected');
				$el.find('i').removeClass('tree-dot').addClass('icon-ok');
				if (this.options.multiSelect) {
					data.push( $el.data() );
				}
			}

			if(data.length) {
				this.$element.trigger('selected', {info: data});
			}

			// Return new list of selected items, the item
			// clicked, and the type of event:
			$el.trigger('updated', {
				info: data,
				item: $el,
				eventType: eventType
			});
		},

		selectFolder: function (el) {
			var $el = $(el);
			var $parent = $el.parent();
			var $treeFolderContent = $parent.find('.tree-folder-content');
			var $treeFolderContentFirstChild = $treeFolderContent.eq(0);
			
			var target = '.' + $.trim(this.options['close-icon'].replace(/\s/g, '.'))//ACE

			var eventType, classToTarget, classToAdd;
			//if ($el.find('.icon-folder-close').length) {
			if ($el.find(target).length) {//ACE
				eventType = 'opened';
				//classToTarget = '.icon-folder-close';
				//classToAdd = 'icon-folder-open';
				classToTarget = this.options['close-icon'];//ACE
				classToAdd = this.options['open-icon'];//ACE

				$treeFolderContentFirstChild.show();
				if (!$treeFolderContent.children().length) {
					this.populate($el);
				}
			} else {
				target = '.' + $.trim(this.options['open-icon'].replace(/\s/g, '.'))//ACE
				
				eventType = 'closed';
				//classToTarget = '.icon-folder-open';
				//classToAdd = 'icon-folder-close';
				classToTarget = this.options['open-icon'];
				classToAdd = this.options['close-icon'];

				$treeFolderContentFirstChild.hide();
				if (!this.options.cacheItems) {
					$treeFolderContentFirstChild.empty();
				}
			}

			//$parent.find(classToTarget).eq(0)
				//.removeClass('icon-folder-close icon-folder-open')
				//.addClass(classToAdd);
			//ACE
			$parent.find(target).eq(0)
				.removeClass(classToTarget)
				.addClass(classToAdd);

			this.$element.trigger(eventType, $el.data());
		},

		selectedItems: function () {
			var $sel = this.$element.find('.tree-selected');
			var data = [];

			$.each($sel, function (index, value) {
				data.push($(value).data());
			});
			return data;
		},

		// collapses open folders
		collapse: function () {
			var cacheItems = this.options.cacheItems;

			// find open folders
			this.$element.find('.icon-folder-open').each(function () {
				// update icon class
				var $this = $(this)
					.removeClass('icon-folder-close icon-folder-open')
					.addClass('icon-folder-close');

				// "close" or empty folder contents
				var $parent = $this.parent().parent();
				var $folder = $parent.children('.tree-folder-content');

				$folder.hide();
				if (!cacheItems) {
					$folder.empty();
				}
			});
		}
	};


	// TREE PLUGIN DEFINITION

	$.fn.tree = function (option) {
		var args = Array.prototype.slice.call( arguments, 1 );
		var methodReturn;

		var $set = this.each(function () {
			var $this   = $( this );
			var data    = $this.data( 'tree' );
			var options = typeof option === 'object' && option;

			if( !data ) $this.data('tree', (data = new Tree( this, options ) ) );
			if( typeof option === 'string' ) methodReturn = data[ option ].apply( data, args );
		});

		return ( methodReturn === undefined ) ? $set : methodReturn;
	};

	$.fn.tree.defaults = {
		multiSelect: false,
		loadingHTML: '<div>Loading...</div>',
		cacheItems: true
	};

	$.fn.tree.Constructor = Tree;

	$.fn.tree.noConflict = function () {
		$.fn.tree = old;
		return this;
	};
//});
})(window.jQuery);//ACE