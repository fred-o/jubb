$(function() {

	function updateStatus(endpoint) {
		$.getJSON(endpoint, function(json) {
			var status = $('<table>').append($('<tr>')
											 .append('<th>Queue</th>')
											 .append('<th>Size</th>'));
			;
			$.each(json, function(key, value) {
				status.append($('<tr>')
							  .append($('<td>').append(endpoint + key))
							  .append($('<td>').append(value.size)));
			});
			$('#queueStatus').empty().append(status);
		});
	}

	$('#refresh').click(function() { updateStatus('/queue/'); });

	updateStatus('/queue/');

});
