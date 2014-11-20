
require 'json'

class PoiApiController < ApplicationController 

	def index
		@lat = params[:latitude].to_i
		@long = params[:longitude].to_i
		obj = {latitude:@lat, longitude:@long}
		render :json => JSON.pretty_generate(obj)
	end

end