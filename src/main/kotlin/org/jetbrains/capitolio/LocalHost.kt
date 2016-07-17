package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.ConnectionOptions
import com.xebialabs.overthere.OperatingSystemFamily
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import java.util.zip.ZipInputStream

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */

class LocalHost(): Host {

    val local = LocalConnection("local", ConnectionOptions());
    override val os: OperatingSystemFamily = local.hostOperatingSystem
    override var host = "localhost"
    override var envKeyword = "set"

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
        val website = URL("${serverUrl.toString()}/update/buildAgent.zip")
        val rbc = Channels.newChannel(website.openStream())
        val fos = FileOutputStream("buildAgent.zip")
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)

        unZip(File("buildAgent.zip"), installPath)

        return BuildAgent(this, serverUrl)
    }

    override fun setWorkingDirectory(directoryPath:String) {
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

    fun extract(archive: File, destinationPath: File) {
        unTarGz(archive, destinationPath)
    }

    private fun unTarGz(archive: File, destinationPath: File) {
        destinationPath.mkdirs();

        val tarIn = TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(archive))))
        System.out.println("Extracting [$archive] into [$destinationPath]");

        var tarEntry = tarIn.nextTarEntry
        while (tarEntry != null) {
            val destPath = File(destinationPath, tarEntry.name);
            if (tarEntry.isDirectory) {
                destPath.mkdirs();
            } else {
                if (!destPath.parentFile.exists()) {
                    destPath.parentFile.mkdirs()
                }
                destPath.createNewFile();

                val data = ByteArray(2048)
                val bout = BufferedOutputStream(FileOutputStream(destPath));
                var len = tarIn.read(data);
                while(len != -1) {
                    bout.write(data, 0, len);
                    len = tarIn.read(data)
                }
                bout.close();
            }
            tarEntry = tarIn.nextTarEntry;
        }

        tarIn.close();
    }

    fun unZip(zipFile: File, outputFolder: String) {

        val buffer = ByteArray(1024)

        try {
            val folder = File(outputFolder)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            val zis = ZipInputStream(FileInputStream(zipFile))
            var ze = zis.nextEntry

            while (ze != null) {
                val fileName = ze.name
                val newFile = File(outputFolder + File.separator + fileName)
                File(newFile.parent).mkdirs()

                val fos = FileOutputStream(newFile)
                var len: Int = zis.read(buffer)

                while (len > 0) {
                    fos.write(buffer, 0, len)
                    len = zis.read(buffer)
                }

                fos.close()
                ze = zis.nextEntry
            }

            zis.closeEntry()
            zis.close()

        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }
}
