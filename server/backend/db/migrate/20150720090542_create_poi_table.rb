class CreatePoiTable < ActiveRecord::Migration
  def change
    create_table :poi do |t|
    end
    add_column :poi, :title, :string
    add_column :poi, :latitude, :decimal
    add_column :poi, :longitude, :decimal
    add_column :poi, :altitude, :decimal
    add_column :poi, :count, :decimal, :default => 1

    create_function_get_distance_file = File.join(Rails.root, "db", "sqlscripts", "function_get_distance.sql")
    create_function_get_distance = File.read(create_function_get_distance_file)
    execute create_function_get_distance
  end
end
