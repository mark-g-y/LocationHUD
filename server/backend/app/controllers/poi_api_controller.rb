
require 'json'
require 'clustering'
require 'request_history'
require 'parse_api_config'
require 'net/http'
require 'uri'

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
		lat = params[:latitude].to_s.to_f
		long = params[:longitude].to_s.to_f
		
		url = URI.parse("https://api.parse.com/1/functions/nearby_locations_within_range")
		request = Net::HTTP::Post.new(url.to_s, initheader = {'Content-Type' =>'application/json'})
		request.add_field(ParseApiConfig::APPLICATION_ID_HEADER, ParseApiConfig::APPLICATION_ID_VALUE)
		request.add_field(ParseApiConfig::APPLICATION_REST_API_KEY_HEADER, ParseApiConfig::APPLICATION_REST_API_KEY_KEY)
		request.body = JSON.dump({"latitude" => lat, "longitude" => long})
		
		http = Net::HTTP.new(url.host, url.port)
		http.use_ssl = true
		response = http.request(request)
		
		# parse the response object - by default, Parse returns the response as a value under the "result" key
		response_obj = JSON.parse(response.body)
    response_obj = response_obj["result"]
		nearby_locations = JSON.load(response_obj)
		
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
	
		for header in request.headers
			puts(header)
		end
		ip = request.headers['IP']
		user = params[:user]
		
		if is_spam(ip)
			message = {}
			message['status'] = 'failure'
			render :json => JSON.pretty_generate([message])
			return
		end
				
		poi_list = JSON.parse(request.body.read)
		
		for poi in poi_list
			name = poi['title']
			lat = poi['latitude']
			long = poi['longitude']
			altitude = poi['altitude']
			
			if RequestHistory.is_similar_location(ip, lat, long)
				puts('Is similar ' + name)
				next
			else
				puts('Is NOT similar ' + name)
			end
			
			url = URI.parse("https://api.parse.com/1/classes/Location")
			url.query = URI.encode_www_form("where" => JSON.dump({"name" => name, "latlong" => {
         "$nearSphere" => {
           "__type" => "GeoPoint",
           "latitude" => lat,
           "longitude" => long
         },
         "$maxDistanceInKilometers" => 1
       }, "altitude" => altitude}))

      request = Net::HTTP::Get.new(url.to_s)
      request.add_field(ParseApiConfig::APPLICATION_ID_HEADER, ParseApiConfig::APPLICATION_ID_VALUE)
      request.add_field(ParseApiConfig::APPLICATION_REST_API_KEY_HEADER, ParseApiConfig::APPLICATION_REST_API_KEY_KEY)

      http = Net::HTTP.new(url.host, url.port)
      http.use_ssl = true
      response = http.request(request)
      
      # parse the response object - by default, Parse returns the response as a value under the "result" key
      response_obj = JSON.parse(response.body)
      response_obj = response_obj["results"]
      locations = response_obj
  	  
			if locations.count() > 0
				location = locations.first
				url = URI.parse("https://api.parse.com/1/classes/Location/%s/" % [location["objectId"]])
        request = Net::HTTP::Put.new(url.to_s,  initheader = {"Content-Type" => "application/json"})
        request.add_field(ParseApiConfig::APPLICATION_ID_HEADER, ParseApiConfig::APPLICATION_ID_VALUE)
        request.add_field(ParseApiConfig::APPLICATION_REST_API_KEY_HEADER, ParseApiConfig::APPLICATION_REST_API_KEY_KEY)
        request.body = JSON.dump({"count" => location["count"] + 1})
  
        http = Net::HTTP.new(url.host, url.port)
        http.use_ssl = true
        response = http.request(request)
			else
			  puts "Should create"
				# Poi.create(name: name, latitude: lat, longitude: long, altitude: altitude)
				url = URI.parse("https://api.parse.com/1/classes/Location/")
				request = Net::HTTP::Post.new(url.to_s, initheader = {"Content-Type" => "application/json"})
				request.add_field(ParseApiConfig::APPLICATION_ID_HEADER, ParseApiConfig::APPLICATION_ID_VALUE)
        request.add_field(ParseApiConfig::APPLICATION_REST_API_KEY_HEADER, ParseApiConfig::APPLICATION_REST_API_KEY_KEY)
        request.body = JSON.dump({"name" => name, "latlong" => {"__type" => "GeoPoint", "latitude" => lat, "longitude" => long}, "altitude" => altitude, "count" => 1})
        
        http = Net::HTTP.new(url.host, url.port)
        http.use_ssl = true
        response = http.request(request)
			end
			
		end
		
		message = {}
		message['status'] = 'success'
		render :json => JSON.pretty_generate([message])

	end
	
	def is_spam(ip)
	
		# spam algorithm
		# 1) check if IP has been uploading a lot of stuff lately
		# 2) check if IP has been uploading a lot of similar stuff lately
		# 2) check for keywords in content (i.e. name of POI in this case) - will add this feature in later, since the first two will stop any spam from making it to the top of the list
		#puts('Is spam ' + RequestHistory.is_ip_spam(ip).to_s)
		#RequestHistory.add()
		#puts('Current count is ' + RequestHistory.get().to_s)
		return RequestHistory.is_ip_spam(ip)
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
		obj['title'] = row['title']
		obj['latitude'] = row['latitude']
		obj['longitude'] = row['longitude']
		obj['altitude'] = row['altitude']
		obj['distance'] = row['distance']
		return obj
	end

end