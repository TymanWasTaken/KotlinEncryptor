import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

import kotlin.random.Random
import kotlin.system.exitProcess



class EncryptionFrame {

    private lateinit var mainFrame: JFrame
    private lateinit var keyLabel: JLabel
    private lateinit var textLabel: JLabel
    private lateinit var outputLabel: JLabel
    private lateinit var controlPanel: JPanel
    private lateinit var textBox: JTextField
    private lateinit var seedBox: JTextField
    private lateinit var output: JTextField
    private val charSet = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890!@#$%^&*()_+-={}|\\[]:;\"'<>,.?/".toCharArray().toList()

    init {
        prepareGUI()
    }

    private fun prepareGUI() {
        keyLabel = JLabel("Enter key:", JLabel.CENTER)
        textLabel = JLabel("Enter text:", JLabel.CENTER)
        outputLabel = JLabel("Output:", JLabel.CENTER)

        seedBox = JTextField().apply {
            setSize(10, 50)
        }

        textBox = JTextField()

        controlPanel = JPanel().apply { layout = FlowLayout() }

        output = JTextField().apply {
            isEditable = false
        }

        val iconURL = EncryptionFrame::class.java.getResource("/lock_icon.png")
        val icon = ImageIcon(iconURL)

        mainFrame = JFrame("Text Encryptor").apply {
            setSize(300, 300)
            layout = GridLayout(7, 1)
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(windowEvent: WindowEvent?) {
                    exitProcess(0)
                }
            })
            add(keyLabel)
            add(seedBox)
            add(textLabel)
            add(textBox)
            add(controlPanel)
            add(outputLabel)
            add(output)
            iconImage = icon.image
            isVisible = true
        }
    }

    internal fun show() {

        val encryptButton = JButton("Encrypt").apply {
            actionCommand = "ENCRYPT"
            addActionListener(ButtonClickListener())
        }
        controlPanel.add(encryptButton)

        val decryptButton = JButton("Decrypt").apply {
            actionCommand = "DECRYPT"
            addActionListener(ButtonClickListener())
        }
        controlPanel.add(decryptButton)

        mainFrame.isVisible = true
    }

    fun shuffleCharSetWithKey(key: Long): List<Char> {
        return charSet.shuffled(Random(key))
    }

    fun runEncryptor(key: Long, text: String, decrypt: Boolean): String {
        val encryptedText: String
        var currentKey = key
        var shuffledCharSet = shuffleCharSetWithKey(currentKey)
        encryptedText = text.toList().map {
            if (it == ' ') {
                return@map ' '
            } else {
                val changedChar: Char
                if (decrypt) {
                    changedChar = charSet[shuffledCharSet.indexOf(it)]
                    currentKey++
                    shuffledCharSet = shuffleCharSetWithKey(currentKey)
                } else {
                    changedChar = shuffledCharSet[charSet.indexOf(it)]
                    currentKey++
                    shuffledCharSet = shuffleCharSetWithKey(currentKey)
                }
                return@map changedChar
            }
        }.joinToString("")
        return encryptedText
    }

    fun getSeed(): Long? {
        return seedBox.text.toLongOrNull()
    }

    fun isValidText(text: String): Boolean {
        return text.any {
            charSet.contains(it) or (it == ' ')
        }
    }

    fun sendError(message: String) {
        JOptionPane.showMessageDialog(
            JFrame(), message, "Error",
            JOptionPane.ERROR_MESSAGE
        )
    }

    private inner class ButtonClickListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (e.actionCommand == "ENCRYPT" || e.actionCommand == "DECRYPT") {
                if (isValidText(textBox.text)) {
                    val seed = getSeed()
                    if (seed is Long) {
                        output.text = runEncryptor(seed, textBox.text, e.actionCommand == "DECRYPT")
                    } else {
                        sendError("Key must be a number!")
                    }
                } else {
                    sendError("Invalid text!")
                }
            } else {
                exitProcess(2)
            }
        }
    }
}

fun main() {
    val encryptionFrame = EncryptionFrame()
    encryptionFrame.show()
}
