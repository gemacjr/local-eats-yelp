-- Seed data: named areas (simplified bounding polygons) and sample businesses.

INSERT INTO locations (name, slug, type, boundary) VALUES
  ('San Francisco', 'san-francisco', 'CITY', ST_GeogFromText('SRID=4326;POLYGON((-122.5155 37.708, -122.357 37.708, -122.357 37.812, -122.5155 37.812, -122.5155 37.708))')),
  ('Mission District', 'sf-mission', 'NEIGHBORHOOD', ST_GeogFromText('SRID=4326;POLYGON((-122.426 37.748, -122.405 37.748, -122.405 37.77, -122.426 37.77, -122.426 37.748))')),
  ('SoMa', 'sf-soma', 'NEIGHBORHOOD', ST_GeogFromText('SRID=4326;POLYGON((-122.412 37.77, -122.387 37.77, -122.387 37.788, -122.412 37.788, -122.412 37.77))')),
  ('North Beach', 'sf-north-beach', 'NEIGHBORHOOD', ST_GeogFromText('SRID=4326;POLYGON((-122.415 37.796, -122.4 37.796, -122.4 37.807, -122.415 37.807, -122.415 37.796))')),
  ('New York', 'new-york', 'CITY', ST_GeogFromText('SRID=4326;POLYGON((-74.03 40.68, -73.9 40.68, -73.9 40.88, -74.03 40.88, -74.03 40.68))')),
  ('Greenwich Village', 'nyc-greenwich-village', 'NEIGHBORHOOD', ST_GeogFromText('SRID=4326;POLYGON((-74.01 40.727, -73.99 40.727, -73.99 40.738, -74.01 40.738, -74.01 40.727))')),
  ('Midtown', 'nyc-midtown', 'NEIGHBORHOOD', ST_GeogFromText('SRID=4326;POLYGON((-73.995 40.748, -73.97 40.748, -73.97 40.765, -73.995 40.765, -73.995 40.748))'));

INSERT INTO businesses (name, description, category, address, latitude, longitude) VALUES
  ('La Taqueria', 'Legendary Mission-style burritos and tacos.', 'Mexican', '2889 Mission St, San Francisco, CA', 37.7509, -122.4181),
  ('Tartine Bakery', 'Morning buns, country bread and pastries.', 'Bakery', '600 Guerrero St, San Francisco, CA', 37.7614, -122.4241),
  ('Foreign Cinema', 'Californian-Mediterranean dining with films in the courtyard.', 'American', '2534 Mission St, San Francisco, CA', 37.7563, -122.4193),
  ('Ritual Coffee Roasters', 'Third-wave coffee roasted in house.', 'Coffee', '1026 Valencia St, San Francisco, CA', 37.7565, -122.4213),
  ('Zeitgeist', 'Beer garden with a huge outdoor patio.', 'Bar', '199 Valencia St, San Francisco, CA', 37.77, -122.4221),
  ('Deli Board', 'Oversized deli sandwiches.', 'Sandwiches', '1058 Folsom St, San Francisco, CA', 37.7775, -122.4077),
  ('Sightglass Coffee', 'Coffee bar and roastery in a warehouse space.', 'Coffee', '270 7th St, San Francisco, CA', 37.7771, -122.4085),
  ('Marlowe', 'Brussels sprout chips and a famous burger.', 'American', '500 Brannan St, San Francisco, CA', 37.7784, -122.3965),
  ('Tony''s Pizza Napoletana', 'Award-winning pizza in many regional styles.', 'Pizza', '1570 Stockton St, San Francisco, CA', 37.8003, -122.409),
  ('Molinari Delicatessen', 'Old-school Italian deli since 1896.', 'Sandwiches', '373 Columbus Ave, San Francisco, CA', 37.799, -122.4077),
  ('Caffe Trieste', 'Historic espresso house and poets'' hangout.', 'Coffee', '601 Vallejo St, San Francisco, CA', 37.7986, -122.4075),
  ('Golden Boy Pizza', 'Focaccia-style square slices.', 'Pizza', '542 Green St, San Francisco, CA', 37.7998, -122.4076),
  ('Swan Oyster Depot', 'Counter-service seafood institution.', 'Seafood', '1517 Polk St, San Francisco, CA', 37.7907, -122.4205),
  ('House of Prime Rib', 'Classic prime rib carved tableside.', 'Steakhouse', '1906 Van Ness Ave, San Francisco, CA', 37.7935, -122.4225),
  ('Burma Superstar', 'Tea leaf salad and Burmese favorites.', 'Asian', '309 Clement St, San Francisco, CA', 37.783, -122.4623),
  ('Joe''s Pizza', 'Classic New York slices since 1975.', 'Pizza', '7 Carmine St, New York, NY', 40.7305, -74.0021),
  ('Katz''s Delicatessen', 'Hand-carved pastrami on rye.', 'Sandwiches', '205 E Houston St, New York, NY', 40.7223, -73.9874),
  ('Blue Hill', 'Farm-to-table tasting menus.', 'American', '75 Washington Pl, New York, NY', 40.7321, -73.9995),
  ('Mamoun''s Falafel', 'Falafel and shawarma since 1971.', 'Middle Eastern', '119 MacDougal St, New York, NY', 40.7302, -74.0003),
  ('Stumptown Coffee', 'Espresso bar in the Ace Hotel lobby.', 'Coffee', '18 W 29th St, New York, NY', 40.7457, -73.9882),
  ('The Halal Guys', 'Chicken and gyro over rice from the famous cart.', 'Middle Eastern', '307 W 53rd St, New York, NY', 40.7632, -73.9848),
  ('Le Bernardin', 'Refined French seafood.', 'Seafood', '155 W 51st St, New York, NY', 40.7615, -73.9818),
  ('Keens Steakhouse', 'Mutton chops and a ceiling full of clay pipes.', 'Steakhouse', '72 W 36th St, New York, NY', 40.7507, -73.9862),
  ('Los Tacos No. 1', 'Adobada and carne asada tacos.', 'Mexican', '229 W 43rd St, New York, NY', 40.7571, -73.9881),
  ('Levain Bakery', 'Giant, gooey cookies.', 'Bakery', '167 W 74th St, New York, NY', 40.78, -73.9803),
  ('Xi''an Famous Foods', 'Hand-pulled noodles and spicy cumin lamb.', 'Asian', '81 St Marks Pl, New York, NY', 40.7275, -73.9853);

-- Precompute business -> location membership (the app does the same on every new business).
INSERT INTO business_locations (business_id, location_id)
SELECT b.id, l.id FROM businesses b JOIN locations l ON ST_Covers(l.boundary, b.geog);
