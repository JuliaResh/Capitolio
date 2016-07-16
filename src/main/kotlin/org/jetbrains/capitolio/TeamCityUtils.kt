package capitolio

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * Created by Julia.Reshetnikova on 30-Jun-16.
 */

fun defaultInstallPath(): String {
    val installPath: String
    if (true) {
        installPath = "C:/"
    } else {
        installPath = "/etc/"
    }

    return installPath
}

fun downloadTeamCityDist(version: String): File {
    val archiveName = "TeamCity-$version.tar.gz"
    val dist = File(archiveName)
    val url = URL("http://download.jetbrains.com/teamcity/" + archiveName)
    var downloadNeeded = true

    // Check if file already exists
    if (dist.exists()) {
        if (dist.isFile) {
            if (verifyCheckSum(dist, archiveName)) {
                println("Will use already present " + archiveName)
                downloadNeeded = false
            }
        } else {
            println("Path is present, but is not a file")
        }
    }

    for (i in 0..2) {
        if (downloadNeeded) {
            println("Start downloading TeamCity $version from jetbrains.com...")
            FileUtils.copyURLToFile(url, dist)
            if (verifyCheckSum(dist, archiveName)) {
                downloadNeeded = false
            }
        }
    }

    return dist
}

@Throws(IOException::class)
private fun verifyCheckSum(localFile: File, archiveFilename: String): Boolean {
    var verified = false

    val localSha256 = DigestUtils.sha256Hex(FileInputStream(localFile))
    var distSha256 = IOUtils.toString(URI.create("https://download.jetbrains.com/teamcity/$archiveFilename.sha256"))
    distSha256 = distSha256.substring(0, distSha256.lastIndexOf(" *" + archiveFilename))
    if (localSha256 == distSha256) {
        verified = true
    } else {
        println("$archiveFilename sha256 sum is: $localSha256 while remote sha256 is $distSha256")
    }

    return verified
}

@Throws(IOException::class)
fun getLatestReleasedVersionNumber(): String {
    val url = IOUtils.toString(URI.create("https://www.jetbrains.com/teamcity/update.xml"))
    val document = Jsoup.parse(url)

    return document.select("build").first().attr("version")
}

fun downloadLatestRelease(): File {
    return downloadTeamCityDist(getLatestReleasedVersionNumber())
}

fun waitForServerStart(url:String) {
    var serverIsUp = false
    println("Waiting for server to become available at " + url)

    for (i in 0..239) {
        var responseCode = 0
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            responseCode = connection.responseCode
            connection.disconnect()
        } catch (e: ConnectException) {
            Thread.sleep(1000)
        }

        if (responseCode == 200 || responseCode == 403) {
            serverIsUp = true
            break
        } else {
            Thread.sleep(1000)
        }
    }

    if (serverIsUp) {
        println("Server is up!")
    } else {
        throw Exception("Server failed to start")
    }

}

fun waitForServerStop(port:Int, url:Int) {}

fun getServerPid(): Int {
    return 123456789
}
