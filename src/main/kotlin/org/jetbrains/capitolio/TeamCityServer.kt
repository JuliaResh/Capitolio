package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.CmdLine.build
import com.xebialabs.overthere.CmdLineArgument
import com.xebialabs.overthere.CmdLineArgument.arg
import com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import org.apache.commons.io.FileUtils
import org.jetbrains.capitolio.ServerModeEnum.MAIN_SERVER
import org.jetbrains.capitolio.ServerModeEnum.RUNNING_BUILDS_NODE
import java.io.File
import java.net.URL
import java.util.*

/**
 * Created by Julia.Reshetnikova on 30-Jun-16.
 */

class TeamCityServer(val host: Host, val installPath: File? = File(defaultInstallPath(), "TeeamCity")) {

    private val binPath = File(installPath, "bin")
    private val confPath = File(installPath, "conf")
    private val logPath = File(installPath, "log")

    private val ext = if (host.os.equals(WINDOWS)) "bat" else "sh"
    private val TEAMCITY_SERVER_SCRIPT = "teamcity-server.$ext"
    private val RUN_ALL_SCRIPT = "runAll.$ext"
    private val CMD_EXE = ArrayList<CmdLineArgument>()

    var port:Int = 8111
    var catalinaPort:Int = 8005
    var serverUrl = URL("http://$host:$port")
        get() { return URL("http://$host:$port") }

    var startUpOptions:String? = null
    var startJdkPath:String? = null
    var dataDirectoryPath:String? = null

    var serverRole = MAIN_SERVER
        set(value) { setRole(value) }

    private val bundledAgent = BuildAgent(host, serverUrl, File(installPath, "buildAgent"))


    constructor(host: LocalHost) : this(host, null) {}

    init {
        if (host.os.equals(WINDOWS)) {
            CMD_EXE.add(arg("cmd"))
            CMD_EXE.add(arg("/c"))
        } else {
            CMD_EXE.add(arg("bash"))
        }
    }

    fun start() { startServer() }

    fun runAll() { start(RUN_ALL_SCRIPT) }

    fun startServer() { start(TEAMCITY_SERVER_SCRIPT) }

    fun startBundledAgent() { bundledAgent.start() }

    fun waitForMaintenancePage() { waitForServerStart("$serverUrl/mnt") }


    fun stop() {
        stopBundledAgent()
        stopServer()
    }

    fun stopServer() { stop(TEAMCITY_SERVER_SCRIPT) }

    fun stopAll() { stop(RUN_ALL_SCRIPT) }

    fun stopBundledAgent() { bundledAgent.stop() }

    fun waitForServerStop() { waitForServerStop(port, getServerPid()) }


    private fun start(script:String) {
        host.setWorkingDirectory(binPath)

        val cmdLine = CmdLine()

        if (!startUpOptions.isNullOrEmpty()) {
            cmdLine.add(startUpOptionsCmdLine()).addArgument("&&")
        }
        if (!dataDirectoryPath.isNullOrEmpty()) {
            cmdLine.add(dataDirectoryPathPathCmdLine()).addArgument("&&")
        }
        if (!startJdkPath.isNullOrEmpty()) {
            cmdLine.add(startJavaCmdLine()).addArgument("&&")
        }

        cmdLine.add(CMD_EXE).addArgument(script).addArgument("start")
        cmdLine.addRaw(">>").addArgument("$logPath/start.log")
        host.startProcess(cmdLine)
    }

    private fun stop(script: String) {
        host.setWorkingDirectory(binPath)
        host.execute(CmdLine().add(CMD_EXE).addNested(build(script, "stop", "-force", "60")))
    }

    private fun setRole(role: ServerModeEnum) {
        val teamcityStartupProperties = File(installPath, "conf/teamcity-startup.properties")
        if (role.equals(RUNNING_BUILDS_NODE)) {
            FileUtils.writeStringToFile(teamcityStartupProperties, "teamcity.server.role=running-builds-node")
            FileUtils.writeStringToFile(teamcityStartupProperties, "teamcity.rootUrl=$serverUrl", true)
        } else if (teamcityStartupProperties.exists()) {
            teamcityStartupProperties.delete()
        }
    }

    private fun startJavaCmdLine(): ArrayList<CmdLineArgument> {

        return object : ArrayList<CmdLineArgument>() {
            init {
                add(arg(host.envKeyword))
                add(arg("TEAMCITY_JRE=$startJdkPath"))
            }
        }
    }

    private fun startUpOptionsCmdLine(): ArrayList<CmdLineArgument> {

        return object : ArrayList<CmdLineArgument>() {
            init {
                add(arg(host.envKeyword))
                add(arg("TEAMCITY_SERVER_OPTS=$startUpOptions"))
            }
        }
    }

    private fun dataDirectoryPathPathCmdLine(): ArrayList<CmdLineArgument> {

        return object : ArrayList<CmdLineArgument>() {
            init {
                add(arg(host.envKeyword))
                add(arg("TEAMCITY_DATA_PATH=$dataDirectoryPath"))
            }
        }
    }

    fun configureServerPort(value:Int) {
        val serverConf = File("$installPath/conf/server.xml")
        var content = FileUtils.readFileToString((serverConf), "UTF-8")
        content = content.replace("<Connector port=\"$port\"", "<Connector port=\"$value\"")
        FileUtils.writeStringToFile(serverConf, content, "UTF-8")

        this.port = value
    }

    fun configureCatalinaPort(value:Int) {
        val serverConf = File("$installPath/conf/server.xml")
        var content = FileUtils.readFileToString((serverConf), "UTF-8")
        content = content.replace("<Server port=\"$catalinaPort\"", "<Server port=\"$value\"")
        FileUtils.writeStringToFile(serverConf, content, "UTF-8")

        this.catalinaPort = value
    }

}
