import com.formdev.flatlaf.FlatDarkLaf
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

import kotlin.random.Random
import kotlin.system.exitProcess
import javax.swing.JFrame
import java.awt.datatransfer.Clipboard

import java.awt.datatransfer.StringSelection




class EncryptionFrame {

    private lateinit var mainFrame: JFrame
    private lateinit var keyLabel: JLabel
    private lateinit var encryptionLabel: JLabel
    private lateinit var outputLabel: JLabel
    private lateinit var keyPanel: JPanel
    private lateinit var encryptionPanel: JPanel
    private lateinit var outputPanel: JPanel
    private lateinit var encryptionBox: JTextField
    private lateinit var keyBox: JTextField
    private lateinit var output: JTextField

    @Suppress("SpellCheckingInspection")
    private val charSet = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890!@#$%^&*()_+-={}|\\[]:;\"'<>,.?/".toCharArray().toList()

    init {
        prepareGUI()
    }

    private fun addAll(frame: JFrame, vararg components: java.awt.Component) {
        components.forEach {
            frame.add(it)
        }
    }

    private fun prepareGUI() {

        // Set up dark mode
        FlatDarkLaf.install()
        JFrame.setDefaultLookAndFeelDecorated(true)

        keyLabel = JLabel("Enter key:", JLabel.CENTER)
        encryptionLabel = JLabel("Enter text:", JLabel.CENTER)
        outputLabel = JLabel("Output:", JLabel.CENTER)

        keyBox = JTextField().apply {
            setSize(10, 50)
        }

        encryptionBox = JTextField()

        keyPanel = JPanel().apply { layout = FlowLayout() }
        encryptionPanel = JPanel().apply { layout = FlowLayout() }
        outputPanel = JPanel().apply { layout = FlowLayout() }

        output = JTextField().apply {
            isEditable = false
        }

        val iconURL = EncryptionFrame::class.java.getResource("/lock_icon.png")
        val icon = ImageIcon(iconURL)

        mainFrame = JFrame("Text Encryptor").apply {
            setSize(300, 300)
            layout = GridLayout(9, 1)
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(windowEvent: WindowEvent?) {
                    exitProcess(0)
                }
            })
            addAll(this,
                // key components
                keyLabel, keyBox, keyPanel,
                // encryption components
                encryptionLabel, encryptionBox, encryptionPanel,
                // output components
                outputLabel, output, outputPanel
            )
            iconImage = icon.image
            isUndecorated = true
            isVisible = true
        }
    }

    internal fun show() {

        val buttonClickListener = ButtonClickListener()

        val randKeyButton = JButton("Randomize").apply {
            actionCommand = "RANDOMIZE_KEY"
            addActionListener(buttonClickListener)
        }
        keyPanel.add(randKeyButton)

        val encryptButton = JButton("Encrypt").apply {
            actionCommand = "ENCRYPT"
            addActionListener(buttonClickListener)
        }

        val decryptButton = JButton("Decrypt").apply {
            actionCommand = "DECRYPT"
            addActionListener(buttonClickListener)
        }
        encryptionPanel.add(encryptButton)
        encryptionPanel.add(decryptButton)

        val copyButton = JButton("Copy result").apply {
            actionCommand = "COPY"
            addActionListener(buttonClickListener)
        }
        outputPanel.add(copyButton)

        mainFrame.isVisible = true
    }

    private fun shuffleCharSetWithKey(key: Long): List<Char> {
        return charSet.shuffled(Random(key))
    }

    fun runEncryptor(key: Long, text: String, decrypt: Boolean): String {
        val encryptedText: String
        var currentKey = key
        var shuffledCharSet = shuffleCharSetWithKey(currentKey)
        encryptedText = text.toList().map {
            if ((it == ' ') or (it == '\u200b')) {
                if (decrypt) {
                    return@map ' '
                } else {
                    return@map '\u200b'
                }
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
        return keyBox.text.toLongOrNull()
    }

    fun isValidText(text: String): Boolean {
        return text.any {
            charSet.contains(it) or (it == ' ') or (it == '\u200b')
        }
    }

    fun sendError(message: String) {
        JFrame.setDefaultLookAndFeelDecorated(true)
        JOptionPane.showMessageDialog(
            JFrame(), message, "Error",
            JOptionPane.ERROR_MESSAGE
        )
    }

    private inner class ButtonClickListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (e.actionCommand == "ENCRYPT" || e.actionCommand == "DECRYPT") {
                if (isValidText(encryptionBox.text)) {
                    val seed = getSeed()
                    if (seed is Long) {
                        output.text = runEncryptor(seed, encryptionBox.text, e.actionCommand == "DECRYPT")
                    } else {
                        sendError("Key must be a number!")
                    }
                } else {
                    sendError("Invalid text!")
                }
            } else if (e.actionCommand == "RANDOMIZE_KEY") {
                keyBox.text = (1..1000000).random().toString()
            } else if (e.actionCommand == "COPY") {
                output.selectAll()
                output.requestFocus()
                val stringSelection = StringSelection(output.text)
                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)
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
