# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check DocumentsManager Rest Api Response

  Scenario: DocumentsManager CRUD Operations

    Given header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    # ---- Add entity fields here -----
    And multipart file documentContent = { read: 'classpath:upload/testFile.txt', filename: 'testFile.txt', contentType: 'text/plain' }
    And multipart field data = { "path": "/myPath","fileName": "prova.txt", "uid": "234123123", "contentType": "text/plain"}
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
        "contentType":"text/plain"
       }
    """
    * def entityId = response.id

    # --------------- FETCH FILE -------------------------

    Given url serviceBaseUrl+'/water/documents/content?path=/myPath&fileName=prova.txt'
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match header Content-Type contains 'text/plain'
    And match responseBytes != null
    * def expected = read('classpath:upload/testFile.txt')
    * def downloadedText = new java.lang.String(responseBytes)
    Then status 200
    And match downloadedText == expected

    # --------------- FETCH FILE BY ID-------------------------

    Given url serviceBaseUrl+'/water/documents/content/id/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match header Content-Type contains 'text/plain'
    And match responseBytes != null
    * def expected = read('classpath:upload/testFile.txt')
    * def downloadedText = new java.lang.String(responseBytes)
    Then status 200
    And match downloadedText == expected

    # --------------- FETCH FILE BY UID-------------------------

    Given url serviceBaseUrl+'/water/documents/content/uid/234123123'
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match header Content-Type contains 'text/plain'
    And match responseBytes != null
    * def expected = read('classpath:upload/testFile.txt')
    * def downloadedText = new java.lang.String(responseBytes)
    Then status 200
    And match downloadedText == expected
    
    # --------------- UPDATE -----------------------------

    Given header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    # ---- Add entity fields here ----
    And multipart file documentContent = { read: 'classpath:upload/testFile.txt', filename: 'testFile.txt', contentType: 'text/plain' }
    And multipart field data = { "id":"#(entityId)","path": "/myPath","fileName": "prova1.txt", "uid": "234123123", "contentType": "text/plain"}
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
      "contentType":"text/plain"
    }
    """

    # --------------- UPDATE with no content-----------------------------

    Given header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents'
    # ---- Add entity fields here ----
    And multipart field data = { "id":"#(entityId)","entityVersion":2,"path": "/myPath","fileName": "prova1.txt", "uid": "234123123", "contentType": "text/plain"}
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response ==
      """
    { "id": #number,
    "entityVersion":3,
    "entityCreateDate":'#number',
    "entityModifyDate":'#number',
    "path": '/myPath',
    "fileName": 'prova1.txt',
    "uid":"234123123",
    "contentType":"text/plain"
    }
    """
    # --------------- FETCH AGAIN FILE WITH NEW NAME -------------------------

    Given url serviceBaseUrl+'/water/documents/content?path=/myPath&fileName=prova1.txt'
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match header Content-Type contains 'text/plain'
    And match responseBytes != null
    * def expected = read('classpath:upload/testFile.txt')
    * def downloadedText = new java.lang.String(responseBytes)
    Then status 200
    And match downloadedText == expected

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
      "entityVersion":3,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "path": '/myPath',
      "fileName": 'prova1.txt',
      "uid":"234123123",
      "contentType":"text/plain"
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
      "entityVersion":3,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "path": '/myPath',
      "fileName": 'prova1.txt',
      "uid":"234123123",
      "contentType":"text/plain"
      }
    """
  
  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/documents/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
