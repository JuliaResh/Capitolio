package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.OperatingSystemFamily
import java.io.File
import java.net.URL

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
interface Host {

    fun installTeamCity(dist: File, installPath:File = defaultInstallPath()): TeamCityServer
    fun installTeamCity(version:String? = null, installPath:File = File(defaultInstallPath(), "TeamCity")): TeamCityServer

    fun installBuildAgent(serverUrl: URL, dist: File? = null, installPath: String = "${defaultInstallPath()}/BuildAgent"): BuildAgent
    fun setWorkingDirectory(directoryPath:File)

    fun  startProcess(cmdLine: CmdLine)

    fun  execute(cmdLine: CmdLine)

    val  envKeyword: String
    val  host: String
    val os: OperatingSystemFamily

}