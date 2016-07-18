package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.CmdLineArgument
import com.xebialabs.overthere.CmdLineArgument.arg
import com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import org.apache.commons.io.FileUtils
import org.jetbrains.capitolio.ServerModeEnum.BUILD_MESSAGES_PROCESSOR
import org.jetbrains.capitolio.ServerModeEnum.MAIN_SERVER
import java.io.File
import java.util.*

/**
 * Created by Julia.Reshetnikova on 30-Jun-16.
 */

class TeamCityServer(var host: Host) {

    var installPath = "${defaultInstallPath()}/TeamCity"
    var port:Int = 8111
    var catalinaPort:Int = 8005
    var startUpOptions:String? = null
    var startJdkPath:String? = null
    var dataDirectoryPath:String? = null
    var startMode = MAIN_SERVER

    val ext = if (host.os.equals(WINDOWS)) "bat" else "sh"
    val TEAMCITY_SERVER_SCRIPT = "teamcity-server.$ext"
    val RUN_ALL_SCRIPT = "runAll.$ext"
    val AGENT_SCRIPT = "agent.$ext"

    val isBuildMessageProcessor = false

    constructor(host: LocalHost, installPath: String) : this(host) {
        this.installPath = installPath
    }

    fun runAll() {
        host.setWorkingDirectory("$installPath/bin")
        start(RUN_ALL_SCRIPT)
    }

    fun startBundledAgent() {
        host.setWorkingDirectory("$installPath/buildAgent/bin")
        start(AGENT_SCRIPT)
    }

    fun startServer() {
        host.setWorkingDirectory("$installPath/bin")
        start(TEAMCITY_SERVER_SCRIPT)
    }

    fun waitForMaintenancePage() {
        waitForServerStart("http://${host.host}:$port/mnt")
    }

    fun start() {
        if (startMode.equals(BUILD_MESSAGES_PROCESSOR)) {
            startServer()
        } else {
            runAll()
        }
    }

    private fun start(script:String) {
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

        cmdLine.addArgument("cmd").addArgument("/c")
        cmdLine.addArgument(script).addArgument("start")

        host.startProcess(cmdLine)
    }

    fun stopServer() {
        host.setWorkingDirectory("$installPath/bin")
        stop(TEAMCITY_SERVER_SCRIPT)
    }

    fun stop() {
        stopBundledAgent()
        stopServer()
    }

    fun stopBundledAgent() {
        host.setWorkingDirectory("$installPath/buildAgent/bin")
        stop(AGENT_SCRIPT)
    }

    private fun stop(script: String) {
        host.execute(CmdLine.build("cmd", "/c", script, "stop", "-force", "60"))
    }

    fun waitForServerStop() {
        waitForServerStop(port, getServerPid())
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

                if (startMode.equals(BUILD_MESSAGES_PROCESSOR)) {
                    add(arg("-Dteamcity.server.mode=build-messages-processor"))
                    add(arg("-Dteamcity.server.rootURL=http://${host.host}:$port"))
                }
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
