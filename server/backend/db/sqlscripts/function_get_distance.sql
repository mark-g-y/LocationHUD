
drop function if exists get_distance(double precision, double precision, double precision, double precision);

create function get_distance(lat1 double precision, long1 double precision, lat2 double precision, long2 double precision)
returns double precision language plpgsql as 
$$
declare
    a double precision;
begin
    lat1 := lat1 * pi() / 180;
    lat2 := lat2 * pi() / 180;
    long1 := long1 * pi() / 180;
    long2 := long2 * pi() / 180;
    a := pow(sin((lat1-lat2)/2),2)+cos(lat1)*cos(lat2)*pow(sin(long1-long2),2);
    return 2 * atan2(sqrt(a),sqrt(1-a)) * 6371;
end;
$$