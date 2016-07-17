package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.OperatingSystemFamily
import java.io.File
import java.net.URL

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
class RemoteHost(override val host: String, override val os: OperatingSystemFamily) : Host {

    override fun installBuildAgent(serverUrl: URL, dist: File?, installPath: String): BuildAgent {
        throw UnsupportedOperationException("not implemented")
    }

    override fun installTeamCity(version: String?, installPath: String?): TeamCityServer {
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

    override fun setWorkingDirectory(directoryPath: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun installTeamCity(dist: File, installPath: String?): TeamCityServer {
        throw UnsupportedOperationException("not implemented")
    }

}

