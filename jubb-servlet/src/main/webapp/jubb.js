$(function() {
	
	function updateStatus() {
		$.getJSON("queue", function(json) {
			$.each(json, function(key, val) {
				$('#queueStatus').append($('<div>').append(key));
			});
		});
	}

	updateStatus();

});