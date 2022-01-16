package com.example.printo

import android.util.Log
import java.net.ServerSocket

class Server
{
    lateinit var serverThread : Thread
    lateinit var server : ServerSocket
    private val PORT = 5050
    var isStart : Boolean = true

    fun start()
    {
        serverThread = Thread()
        {
            try
            {
                server = ServerSocket(PORT)
                while (isStart)
                    try{Thread(ServerHandler(server.accept())).start()}catch(e : java.lang.Exception){}
            }
            catch (e : Exception)
            {
                Log.w("fThread",e.toString())
            }
        }
        isStart = true
        serverThread.priority = Thread.MAX_PRIORITY
        serverThread.start()
    }

    fun stop()
    {
        try
        {
            isStart = false
            try{ server.close() } catch (e : Exception){}
        }
        catch (e : Exception) { Log.w("step",e.toString()) }
    }
}