package com.example.printo
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.*
import java.net.*
import java.nio.file.Files
import java.nio.file.Paths
import java.io.FileOutputStream as FileOutputStream1

class ServerHandler(private val socket : Socket) : Runnable
{
    private  var PATH :String = ServerActivity.getIns().PATH
    private  var PATH_FOR_DATA = ServerActivity.getIns().PATH_FOR_DATA
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var sb : java.lang.StringBuilder

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run()
    {
        try
        {
            inputStream = socket.getInputStream()
            sb = StringBuilder()

            //http msg
            while (true)
            {
                if(sb.toString().endsWith("\r\n\r\n"))
                    break
                val read = inputStream.read()
                sb.append(read.toChar())
            }

            //getting method and url form http msg
            val httpMsg: String = sb.toString()
            val req = httpMsg.split("\r\n")
            val firstLine = req[0].split(" ")
            val method = firstLine[0]
            val url = firstLine[1].replace("%20"," ")

            //server code for responding to POST request
            if (method == "POST")
            {
                val size = if(req[2].split(" ")[1].startsWith("Mozilla",true))
                {
                    req[6].split(" ")[1].toInt()
                } else {
                    req[3].split(" ")[1].toInt()
                }

                val file = File("$PATH_FOR_DATA/Printo$url")
                val fileOutput = FileOutputStream1(file)
                var count = 0
                val chunk = 1024*1024*5
                val buffer = ByteArray(chunk)
                //no more base 64
                while(true)
                {
                    if(count == size)
                        break
                    val read = inputStream.read(buffer,0,buffer.size)
                    fileOutput.write(buffer,0,read)
                    count += read
                }
                fileOutput.flush()
                fileOutput.close()
                sendMassage("com",url)
            }

            //code for server response to GET http method
            else if(method == "GET")
            {
                if (url == "/")
                   sendFile("text/html","/index.html")
                else
                   sendFile(getMime(url),url)
            }
        }
        catch (e : Exception)
        {
            Log.w("REST",e.toString())
        }
        finally
        {
            socket.close()
        }
    }

    //routine for sending file over http
    private fun sendFile(type : String,file : String)
    {
        try
        {
            outputStream = socket.getOutputStream()
            outputStream.write("HTTP/1.1 200 OK\r\n".toByteArray())
            outputStream.write("Content-Type:$type\r\n".toByteArray())
            outputStream.write("\r\n".toByteArray())
            val fileInput = FileInputStream("$PATH/clientSide$file")
            val bufferedInputStream = BufferedInputStream(fileInput)
            var read: Int
            val buf  =  ByteArray(1024)
            while (true)
            {
                read = bufferedInputStream.read(buf)
                if (read == -1)
                    break
                outputStream.write(buf,0,read)
            }
            bufferedInputStream.close()
            fileInput.close()
        }
        catch (e : Exception)
        {
            Log.w("FILE_SENDING",e.toString())
        }
    }

    //routine for getting mime type
    @SuppressLint("NewApi")
    private fun getMime(file : String) : String
    {
        return when
        {
            file.endsWith(".js") -> "text/javascript"
            file.endsWith(".css") -> "text/css"
            else -> Files.probeContentType(Paths.get("$PATH/clientSide$file")).toString()
        }
    }

    private fun sendMassage(key : String, msg : String)
    {
        val intent = Intent("file_event")
        intent.putExtra(key,msg)
        LocalBroadcastManager.getInstance(ServerActivity.getIns()).sendBroadcast(intent)
    }
}