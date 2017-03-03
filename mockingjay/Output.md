# Introduction


The output CSV file contains the following fields, and the meaning of each field is provided in the corresponding line too

| Field Name                    | Description                                                               |
|:------------------------------|:--------------------------------------------------------------------------|
|      "uid",                   | Real User ID of each twitter user                                         |
|      "username:firstName",    | First Name. By separating name field in Twitter user.   See [NameExtractor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/NameExtractor.scala) for details                    | 
|      "username:lastName",     | Last Name. By separating name field in Twitter user.    See [NameExtractor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/NameExtractor.scala) for details                    | 
|      "Gender:gender",         | The gender decided by Facebook data or SSO data. Facebook data is used at the first priority. But if facebook data cannot decide, we use SSO data. See [AgeGenderPredictor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/AgeGenderPredictor.scala) for details.                                                                          | 
|      "Gender:female_prob",    | Probability of this user to be female.                                                                          |   
|      "Gender:male_prob",      | Probability of this user to be male.                                                                          | 
|      "Gender:year_prob",      | When source = SSO,  $(gender)_prob = year_prob. $(!$gender)_prob = 1-year_prob. When source = FB, ignore year_prob.See [AgeGenderPredictor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/AgeGenderPredictor.scala) for details.                                                                          | 
|      "Gender:source",         | When source = SSO,  $(gender)_prob = year_prob. $(!$gender)_prob = 1-year_prob. When source = FB, ignore year_prob.See [AgeGenderPredictor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/AgeGenderPredictor.scala) for details.                                                                          | 
|      "Age:prob",              | Probability of this user to be born in $(year)                                                                          | 
|      "Age:year",              | See [AgeGenderPredictor.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/AgeGenderPredictor.scala) for details.                                                                          | 
|      "political:sumblue",     | Score of a user having the tendency to support blue  See [UserSentiment.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/UserSentiment.scala) for details                                                                          | 
|      "political:sumred",      | Score of a user having the tendency to support red  See [UserSentiment.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/UserSentiment.scala) for details                                                                          | 
|      "political:type",        | Final decision of a user being blue-supported or red-supported.                                                                          | 
|      "location:Tract_geoid",  | The GEOID10 in the original tract-level census data                         | 
|      "location:Tract_name",   | The NAME10 in the original tract-level census data                         | 
|      "location:tract",        | Location coordinate  "[x,y]"                                                                          | 
|      "Coord:tract_WKT",       | WKT format user coordinate "POINT(x y)"     See [ResidencyLocator.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/ResidencyLocator.scala) [LocationFillInBlank.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/LocationFillInBlank.scala)  [UserProbabilityWithCSV.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/RaceProbabilityWithCSV.scala)  [CSVExporter.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/exporter/csv/CSVExporter.scala) for details  | 
|      "Coord:tract_x",         | x-coordinate                                                                          | 
|      "Coord:tract_y",         | y-coordinate                                                                          | 
|      "Race_Tract:max_race",   | Track-level race decision with maximum race probability      See [ResidencyLocator.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/ResidencyLocator.scala) [LocationFillInBlank.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/LocationFillInBlank.scala)  [UserProbabilityWithCSV.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/twitteruser/RaceProbabilityWithCSV.scala)  [CSVExporter.scala](src/main/scala/x/spirit/dynamicjob/mockingjay/exporter/csv/CSVExporter.scala) for details  | 
|      "Race_Tract:pct2prace",  | Percent Non-Hispanic of Two or More Races                                                                          | 
|      "Race_Tract:pctaian",    | Percent Non-Hispanic American Indian and Alaskan Native Only                                                                          | 
|      "Race_Tract:pctapi",     | Percent Non-Hispanic Asian and Pacific Islander Only                                                                          | 
|      "Race_Tract:pctblack",   | Percent Non-Hispanic Black Only           |
|      "Race_Tract:pcthispanic",| Percent Non-Hispanic Hispanic Only           | 
|      "Race_Tract:pctwhite",   | Percent Hispanic Origin           | 
|      "location:County_geoid", |                                                                           | 
|      "location:County_name",  |                                                                           | 
|      "location:county",       |                                                                           | 
|      "Coord:county_WKT",      |                                                                           | 
|      "Coord:county_x",        |                                                                           | 
|      "Coord:county_y",        |                                                                           | 
|      "Race_County:max_race",  |                                                                           | 
|      "Race_County:pct2prace", |                                                                           | 
|      "Race_County:pctaian",   |                                                                           | 
|      "Race_County:pctapi",    |                                                                           | 
|      "Race_County:pctblack",  |                                                                           | 
|     "Race_County:pcthispanic",|                                                                           | 
|      "Race_County:pctwhite",  |                                                                           | 
|      "location:State_geoid",  |                                                                           | 
|      "location:State_name",   |                                                                           | 
|      "location:state",        |                                                                           | 
|      "Coord:state_WKT",       |                                                                           | 
|      "Coord:state_x",         |                                                                           | 
|      "Coord:state_y",         |                                                                           | 
|      "Race_State:max_race",   |                                                                           | 
|      "Race_State:pct2prace",  |                                                                           |      
|      "Race_State:pctaian",    |                                                                           |      
|      "Race_State:pctapi",     |                                                                           | 
|      "Race_State:pctblack",   |                                                                           | 
|      "Race_State:pcthispanic",|                                                                           | 
|      "Race_State:pctwhite"    |                                                                           | 
