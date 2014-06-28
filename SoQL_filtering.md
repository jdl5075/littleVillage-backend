For Little Village [Community_Area=30]

City of Chicago crime data
    https://data.cityofchicago.org/resource/ijzp-q8t2.json?community_area=30

    https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,description&community_area=30
  
  Five Categories:
    *Make an individual call for each crime type, altering the 'primary_type=xxxxxxxx' each time
    Violent Crime
      -Homicide
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=homicide
      -Assault  
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=assault
      -Battery  
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=battery
      -Crim sexual assault  
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=crim sexual assault
      -Arson  
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=arson
      -Weapons violation
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=weapons violation
        
    Property Crime
      -Burglary
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=burglary
      -Theft
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=theft
      -Robbery
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=robbery
      -Motor vehicle theft
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=motor vehicle theft
      -Criminal damage
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=criminal damage
      -Criminal trespass
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=criminal trespass
    
    Sin Crime
      -Narcotics
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=narcotics
      -Liquor law violation
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=liquor law violation
      -Prostitution
        https://data.cityofchicago.org/resource/ijzp-q8t2.json?$select=primary_type,date,description,location_description,latitude,longitude,block,arrest,community_area&community_area=30&primary_type=prostitution
    Children
        Offense involving children
        Kidnapping
    Other
        Intimidation
        Other offense
        Public peace violation
        Interference with public officer
        Stalking
        Deceptive practice
    

City of Chicago 311
  Vacant and Abandoned Buildings
  
    http://data.cityofchicago.org/resource/7nii-7srd.json?$select=service_request_type,date_service_request_was_received,community_area,latitude,longitude,address_street_name,address_street_suffix,address_street_number&community_area=30
   
    "service_request_type" : "Vacant/Abandoned Building",
    "address_street_name" : "HOMAN",
    "longitude" : "-87.70981064044761",
    "community_area" : "30",
    "latitude" : "41.84077990677132",
    "address_street_suffix" : "AVE",
    "date_service_request_was_received" : "2010-04-23T00:00:00",
    "address_street_number" : "2801"
    
        
  Tree Trims
    
    http://data.cityofchicago.org/resource/uxic-zsuj.json?$select=creation_date,status,completion_date,community_area,latitude,longitude&community_area=30
    
    "creation_date" : "2011-06-15T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.72420678334811",
    "community_area" : "30",
    "latitude" : "41.83029919833776",
    "completion_date" : "2011-06-15T00:00:00"
  
  Tree Debris
  
    http://data.cityofchicago.org/resource/mab8-y9h3.json?$select=creation_date,status,completion_date,community_area,latitude,longitude&community_area=30

    "creation_date" : "2011-01-07T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.73140677082365",
    "community_area" : "30",
    "latitude" : "41.845071684560445",
    "completion_date" : "2011-01-07T00:00:00"

  Sanitation Code Complaints
  
    http://data.cityofchicago.org/resource/me59-5fac.json?$select=creation_date,status,what_is_the_nature_of_this_code_violation_,community_area,longitude,latitude,street_address&community_area=30
  
    "what_is_the_nature_of_this_code_violation_" : "Dumpster not being emptied",
    "creation_date" : "2012-01-03T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.71004849892743",
    "community_area" : "30",
    "latitude" : "41.84912115679059",
    "street_address" : "2342 S HOMAN AVE"
  
  Pot Holes Reported
  
    http://data.cityofchicago.org/resource/7as2-ds3y.json?$select=creation_date,status,community_area,longitude,latitude&community_area=30

    "creation_date" : "2013-01-02T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.71961298487777",
    "community_area" : "30",
    "latitude" : "41.836875643608955"
  
  Graffiti Removal
  
    http://data.cityofchicago.org/resource/hec5-y4x5.json?$select=where_is_the_graffiti_located_,creation_date,what_type_of_surface_is_the_graffiti_on_,status,community_area,longitude,latitude,street_address&community_area=30

    "creation_date" : "2012-01-01T00:00:00",
    "what_type_of_surface_is_the_graffiti_on_" : "Brick - Painted",
    "status" : "Completed",
    "where_is_the_graffiti_located_" : "Garage",
    "longitude" : "-87.72201701573638",
    "community_area" : "30",
    "latitude" : "41.846216243614116",
    "street_address" : "2459 S SPRINGFIELD AVE"
  
  Garbage Cart Black Maintenance/Replacement
  
    http://data.cityofchicago.org/resource/9ksk-na4q.json?$select=status,creation_date,community_area,latitude,longitude,street_address&community_area=30
    
    "creation_date" : "2012-06-16T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.72564384764607",
    "community_area" : "30",
    "latitude" : "41.8338515435035",
    "street_address" : "3242 S KOMENSKY AVE"
  
  Rodent Baiting/Rat Complaint
  
    http://data.cityofchicago.org/resource/97t6-zrhs.json?$select=status,creation_date,community_area,latitude,longitude,street_address&community_area=30
    
    "creation_date" : "2011-01-03T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.71845202641974",
    "community_area" : "30",
    "latitude" : "41.84431125492574",
    "street_address" : "2601 S RIDGEWAY AVE"
  
  Abandoned Vehicles
  
    http://data.cityofchicago.org/resource/3c9v-pnva.json?$select=status,creation_date,completion_date,community_area,latitude,longitude,vehicle_make_model,vehicle_color,how_many_days_has_the_vehicle_been_reported_as_parked_&community_area=30
    
    "how_many_days_has_the_vehicle_been_reported_as_parked_" : "30",
    "creation_date" : "2013-01-02T00:00:00",
    "vehicle_color" : "Yellow",
    "vehicle_make_model" : "Chevrolet",
    "status" : "Completed",
    "longitude" : "-87.7295751853971",
    "community_area" : "30",
    "latitude" : "41.844609373231194",
    "completion_date" : "2013-02-13T00:00:00"
  
  Street Lights - All Out
  
    https://data.cityofchicago.org/resource/zuxi-7xem.json?$select=status,creation_date,completion_date,community_area,latitude,longitude,street_address&community_area=30
  
    "creation_date" : "2012-01-01T00:00:00",
    "status" : "Completed - Dup",
    "longitude" : "-87.70994316624493",
    "community_area" : "30",
    "latitude" : "41.845657614069985",
    "completion_date" : "2012-01-03T00:00:00",
    "street_address" : "2533 S HOMAN AVE"
  
  Alley Lights Out
  
    http://data.cityofchicago.org/resource/t28b-ys7j.json?$select=status,creation_date,completion_date,community_area,latitude,longitude,street_address&community_area=30

    "creation_date" : "2012-01-03T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.69816867357372",
    "community_area" : "30",
    "latitude" : "41.85186526814943",
    "completion_date" : "2012-01-06T00:00:00",
    "street_address" : "2874 W CERMAK RD"
  
  Street Lights - One Out

    http://data.cityofchicago.org/resource/3aav-uy2v.json?$select=status,creation_date,completion_date,community_area,latitude,longitude,street_address&community_area=30
    
    "creation_date" : "2012-01-10T00:00:00",
    "status" : "Completed",
    "longitude" : "-87.70981119588826",
    "community_area" : "30",
    "latitude" : "41.84079829637197",
    "completion_date" : "2012-01-25T00:00:00",
    "street_address" : "2800 S HOMAN AVE"


