package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.OperatingSystemFamily
import java.io.File

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */
interface Host {

    fun installTeamCity(dist: File, installPath:String?): TeamCityServer
    fun installTeamCity(version:String?, installPath:String?): TeamCityServer
    fun installTeamCity(): TeamCityServer
    fun setWorkingDirectory(directoryPath:String)

    fun  startProcess(cmdLine: CmdLine)

    fun  execute(cmdLine: CmdLine)

    val  envKeyword: String
    val  host: String
    val os: OperatingSystemFamily

}