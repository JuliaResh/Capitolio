package org.jetbrains.capitolio

import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.CmdLineArgument
import com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import java.io.File
import java.net.URL
import java.util.*

/**
 * Created by Julia.Reshetnikova on 15-Jul-16.
 */

class BuildAgent(val host: Host, var serverUrl: URL,
                 val installPath: File? = File(defaultInstallPath(), "BuildAgent")) {

    var port = 9090
    var startJdkPath: String? = null

    val ext = if (host.os.equals(WINDOWS)) "bat" else "sh"
    val AGENT_SCRIPT = "agent.$ext"

    constructor(host: Host, serverUrl: URL) : this(host, serverUrl, null) {}

    fun configure(serverUrl: String? = null, port: Int? = null, name: String? = null) {
        val cmdLine = CmdLine()

        cmdLine.addArgument("cmd").addArgument("/c")
        cmdLine.addArgument(AGENT_SCRIPT).addArgument("configure")

        if (!serverUrl.isNullOrEmpty()) {
            this.serverUrl = URL(serverUrl)
            cmdLine.addArgument("serverUrl=$serverUrl")
        } else {
            cmdLine.addArgument("serverUrl=$this.serverUrl")
        }

        if (port != null) {
            this.port = port
            cmdLine.addArgument("port=$port")
        } else {
            cmdLine.addArgument("port=$this.port")
        }

        if (!name.isNullOrEmpty()) {
            cmdLine.addArgument("name=${name!!.replace(" ", "_")}")
        }

        host.execute(cmdLine)
    }

    fun start() {
        val cmdLine = CmdLine()

        if (!startJdkPath.isNullOrEmpty()) {
            cmdLine.add(startJavaCmdLine()).addArgument("&&")
        }

        cmdLine.addArgument("cmd").addArgument("/c")
        cmdLine.addArgument(AGENT_SCRIPT).addArgument("start")

        host.startProcess(cmdLine)
    }

    fun stop() {
        host.execute(CmdLine.build("cmd", "/c", AGENT_SCRIPT, "stop"))
    }


    private fun startJavaCmdLine(): ArrayList<CmdLineArgument> {

        return object : ArrayList<CmdLineArgument>() {
            init {
                add(CmdLineArgument.arg(host.envKeyword))
                add(CmdLineArgument.arg("TEAMCITY_JRE=$startJdkPath"))
            }
        }
    }

}