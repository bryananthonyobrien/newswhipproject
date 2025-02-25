import munit.FunSuite
import java.nio.file.{Files, Paths}

class URLManagerTest extends FunSuite {

  override def beforeEach(context: BeforeEach): Unit = {
    URLManager.clearData() // Reset before each test
  }

  // ===========================
  // Tests in the spec
  // ===========================

  test("1. Adding a URL should store it and update domain stats") {
    val result = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    assert(result.contains("Added"))
    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score\nrte.ie;1;20"
    )
  }

  test(
    "2. Adding another URL from the same domain should update count and score correctly"
  ) {
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    URLManager.addUrl(
      "https://www.rte.ie/news/ulster/2018/1004/1000952-moanghan-mine/",
      30
    )
    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score\nrte.ie;2;50"
    )
  }

  test(
    "3. Adding a URL from a different domain should update the export correctly"
  ) {
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    URLManager.addUrl(
      "https://www.rte.ie/news/ulster/2018/1004/1000952-moanghan-mine/",
      30
    )
    URLManager.addUrl("http://www.bbc.com/news/world-europe-45746837", 10)

    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score\nbbc.com;1;10\nrte.ie;2;50"
    )
  }

  test("4. Removing a URL should correctly update domain stats") {
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    URLManager.addUrl(
      "https://www.rte.ie/news/ulster/2018/1004/1000952-moanghan-mine/",
      30
    )
    URLManager.addUrl("http://www.bbc.com/news/world-europe-45746837", 10)

    val removeResult = URLManager.removeUrl(
      "https://www.rte.ie/news/ulster/2018/1004/1000952-moanghan-mine/"
    )
    assert(removeResult.contains("Removed all instances"))

    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score\nbbc.com;1;10\nrte.ie;1;20"
    )
  }

  // ===========================
  // Duplicate URLS are allowed
  // ===========================

  test("5. Adding duplicate URLs should increment count and score correctly") {
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )

    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score\nrte.ie;2;40"
    )
  }

  test(
    "6. Removing a URL should remove it completely (i.e. any duplicates too)"
  ) {
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )

    val removeResult = URLManager.removeUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/"
    )
    assert(removeResult.contains("Removed all instances"))

    assertEquals(
      URLManager.exportData(),
      "domain;urls;social_score" // Should be empty
    )
  }

  // ==================================================================
  // Make sure Social scores don't underflow and reject negatives
  // ==================================================================

  test("7. Adding a score that exceeds the integer limit should be rejected") {
    val maxScore = Int.MaxValue // Get the max integer value
    val largeScore = maxScore / 2 // Use a large but safe number

    val response1 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      largeScore
    )
    assert(response1.contains("Added")) // First addition should work

    val response2 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      largeScore
    )
    assert(response2.contains("Added")) // Second addition should work

    // Use `2` to ensure rounding errors don't falsely pass the test
    val response3 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      2
    )
    assert(
      response3.contains("Max social score exceeded"),
      s"Unexpected message: $response3"
    )

    val remainingScore = maxScore - (largeScore * 2)
    val response4 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      remainingScore
    )
    assert(response4.contains("Added")) // This should be accepted

    assertEquals(
      URLManager.exportData(),
      s"domain;urls;social_score\nrte.ie;3;$maxScore"
    )
  }

  test(
    "8. Attempting to add a score when the domain is at max limit should fail"
  ) {
    val maxScore = Int.MaxValue // Get the max integer value
    val largeScore = maxScore / 2 // Use a large but safe number

    val response1 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      largeScore
    )
    assert(response1.contains("Added")) // First addition should work

    val response2 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      largeScore
    )
    assert(response2.contains("Added")) // Second addition should work

    // Use `2` to ensure rounding errors don't falsely pass the test
    val response3 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      2
    )
    assert(
      response3.contains("Max social score exceeded"),
      s"Unexpected message: $response3"
    )

    val remainingScore = maxScore - (largeScore * 2)
    val response4 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      remainingScore
    )
    assert(response4.contains("Added")) // This should be accepted

    val response5 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      1
    )
    assert(
      response5.contains("Max social score exceeded"),
      s"Unexpected message: $response5"
    )
  }

  test("9. Adding a negative score should be rejected") {
    val response = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      -1
    )
    assert(response.contains("Social score must be a non-negative integer."))
  }

  test("10. Social score remains unchanged after invalid additions") {
    // Add a valid score first
    URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      20
    )

    // Capture the export before attempting invalid additions
    val beforeExport = URLManager.exportData()

    // Try adding invalid scores (negative & exceeding max)
    val response1 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      -1
    )
    assert(response1.contains("Social score must be a non-negative integer."))

    val response2 = URLManager.addUrl(
      "http://www.rte.ie/news/politics/2018/1004/1001034-cso/",
      Int.MaxValue
    )
    assert(
      response2.contains("Max social score exceeded"),
      s"Unexpected message: $response2"
    )

    // Ensure state remains the same
    assertEquals(
      URLManager.exportData(),
      beforeExport // Should remain unchanged
    )
  }
}
