package capitolio

import capitolio.ServerModeEnum.BUILD_MESSAGE_PROCESSOR
import capitolio.ServerModeEnum.MAIN_SERVER
import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.CmdLineArgument
import com.xebialabs.overthere.CmdLineArgument.arg
import com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Julia.Reshetnikova on 30-Jun-16.
 */

class TeamCityServer(var host: Host) {

    var installPath = "${defaultInstallPath()}/TeamCity"
    var port:Int = 8111
        set(value) { changePort(value) }
    var catalinaPort:Int = null!!
        set(value) { changeCatalinaPort(value) }
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

    private fun start(script:String) {
        val cmdLine = com.xebialabs.overthere.CmdLine()

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

    fun stop() {
        host.setWorkingDirectory("$installPath/bin")
        host.execute(CmdLine.build("cmd", "/c", "teamcity-server.bat", "stop", "-force", "60"))
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

                if (startMode.equals(BUILD_MESSAGE_PROCESSOR)) {
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

    private fun changePort(value:Int) {

        val serverConf = File("$installPath/conf/server.xml")
        var content = org.apache.commons.io.FileUtils.readFileToString((serverConf), "UTF-8")
        content.replace("Connector port=\"$port\"", "Connector port=\"$value\"")
        FileUtils.writeStringToFile(serverConf, content, "UTF-8")

        this.port = value
    }


}
