
Parse.Cloud.define("nearby_locations", function(request, response) {

	function Cluster(){
		this.name = "";
		this.names = {};
		this.number_of_points = 0;
		this.most_popular_count = 0;
		this.add = function(name, result) {
			if (!(name in this.names)) {
				this.names[name] = [];
			}
			this.names[name].push(result);
			if (this.names[name].length > this.most_popular_count) {
				this.name = name;
				this.most_popular_count = this.names[name].length;
			}
			this.number_of_points++;
		};
		this.get_levenshtein_distance = function(name1, name2) {
			var d = new Array(name1.length + 1);
			for (var i = 0; i < name1.length + 1; i++) {
				d[i] = new Array(name2.length + 1);
				d[i][0] = i;
			}
			for (var i = 0; i < name2.length + 1; i++) {
				d[0][i] = i;
			}
			for (var x = 1; x < name2.length + 1; x++) {
				for (var y = 1; y < name1.length + 1; y++) {
					if (name1[y] == name2[x]) {
						d[y][x] = d[y - 1][x - 1];
					} else {
						d[y][x] = Math.min(d[y - 1][x], d[y][x - 1], d[y - 1][x - 1]) + 1;
					}
				}
			}
			return d[name1.length][name2.length];
		};
		this.get_distance = function (lat1,lon1,lat2,lon2) {
			var R = 6371; // Radius of the earth in km
			var dLat = this.deg2rad(lat2-lat1);  // deg2rad below
			var dLon = this.deg2rad(lon2-lon1); 
			var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2); 
			var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
			var d = R * c; // Distance in km
			return d;
		};
		this.deg2rad = function(deg) {
			return deg * (Math.PI/180)
		};
		this.is_in_cluster = function(name, latitude, longitude) {
			var latlong = this.names[this.name][0].get("latlong").toJSON();
			if (this.get_levenshtein_distance(name.toLowerCase(), this.name.toLowerCase()) < 3 && Math.abs(this.get_distance(latitude, longitude, latlong["latitude"], latlong["longitude"])) < 0.1) {
				return true;
			}
			return false;
		};
	}
		
	var query = new Parse.Query("Location");
	var current_location = new Parse.GeoPoint(request.params.latitude, request.params.longitude);
	query.withinKilometers("latlong", current_location, 100);
	query.find({
		success : function(results) {
			var groups = [];
			for (var i = 0; i < results.length; i++) {
				var result = results[i];
				if (groups.length == 0) {
					var group = new Cluster();
					group.add(result.get("name"), result);
					groups.push(group);
				} else {
					for (var m = 0; m < groups.length; m++) {
						var latlong = result.get("latlong").toJSON();
						if (groups[m].is_in_cluster(result.get("name"), latlong["latitude"], latlong["longitude"])) {
							groups[m].add(result.get("name"), result);
							break;
						} else if (m == groups.length - 1) {
							var group = new Cluster();
							group.add(result.get("name"), result);
							groups.push(group);
							break;
						}
					}
				}
			}
			results = [];
			groups.sort(function(a, b) {
				return -(a.number_of_points - b.number_of_points);
			});
			var MAX_GROUP_LENGTH = 8;
			for (var i = 0; i < groups.length && i < MAX_GROUP_LENGTH; i++) {
				result = {};
				group = groups[i];
				name = group["name"];
				result["title"] = name;
				result["altitude"] = group["names"][name][0].get("altitude");
				var latlong = group["names"][name][0].get("latlong").toJSON();
				result["latitude"] = latlong["latitude"];
				result["longitude"] = latlong["longitude"];
				result["number_of_points"] = group["number_of_points"];
				results.push(result);
			}
			response.success(JSON.stringify(results));
		},
		error : function() {
			response.error("oh no!");
		}
	});
});

Parse.Cloud.define("nearby_locations_within_range", function(request, response) {
	var query = new Parse.Query("Location");
	var latitude = request.params.latitude;
	var longitude = request.params.longitude;
	var current_location = new Parse.GeoPoint(latitude, longitude);
	query.withinKilometers("latlong", current_location, 100);
	query.find({
		success : function(results) {
			var pois = [];
			for (var i = 0; i < results.length; i++) {
				var poi = {};
				var result = results[i];
				var latlong = result.get("latlong").toJSON();
				poi["title"] = result.get("name");
				poi["latitude"] = latlong["latitude"];
				poi["longitude"] = latlong["longitude"];
				poi["altitude"] = result.get("altitude");
				poi["distance"] = current_location.kilometersTo(new Parse.GeoPoint(latlong["latitude"], latlong["longitude"]))
				poi["count"] = result.get("count");
				pois.push(poi);
			}
			pois.sort(function(a, b) {
				return a.distance - b.distance;
			});
			// maybe sort by popularity, aka count
			response.success(JSON.stringify(pois));
		},
		error : function() {
			response.error("oh no!");
		}
	});
});