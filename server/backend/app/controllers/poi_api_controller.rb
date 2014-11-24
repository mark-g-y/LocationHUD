
require 'json'

class PoiApiController < ApplicationController

	protect_from_forgery with: :null_session

	@@MIN_DEGREE_DIFF = 3
	@@MIN_DISTANCE = 200
	@@SAME_LOCATION_TOLERANCE_DISTANCE = 0.25
	@@STRING_DIFFERENCE_THRESHOLD = 3
	@@POI_RESULT_LIMIT = 10

	# GET request
	def index 
		lat = params[:latitude].to_f
		long = params[:longitude].to_f
		
		nearby_locations = Poi.select('*, get_distance(latitude, longitude, %f, %f) as distance' % [Poi.sanitize(lat), Poi.sanitize(long)])
			.where('get_distance(latitude, longitude, ?, ?) < ?', lat, long, @@MIN_DISTANCE)
			.limit(@@POI_RESULT_LIMIT)
			.order('distance asc')
		location_list = []
		nearby_locations.each do |row|
			location_list.push(generate_location_json_from_db_row(row))
		end
		render :json => JSON.pretty_generate(location_list)
	end
	
	#POST request
	def create
		# <TODO> insert spam filter here
		
		poi_list = JSON.parse(request.body.read)
		
		for poi in poi_list
			name = poi['name']
			lat = poi['latitude']
			long = poi['longitude']
			altitude = poi['altitude']
			similar_locations = Poi.select('*, get_distance(latitude, longitude, %f, %f) as distance' % [Poi.sanitize(lat), Poi.sanitize(long)])
				.where('get_distance(latitude, longitude, ?, ?) < ?', lat, long, @@SAME_LOCATION_TOLERANCE_DISTANCE)
				.order('distance asc')
			best_match_name = get_best_name_match(name, similar_locations)
		end
		
		#render :json => JSON.pretty_generate(poi_list
		render :text => best_match_name
		
		# insert into raw poi table - these might be useful for later analysis.
		# commented out for now to avoid spamming my database during testing
		#PoiRaw.create(name: "name", latitude: 'lat', longitude: 'long', altitude: 'alt', time_uploaded: Time.now
		
	end
	
	def get_best_name_match(name, potential_locations)
		potential_locations.each do |row|
			if do_names_match(name, row['name'])
				return row['name']
			end
		end
		return name
	end
	
	def do_names_match(name1, name2)
		name1.downcase!
		name2.downcase
		# <TODO> add string similarity check to see how different they are
		return true # DEBUG for now
	end
	
	def generate_location_json_from_db_row(row)
		obj = {}
		obj['name'] = row['name']
		obj['latitude'] = row['latitude']
		obj['longitude'] = row['longitude']
		obj['altitude'] = row['altitude']
		obj['distance'] = row['distance']
		return obj
	end

end