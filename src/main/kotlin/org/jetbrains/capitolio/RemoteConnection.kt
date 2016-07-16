package org.jetbrains.capitolio

import com.xebialabs.overthere.*
import com.xebialabs.overthere.CmdLine.build
import com.xebialabs.overthere.ConnectionOptions.*
import com.xebialabs.overthere.OperatingSystemFamily.UNIX
import com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL
import com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE
import com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_NATIVE
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL
import com.xebialabs.overthere.ssh.SshConnectionType.SCP
import java.io.File

/**
 * Created by Julia.Reshetnikova on 30-Oct-15.
 */

internal class RemoteConnection @Throws(Exception::class)

constructor(hostIp:String, os: OperatingSystemFamily, username:String, password:String) {
    private val options: ConnectionOptions = null!!
    private val protocol:String = null!!
    var workingDirectory:String = null!!
    private val host:String = null!!
    var os: OperatingSystemFamily = WINDOWS
    var connection: OverthereConnection = null!!

    init {
        this.host = hostIp
        options = ConnectionOptions()
        options.set(ADDRESS, hostIp)
        options.set(USERNAME, username)
        options.set(PASSWORD, password)
        if (os == WINDOWS) {
            options.set(CONNECTION_TYPE, WINRM_NATIVE)
            workingDirectory = "C:\\Users\\$username"
            protocol = CIFS_PROTOCOL
        } else {
            options.set(CONNECTION_TYPE, SCP)
            workingDirectory = "/home/$username"
            protocol = SSH_PROTOCOL
        }
        options.set(OPERATING_SYSTEM, os)
    }

    fun setPrivateKey(privateKeyFile: File) {
        //options.set(PRIVATE_KEY_FILE, privateKeyFile);
    }

    fun connect(): RemoteConnection {
        connection = Overthere.getConnection(protocol, options)
        connection.workingDirectory = connection.getFile(workingDirectory)
        return this
    }

    fun disconnect() {
        connection.close()
    }

    fun execute(command:String) {
        connection.execute(build(command))
    }

    fun delete(path:String) {
        getRemoteFile(path).deleteRecursively()
    }

    fun put(localFilePath:String, remoteFilePath:String) {
        val localFile = getLocalFile(localFilePath)
        val remoteFile: OverthereFile
        try {
            remoteFile = connection.getFile(remoteFilePath)
        } catch (e: RuntimeIOException) {
            remoteFile = connection.getFile(connection.workingDirectory, remoteFilePath)
        }
        localFile.copyTo(remoteFile)
    }

    fun get(remoteFilePath:String, localFilePath:String) {
        var localFile = getLocalFile(localFilePath)
        var remoteFile: OverthereFile
        remoteFile = connection.getFile(remoteFilePath)
        if (!remoteFile.exists()) {
            remoteFile = connection.getFile(connection.workingDirectory, remoteFilePath)
        }
        remoteFile.copyTo(localFile)
    }

    private fun getLocalFile(localFilePath:String): LocalFile {
        val localConnection = LocalConnection("local", ConnectionOptions())
        return LocalFile(localConnection, File(localFilePath))
    }

    private fun getRemoteFile(remoteFilePath:String): OverthereFile {
        val remoteFile: OverthereFile
        try {
            remoteFile = connection.getFile(remoteFilePath)
        } catch (e: RuntimeIOException) {
            remoteFile = connection.getFile(connection.workingDirectory, remoteFilePath)
        }
        //remoteFile = connection.getFile(connection.getWorkingDirectory(), remoteFilePath);
        return remoteFile
    }

    fun extractZip(filename:String, destFolder:String) {
        if (os === UNIX) {
            this.execute("unzip -o $filename -d $destFolder")
        } else if (os === WINDOWS) {
            this.execute("7z x $filename -aoa -o$destFolder")
        }
    }

    val username:String
        get() {
            return options.get(USERNAME)
        }

}