# Sunlight hours

A new feature has been requested for our room listings in future Barcelona: we want to display
the sunlight hours that a given apartment receives in a day. To ensure our announced sunlight
hours will be always true, we decided to display the sunlight hours during the Winter Solstice.
Hence you can do all your calculations assuming December 22nd sunlight hours, ie sunlight
hours will be between 08:14 and 17:25.

In this amazing version of future Barcelona, one can safely assume:

* Buildings are distributed in neighbourhoods,
* In those neighbourhoods, the buildings are always aligned east to west,
* The sun rises in the east and travels at a constant radial speed until setting,
* The only shadows created in a neighbourhood are artefacts of the buildings in it,
* We consider an apartment receives sunlight when either its eastern or western exterior
wall is fully covered in sunlight and/or when the sun is directly overhead,
* There is only one apartment per floor; in a building with N floors they are numbered from
0 to N-1.

![alt text](https://github.com/amar1n/SunlightHours/raw/master/city.png "City")


API
Your program should have two APIs defined:
* **init** method that takes a String containing a JSON describing the city, with this format:

````
[
    {
        neighborhood: <name_string>,
        apartments_height: <number>,
        buildings:  [
                        {
                            name: <name_string>,
                            apartments_count: <number>,
                            distance: <number>
                        }
                    ]
    }
]
````

Assume the building list is ordered from east to west.

* **getSunlightHours** method which takes a neighbourhood name, building name, and
apartment number. It returns the sunlight hours as a string like “hh:mm:ss - hh:mm:ss” in
24hr format.

Assume init is only going to be called once, however, getSunlightHours will be called very
frequently.

Please provide a working solution, and justify any limitations. Don’t be afraid to go above and
beyond!

---
---
---

# Solution

The solution was implemented by developing a Serverless API With DynamoDB, AWS Lambda, and API Gateway

![alt text](https://github.com/amar1n/SunlightHours/raw/master/SunlightHours.png "Solution")

#### The technologies used were...
* AWS Lambdas for running Java 8 code without provisioning servers
* AWS API Gateway, API Keys and Usage Plans for Serverless REST API
* AWS IAM to protect access to AWS services and resources used by the Lambda functions
* AWS DynamoDB for a managed NoSQL database
* AWS Route 53 to set up a custom domain name for the REST API
* JSON Schema Validator from everit-org for validation of input data
* Gson to serialize and deserialize Java objects to JSON
* Java 8 multithreading for calculating the sunlight ranges
* Java 8 Executor framework for the parallel processing of tasks such as the creation and deletion of NoSQL tables and 
to perform the calculations of the sunlight ranges in each neighborhood
* Maven and IntelliJ Idea

#### When an **init** request is received...
* Input data is validated
* It is verified that there is no other **init** process in progress
* Access to subsequent **init** calls is blocked
* A Lambda function (**calculateSunlightHours**) is invoked asynchronously to perform the calculations of the sunlight hours in the indicated 
neighborhoods
* The request is answered with OK

#### When **calculateSunlightHours** is invoked...
* All tables used by the **_old city_** are deleted in parallel
* For each neighborhood the starting and ending ranges are calculated separately. All this is done in parallel (by neighborhood, start and end)
* All tables used by the **_new city_** are created in parallel

#### When a request for **getSunlightHours** is received...
* Input data is validated
* It is verified that the sunlight range calculation process has finished
* Sunlight range is searched
* The request is answered with the sunlight range

#### Examples of calls to endpoints...

* **_init_**

curl --location --request POST 'https://badi.albertomarin.info/challenge/init' \
--header 'Content-Type: application/json' \
--header 'x-api-key: ---ASK-TO-ALBERTO-MARIN---' \
--data-raw '[
  {
    "neighborhood": "Santa Mónica",
    "apartments_height": 2,
    "buildings": [
      {
        "name": "Edif. 1",
        "apartments_count": 8,
        "distance": 15.4
      }
    ]
  }
]'

* **_getSunlightHours_**

curl --location --request GET 'https://badi.albertomarin.info/challenge/getsunlighthours?neighborhood_name=POBLENOU&building_name=01&apartment_number=4' \
--header 'x-api-key: ---ASK-TO-ALBERTO-MARIN---'