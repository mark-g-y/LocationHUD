
import json

def clean_input(lines) :
	cleaned_lines = []
	for line in lines :
		line = line.strip().replace("\n", "")
		cleaned_lines.append(line)
	return cleaned_lines
	
def read_white_space(lines, i) :
	while i < len(lines) and (lines[i].isspace() or lines[i] == "") :
		i = i + 1
	return i
	
poi_data_length = 4;
new_list_prefix = "/|\\"

f = open("input.txt", "r")
lines = clean_input(f.readlines())
f.close()

poi_lists = []

i = 0
while i < len(lines) :
	if len(lines[i]) >= 3 and lines[i][0:3] == new_list_prefix :
		poi_list_obj = {}
		poi_list_obj["list"] = lines[i][3:]
		i = i + 1
		poi_list = []
		
		while i < len(lines) and len(lines[i]) >= 3 and lines[i][0:3] != new_list_prefix :
			poi = {}
			poi["title"] = lines[i]
			poi["latitude"] = lines[i + 1]
			poi["longitude"] = lines[i + 2]
			poi["altitude"] = lines[i + 3]
			poi_list.append(poi)
			i = i + 4
			i = read_white_space(lines, i)
		poi_list_obj["poi_list"] = poi_list
		poi_lists.append(poi_list_obj)
	else :
		i = i + 1
		
f = open("provided_pois.txt", "w")
f.write(json.dumps(poi_lists, sort_keys=True, indent=4, separators=(',', ': ')))
f.close()
			
			
			
			
			
			