import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui
    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();
    static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );
    public SocketChannel userChannel;

    
    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message);
    }

    
    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                    chatBox.setText("");
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                chatBox.requestFocus();
            }
        });
        // --- Fim da inicialização da interface gráfica

        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui
        InetSocketAddress isa = new InetSocketAddress( server, port );
        userChannel = SocketChannel.open(isa);
        userChannel.configureBlocking(true);

    }


    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
        // PREENCHER AQUI com código que envia a mensagem ao servidor
        message = message+"\n";

        Scanner sc = new Scanner(message);
        /**
         * escape / character
         * 
         */
        if(sc.hasNext()){
            String command = sc.next();
            if(!command.equals("/nick") && !command.equals("/join") && !command.equals("/priv") && !command.equals("/leave") && !command.equals("/bye")){
                if(message.substring(0, 1).equals("/")){
                    message="/"+message;
                }
            }
        }


        ByteBuffer response = ByteBuffer.wrap(message.getBytes(charset));
        userChannel.write(response);
        printMessage(message);

    }

    
    // Método principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
        printMessage("\t\t   Welcome\n Commands: \n");
        printMessage("/nick [name] - Choose a name\n");
        printMessage("/join [room] - join a chat room\n");
        printMessage("/leave - leave current chat room\n");
        printMessage("/bye - quit chat app\n");
        printMessage("/priv [name] [message] - send private message to a user\n");
        printMessage("\n******************************************\n\n");
        
        while(true){
            String message="";
            // Read the message to the buffer
            buffer.clear();
            userChannel.read( buffer );
            buffer.flip();

            // If no data, close the connection
            if (buffer.limit() != 0) {
                // Decode and print the message to stdout
                message = decoder.decode(buffer).toString();
                System.out.print( message );
            }

            printMessage(message);
            
            if(message.equals("BYE\n")){
                userChannel.close();
                break;
            }
        }
         //close window when BYE
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));


    }
    

    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}
