/**
The autocomplete dropdown when typing inside search box.
<u><i class="glyphicon glyphicon-flash"></i> You don't need this. Used for demo only</u>
*/
ace.enable_searchbox_autocomplete = function($) {
	ace.vars['US_STATES'] = ["Alabama","Alaska","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","Florida","Georgia","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Dakota","North Carolina","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"]
	try {
		$('#nav-search-input').bs_typeahead({
			source: ace.vars['US_STATES'],
			updater:function (item) {
				//when an item is selected from dropdown menu, focus back to input element
				$('#nav-search-input').focus();
				return item;
			}
		});
	} catch(e) {}
}
