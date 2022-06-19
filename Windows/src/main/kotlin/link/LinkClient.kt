import kotlinx.coroutines.flow.MutableSharedFlow
import link.Sign
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import util.logI
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException


class LinkClient(port: Int) {

    companion object {
        private const val TAG = "LinkClient"
    }

    var socket: Socket

    var msgFlow = MutableSharedFlow<Pair<Int, String>>()

    init {
        logI("${TAG}.open()")
        socket = Socket()
        socket.keepAlive = true
        val address = InetSocketAddress("127.0.0.1", port)
        socket.connect(address)
    }

    suspend fun connect() {
        val source = socket.source().buffer()
        while (!source.exhausted()) {
            val length = source.readInt()
            val type = source.readInt()
            val data = source.readUtf8(length.toLong())
            logI("length = $length , type = $type , data = $data")
            msgFlow.emit(
                Pair(
                    first = type, second = data
                )
            )
        }
    }

    private var sink: BufferedSink? = null

    fun emit(sign: Int, msg: ByteArray):Int {
        return try{
            if (sink == null) {
                sink = socket.sink().buffer()
            }
            sink?.writeInt(msg.size)?.writeInt(sign)?.write(msg)?.emit()
            Sign.EMIT_OK
        }catch (e:java.lang.NullPointerException){
            logI("${TAG}.emit Exception >>> socket is NULL")
            Sign.EMIT_ERROR
        }catch (e:SocketException){
            logI("${TAG}.emit Exception >>> socket is CLOSED")
            Sign.EMIT_ERROR
        }
    }
}


