import scala.collection.mutable
import java.net.URI
import scala.io.{Source, StdIn}
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.io.{BufferedWriter, FileWriter}

case class URLData(url: String, domain: String, socialScore: Int)

object URLManager {
  private val storageFile = "urls.csv"
  private val urlStore =
    mutable.ListBuffer[URLData]()
  private val domainStats =
    mutable.Map[String, (Int, Int)]()
  private val MaxUrlLength: Int = 2083
  private var isModified: Boolean = false

  def clearData(): Unit = {
    urlStore.clear()
    domainStats.clear()
    isModified = true
  }

  def saveData(): Unit = {
    val writer = new BufferedWriter(new FileWriter(storageFile))
    writer.write("url,domain,social_score\n")
    urlStore.foreach { data =>
      writer.write(s"${data.url },${data.domain },${data.socialScore }\n")
    }
    writer.close()
    isModified = false
    println("Data saved successfully.")
  }

  def loadData(): Boolean = {
    if (!Files.exists(Paths.get(storageFile))) return false

    val lines = Source.fromFile(storageFile).getLines()
    if (!lines.hasNext || lines.next().trim != "url,domain,social_score") {
      println(s"Invalid file format: Expected header. File will not be loaded.")
      return false
    }

    var validEntries, skippedEntries = 0

    for (line <- lines) {
      val parts = line.split(",").map(_.trim)
      if (parts.length == 3) {
        parts(2).toIntOption match {
          case Some(socialScore) =>
            addUrl(parts(0), socialScore)
            validEntries += 1
          case None => skippedEntries += 1
        }
      } else {
        skippedEntries += 1
      }
    }

    if (validEntries > 0) isModified = false
    println(
      s"Loaded $validEntries valid entries, skipped $skippedEntries invalid entries."
    )
    validEntries > 0
  }

  def addUrl(url: String, socialScore: Int): String = {
    if (url.length > MaxUrlLength)
      return s"URL exceeds maximum length ($MaxUrlLength characters)"
    if (socialScore < 0) return "Social score must be a non-negative integer."

    // Preserve exact URL formatting, including case
    val storedUrl = url.trim

    // Extract domain and remove "www." prefix if present
    val domain = Option(new URI(storedUrl).getHost)
      .map(
        _.toLowerCase.replaceFirst("^www\\.", "")
      ) // Removes "www." only if it exists
      .getOrElse(return s"Invalid URL format: $url")

    val (currentCount, currentSum) = domainStats.getOrElse(domain, (0, 0))

    val newSum = currentSum.toLong + socialScore.toLong
    if (newSum > Int.MaxValue)
      return s"Max social score exceeded, maximum allowed is ${Int.MaxValue - currentSum }"

    // Store the **exact** user input, without case normalization
    urlStore.append(URLData(storedUrl, domain, socialScore))

    // Update domainStats with processed domain (after any www is removed)
    domainStats.update(domain, (currentCount + 1, newSum.toInt))
    isModified = true

    s"Added: $storedUrl with score $socialScore"
  }

  def removeUrl(url: String): String = {
    val toRemove = urlStore.filter(_.url == url) // Find all instances - i.e could have duplicates which we allow
    if (toRemove.isEmpty) return s"URL $url not found."

    val domain = toRemove.head.domain
    val totalScoreRemoved = toRemove.map(_.socialScore).sum
    val (currentCount, currentSum) = domainStats.getOrElse(domain, (0, 0))

    // Remove all instances - i.e could have duplicates which we allow
    urlStore --= toRemove

    val newCount = currentCount - toRemove.length
    val newSum = currentSum - totalScoreRemoved

    if (newCount > 0) {
      domainStats.update(domain, (newCount, newSum))
    } else {
      domainStats.remove(domain)
    }

    isModified = true
    s"Removed all instances of: $url"
  }

  def exportData(): String = {
    val sb = new StringBuilder("domain;urls;social_score\n")
    domainStats.foreach { case (domain, (count, sum)) =>
      sb.append(s"$domain;$count;$sum\n")
    }
    sb.toString().trim
  }

  def cmdlineLoop(): Unit = {
    if (loadData()) println(s"Data loaded from $storageFile")

    println(
      "Welcome to the URL Social Score Manager. Enter commands: ADD <url> <score>, REMOVE <url>, EXPORT, SAVE, EXIT"
    )

    var running = true
    while (running) {
      print("> ")
      val input = StdIn.readLine().trim
      val command =
        input.split(" ")(0).toUpperCase // Normalize the command - i.e. allow any case

      command match {
        case "ADD" =>
          val parts = input.stripPrefix("ADD ").split(" ", 2).map(_.trim)
          if (parts.length == 2 && parts(1).toIntOption.isDefined) {
            println(addUrl(parts(0), parts(1).toInt))
          } else {
            println("Invalid ADD command. Use: ADD <url> <score>")
          }

        case "REMOVE" =>
          val url = input.stripPrefix("REMOVE ").trim
          if (url.nonEmpty) println(removeUrl(url))
          else println("Invalid REMOVE command. Use: REMOVE <url>")

        case "EXPORT" => println(exportData())
        case "SAVE"   => saveData()
        case "EXIT" =>
          if (isModified) {
            print("Do you want to save before exiting? (Y/N) ")
            if (StdIn.readLine().trim.toUpperCase == "Y") saveData()
          }
          running = false

        case _ =>
          println("Invalid command. Use ADD, REMOVE, EXPORT, SAVE, or EXIT.")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    cmdlineLoop()
  }
}
