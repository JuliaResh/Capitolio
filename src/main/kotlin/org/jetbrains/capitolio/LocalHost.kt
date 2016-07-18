package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.ConnectionOptions
import com.xebialabs.overthere.OperatingSystemFamily
import com.xebialabs.overthere.OperatingSystemFamily.*
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.net.URL
import java.nio.channels.Channels

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */

class LocalHost(): Host {

    val local = LocalConnection("local", ConnectionOptions());
    override val os: OperatingSystemFamily = local.hostOperatingSystem
    override var host = "localhost"
    override var envKeyword = if (os.equals(WINDOWS)) "set" else "export"

    override fun installTeamCity(dist: File, installPath: String?): TeamCityServer {
        val destinationPath = installPath ?: defaultInstallPath()
        extract(dist, File(destinationPath))

        return TeamCityServer(this, "${installPath ?: defaultInstallPath()}/TeamCity")
    }

    override fun installTeamCity(version: String?, installPath: String?): TeamCityServer {
        val dist = downloadTeamCityDist(if (!version.isNullOrEmpty()) version!! else getLatestReleasedVersionNumber())
        val tc = installTeamCity(dist, installPath)
        delete(dist)

        return tc
    }

    override fun installBuildAgent(serverUrl: URL, dist: File?, installPath: String): BuildAgent {
        val distUrl = URL("${serverUrl.toString()}/update/buildAgent.zip")
        val rbc = Channels.newChannel(distUrl.openStream())
        val fos = FileOutputStream("buildAgent.zip")
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)

        extract(File("buildAgent.zip"), File(installPath))

        return BuildAgent(this, serverUrl)
    }

    override fun setWorkingDirectory(directoryPath: String) {
        local.workingDirectory = local.getFile(directoryPath)
    }

    override fun execute(cmdLine: CmdLine) {
        local.execute(cmdLine)
    }

    override fun startProcess(cmdLine: CmdLine) {
        local.startProcess(cmdLine)
    }

    fun delete(file: File) {
        LocalFile(local, file).delete()
    }

    fun deleteRecursively(file: File) {
        LocalFile(local, file).deleteRecursively()
    }

    fun extract(archive: File, destinationFolder: File) {
        System.out.println("Extracting [$archive] into [$destinationFolder]")

        if (archive.extension.equals("gz")) {
            val tarIn = TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(archive))))
            extract(tarIn, destinationFolder)

        } else if (archive.extension.equals("zip")) {
            val zis = ZipArchiveInputStream(FileInputStream(archive))
            extract(zis, destinationFolder)
        }
    }


    private fun extract(input: ArchiveInputStream, destinationFolder: File) {

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs()
        }

        var archiveEntry = input.nextEntry

        while (archiveEntry != null) {

            val buffer = ByteArray(2048)
            val destPath = File(destinationFolder, archiveEntry.name)

            if (archiveEntry.isDirectory) {
                destPath.mkdirs()

            } else {
                if (!destPath.parentFile.exists()) {
                    destPath.parentFile.mkdirs()
                }
                destPath.createNewFile()

                val bout = BufferedOutputStream(FileOutputStream(destPath))
                var len = input.read(buffer)
                while(len != -1) {
                    bout.write(buffer, 0, len)
                    len = input.read(buffer)
                }
                bout.close()
            }

            archiveEntry = input.nextEntry
        }

        input.close()

    }
}
