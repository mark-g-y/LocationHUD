
require 'json'
require 'clustering'
require 'request_history'

class PoiApiController < ApplicationController

	protect_from_forgery with: :null_session

	@@MIN_DEGREE_DIFF = 3
	@@MIN_DISTANCE = 200
	@@SAME_LOCATION_TOLERANCE_DISTANCE = 0.25
	@@STRING_DIFFERENCE_THRESHOLD = 3
	@@POI_RESULT_LIMIT = 10
	
	@@counter = 0

	# GET request
	def index 
		lat = params[:latitude].to_f
		long = params[:longitude].to_f
		
		nearby_locations = Poi.select('*, get_distance(latitude, longitude, %f, %f) as distance' % [Poi.sanitize(lat), Poi.sanitize(long)])
			.where('get_distance(latitude, longitude, ?, ?) < ?', lat, long, @@MIN_DISTANCE)
			.order('distance asc')
		
		clustering = Clustering.new(nearby_locations)
		nearby_locations = clustering.get_clusters_as_models()
		
		location_list = []
		if not nearby_locations.nil?
			nearby_locations.each do |row|
				location_list.push(generate_location_json_from_db_row(row))
			end
		end
		render :json => JSON.pretty_generate(location_list)
	end
	
	#POST request
	def create
	
		user = params[:user]
		
		if is_spam(user, request.remote_ip)
			message = {}
			message['status'] = 'failure'
			render :json => JSON.pretty_generate([message])
			return
		end
				
		poi_list = JSON.parse(request.body.read)
		
		for poi in poi_list
			name = poi['name']
			lat = poi['latitude']
			long = poi['longitude']
			altitude = poi['altitude']
			results = Poi.where(name: name, latitude: lat, longitude: long, altitude: altitude)
			if results.count() > 0
				result = results.first
				Poi.update(result['id'], number: result['number'] + 1)
			else
				Poi.create(name: name, latitude: lat, longitude: long, altitude: altitude)
			end
			
			# for keeping track of stuff, if we ever need to access the raw data instead of the condensed version
			# commented out for now to avoid spamming my server
			#PoiRaw.create(name: name, latitude: lat, longitude: long, altitude: altitude, time_uploaded: Time.now)
		end
		
		message = {}
		message['status'] = 'success'
		render :json => JSON.pretty_generate([message])

	end
	
	def is_spam(user, ip)
	
		# spam algorithm
		# 1) check if IP has been uploading a lot of stuff lately
		# 2) check if IP has been uploading a lot of similar stuff lately
		# 2) check for keywords in content (i.e. name of POI in this case) - will add this feature in later, since the first two will stop any spam from making it to the top of the list
		puts('Is spam ' + RequestHistory.is_ip_spam(ip).to_s)
		RequestHistory.add()
		puts('Current count is ' + RequestHistory.get().to_s)
		return true
	end
	
	def get_best_match_by_name(name, potential_locations)
		potential_locations.each do |row|
			if do_names_match(name, row['name'])
				return row
			end
		end
		return nil
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