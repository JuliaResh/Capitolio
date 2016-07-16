package capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.OperatingSystemFamily
import java.io.File

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
class RemoteHost(override val host: String, override val os: OperatingSystemFamily) : Host {
    override fun installTeamCity(): TeamCityServer {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun installTeamCity(version: String?, installPath: String?): TeamCityServer {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startProcess(cmdLine: CmdLine) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(cmdLine: CmdLine) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val envKeyword: String
        get() = throw UnsupportedOperationException()

    override fun setWorkingDirectory(directoryPath: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun installTeamCity(dist: File, installPath: String?): TeamCityServer {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

