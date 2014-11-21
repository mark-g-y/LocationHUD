
require 'json'
require 'my_math'

class PoiApiController < ApplicationController

	@@MIN_DEGREE_DIFF = 3
	@@MIN_DISTANCE = 300

	def index 
		lat = params[:latitude].to_f
		long = params[:longitude].to_f
		f = MyMath.to_radians(123)
		
		nearby_locations = Poi.where('get_distance(latitude, longitude, ?, ?) < ?', lat, long, @@MIN_DISTANCE)
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