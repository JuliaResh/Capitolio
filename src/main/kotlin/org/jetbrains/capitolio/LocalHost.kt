package capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.ConnectionOptions
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
class LocalHost() : Host {
    override fun installTeamCity(): TeamCityServer {
        return installTeamCity(null, null)
    }

    val local = LocalConnection("local", ConnectionOptions());
    override val os = local.hostOperatingSystem
    override var host = "localhost"
    override var envKeyword = "set"

    override fun installTeamCity(dist: File, installPath: String?): TeamCityServer {
        val destinationPath = installPath ?: defaultInstallPath()
        extract(dist, File(destinationPath))
        return TeamCityServer(this, installPath ?: defaultInstallPath())
    }

    override fun installTeamCity(version: String?, installPath: String?): TeamCityServer {
        val dist = downloadTeamCityDist(if (!version.isNullOrEmpty()) version!! else getLatestReleasedVersionNumber())
        val tc = installTeamCity(dist, installPath)
        delete(dist)

        return tc
    }

    override fun setWorkingDirectory(directoryPath:String) {
        local.workingDirectory = local.getFile(directoryPath)
    }

    override fun execute(cmdLine:CmdLine) {
        local.execute(cmdLine)
    }

    override fun startProcess(cmdLine: CmdLine) {
        local.startProcess(cmdLine)
    }

    fun delete(file:File) {
        LocalFile(local, file).delete()
    }

    fun deleteRecursively(file:File) {
        LocalFile(local, file).deleteRecursively()
    }

    fun extract(archive:File, destinationPath:File) {
        unTarGz(archive, destinationPath)
    }


    private fun unTarGz(archive:File, destinationPath:File) {
        destinationPath.mkdirs();

        val tarIn = org.apache.commons.compress.archivers.tar.TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(archive))))
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
}
