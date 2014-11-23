
require 'json'

class PoiApiController < ApplicationController

	@@MIN_DEGREE_DIFF = 3
	@@MIN_DISTANCE = 200
	@@POI_RESULT_LIMIT = 10

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
	
	def generate_location_json_from_db_row(row)
		obj = {}
		obj['name'] = row['name']
		obj['latitude'] = row['latitude']
		obj['longitude'] = row['longitude']
		obj['altitude'] = row['altitude']
		obj['distance'] = row['distance']
		return obj
	end

	def test_index
		lat = params[:latitude].to_i
		long = params[:longitude].to_i
		obj = {latitude:@lat, longitude:@long}
		everything = []
		results = ResponseType.all
		results.each do |row|
			everything.push({response_type:row["response_type"], description:row["description"]})
		end
		render :json => JSON.pretty_generate(everything)
	end

end