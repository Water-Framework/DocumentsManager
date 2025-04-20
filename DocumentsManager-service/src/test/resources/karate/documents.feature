# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check DocumentsManager Rest Api Response

  Scenario: DocumentsManager CRUD Operations

    Given header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    # ---- Add entity fields here -----
    And multipart file documentContent = { read: 'classpath:upload/testFile.txt', filename: 'testFile.txt', contentType: 'application/txt' }
    And multipart field data = { "path": "/myPath","fileName": "prova.txt", "uid": "234123123", "contentType": "application/text"}
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":1,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "path": '/myPath',
        "fileName": 'prova.txt',
        "uid":"234123123",
        "contentType":"application/text"
       }
    """
    * def entityId = response.id
    
    # --------------- UPDATE -----------------------------

    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    # ---- Add entity fields here ----
    And multipart file documentContent = { read: 'classpath:upload/testFile.txt', filename: 'testFile.txt', contentType: 'application/txt' }
    And multipart field data = { "id":"#(entityId)","path": "/myPath","fileName": "prova1.txt", "uid": "234123123", "contentType": "application/text"}
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
    { "id": #number,
      "entityVersion":2,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "path": '/myPath',
      "fileName": 'prova1.txt',
      "uid":"234123123",
      "contentType":"application/text"
    }
    """
  
  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
    { "id": #number,
      "entityVersion":2,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "path": '/myPath',
      "fileName": 'prova1.txt',
      "uid":"234123123",
      "contentType":"application/text"
    }
    """
    
  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    When method GET
    Then status 200
    And match response.results contains
    """
      { "id": #number,
      "entityVersion":2,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "path": '/myPath',
      "fileName": 'prova1.txt',
      "uid":"234123123",
      "contentType":"application/text"
      }
    """
  
  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
