
require 'json'
require 'geography'

class PoiApiController < ApplicationController 

	@@MIN_DEGREE_DIFF = 3

	def index 
		lat = params[:latitude].to_f
		long = params[:longitude].to_f
		
		# must use 'or' here. Distance per degree of lat/long is different throughout the world
		# we must filter through most locations here, and then do additional manual filtering
		nearby_locations = Poi.where('abs(latitude - ?) < ? or abs(longitude - ?) < ?', lat, @@MIN_DEGREE_DIFF, long, @@MIN_DEGREE_DIFF)
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