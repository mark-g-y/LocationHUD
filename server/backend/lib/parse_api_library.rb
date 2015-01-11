
require "parse_api_config"
require 'net/http'
require 'uri'

class ParseApiLibrary
  
  def self.add_authentication_to_request(request)
    request.add_field(ParseApiConfig::APPLICATION_ID_HEADER, ParseApiConfig::APPLICATION_ID_VALUE)
    request.add_field(ParseApiConfig::APPLICATION_REST_API_KEY_HEADER, ParseApiConfig::APPLICATION_REST_API_KEY_KEY)
  end
  
  def self.init_get_request(url)
    request = Net::HTTP::Get.new(url.to_s)
    ParseApiLibrary.add_authentication_to_request(request)
    return request
  end
  
  def self.init_post_request(url)
    request = Net::HTTP::Post.new(url.to_s, initheader = {'Content-Type' =>'application/json'})
    ParseApiLibrary.add_authentication_to_request(request)
    return request
  end
  
  def self.init_put_request(url)
    request = Net::HTTP::Put.new(url.to_s,  initheader = {"Content-Type" => "application/json"})
    ParseApiLibrary.add_authentication_to_request(request)
    return request
  end
  
  def self.parse_response(response, keyword)
    # parse the response object - by default, Parse returns the response as a value under the "result" or "results" key
    response_obj = JSON.parse(response.body)
    response_obj = response_obj[keyword]
    if response_obj.is_a?(String)
      return JSON.load(response_obj)
    end
    return response_obj
  end
  
  def self.send_http_request(url, request, use_ssl=false)
    http = Net::HTTP.new(url.host, url.port)
    http.use_ssl = use_ssl
    return http.request(request)
  end
  
end
