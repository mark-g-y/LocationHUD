
require 'json'

class PoiApiController < ApplicationController 

	def index
		@lat = params[:latitude].to_i
		@long = params[:longitude].to_i
		@obj = {latitude:@lat, longitude:@long}
		@everything = []
		@results = ResponseType.all
		@results.each do |row|
			@everything.push({response_type:row["response_type"], description:row["description"]})
		end
		render :json => JSON.pretty_generate(@everything)
	end

end