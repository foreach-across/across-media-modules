Feature: Load remote image
  I want to be able to load remote images by providing the url and (optionally) a key.

  Background: Available test environment and remote locations
    Given the test environment is up and running
    And there is a source image on "http://www.foreach.be/original"
    And there is a source image on "http://www.foreach.be"

  Scenario: Try to load image that does not exist
    Given I load image with url "http://www.foreach.be/blablabla"
    Then the status code should be "404"

  Scenario: Load jpeg image
    Given I load image with url "http://www.foreach.be"
    Then the status code should be "200"
    And the content-type should be "image/jpeg"
    And the image returned should be "test-images/jpeg/original.jpg"

  Scenario: Load png image
    Given I load image with url "http://www.foreach.be"
    Then the status code should be "200"
    And the content-type should be "image/png"
    And the image returned should be "test-images/png/original.png"

  Scenario: Load gif image
    Given I load image with url "http://www.foreach.be"
    Then the status code should be "200"
    And the content-type should be "image/gif"
    And the image returned should be "test-images/gif/original.gif"


