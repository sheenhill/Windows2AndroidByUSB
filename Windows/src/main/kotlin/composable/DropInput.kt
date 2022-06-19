package composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bean.FileInfo
import util.logI
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

@Composable
fun DropInput(
    modifier: Modifier = Modifier, msg: String, change: (String) -> Unit, dropFile: (FileInfo) -> Unit
) {
    val msg1 = remember { mutableStateOf("") }
    SwingPanel(background = Color.White, modifier = modifier.fillMaxSize(), factory = {
        ComposePanel().apply {
            dropTarget = DropTarget()
            dropTarget.addDropTargetListener(DropFileListener {
                logI("DropFileListener.getFilesInfo >>> $it")
                if (it.isNotEmpty())
                    dropFile(it[0])
            })
            setContent {
                BasicTextField(
                    value = msg1.value, onValueChange = {
                        change(it)
                    }, modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }, update = {
        msg1.value = msg
    })
}


class DropFileListener(val getFilesInfo: (List<FileInfo>) -> Unit) : DropTargetListener {
    override fun dragEnter(dtde: DropTargetDragEvent?) {
    }

    override fun dragOver(dtde: DropTargetDragEvent?) {
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) {
    }

    override fun dragExit(dte: DropTargetEvent?) {
    }

    override fun drop(dtde: DropTargetDropEvent?) {
        dtde?.let { event ->
            event.acceptDrop(DnDConstants.ACTION_REFERENCE)
            val x = event.transferable?.getTransferData(DataFlavor.javaFileListFlavor)
                .let { it as? List<*> ?: listOf<File>() }.filterIsInstance<File>().map {
                    FileInfo(
                        path = it.toURI().rawPath.substring(1),
                        size = (it.length() / 1024).toInt()
                    )
                }
            getFilesInfo(x)
            event.dropComplete(true)
        }
    }
}