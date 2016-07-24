package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.OperatingSystemFamily
import java.io.File
import java.net.URL

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
class RemoteHost(override val host: String, override val os: OperatingSystemFamily) : Host {
    override fun installTeamCity(dist: File, installPath: File): TeamCityServer {
        throw UnsupportedOperationException("not implemented")
    }

    override fun installTeamCity(version: String?, installPath: File): TeamCityServer {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setWorkingDirectory(directoryPath: File) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun installBuildAgent(serverUrl: URL, dist: File?, installPath: String): BuildAgent {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startProcess(cmdLine: CmdLine) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun execute(cmdLine: CmdLine) {
        throw UnsupportedOperationException("not implemented")
    }

    override val envKeyword: String
        get() = throw UnsupportedOperationException()


}

