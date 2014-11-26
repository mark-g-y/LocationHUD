#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

from google.appengine.ext import webapp
import urllib2

url = "http://ec2-54-148-173-7.us-west-2.compute.amazonaws.com:3000/poi_api/"

class Main(webapp.RequestHandler):
	def get(self):
		latitude = self.request.get("latitude")
		longitude = self.request.get("longitude")
		response =  urllib2.urlopen(url + "?latitude=" + latitude + "&longitude=" + longitude)
		self.response.out.write(response.read())

	def post(self):
		name = self.request.get("name")
		self.response.out.write('wooo')
		
application = webapp.WSGIApplication([
    ('/', Main),
], debug=True)
