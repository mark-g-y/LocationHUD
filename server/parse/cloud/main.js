
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("nearby_locations", function(request, response) {
	var query = new Parse.Query("Location");
	var current_location = new Parse.GeoPoint(request.params.latitude, request.params.longitude);
	query.withinKilometers("latlong", current_location, 100);
	query.find({
		success : function(results) {
			response.success(results);
		},
		error : function() {
			response.error("oh no!");
		}
	});
});