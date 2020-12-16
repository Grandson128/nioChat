import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.lang.String;


class ChatUser{

    String nick;
    String chatRoomName;
    String status;
    SocketChannel userChannel;

    ChatUser(SocketChannel sc){
        nick = "";
        status = "init";
        chatRoomName = "";
        userChannel = sc;
    }

}


public class ChatServer
{
    // A pre-allocated buffer for the received data
    static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();


    /**
     *   HashMap Declaration to keep track of Chat Users 
     *   Key = SocketChannel binded to the user
     *   Value = the user  
     **/            
    
    private static HashMap<SocketChannel, ChatUser> chatUsers = new HashMap<SocketChannel, ChatUser>();
    
  static public void main( String args[] ) throws Exception {
    // Parse port from command line
    //int port = Integer.parseInt( args[0] );
    int port = 8000;
    try {
      // Instead of creating a ServerSocket, create a ServerSocketChannel
      ServerSocketChannel ssc = ServerSocketChannel.open();

      // Set it to non-blocking, so we can use select
      ssc.configureBlocking( false );

      // Get the Socket connected to this channel, and bind it to the
      // listening port
      ServerSocket ss = ssc.socket();
      InetSocketAddress isa = new InetSocketAddress( port );
      ss.bind( isa );

      // Create a new Selector for selecting
      Selector selector = Selector.open();

      // Register the ServerSocketChannel, so we can listen for incoming
      // connections
      ssc.register( selector, SelectionKey.OP_ACCEPT );
      System.out.println( "Listening on port "+port );

      while (true) {
        // See if we've had any activity -- either an incoming connection,
        // or incoming data on an existing connection
        int num = selector.select();

        // If we don't have any activity, loop around and wait again
        if (num == 0) {
          continue;
        }

        // Get the keys corresponding to the activity that has been
        // detected, and process them one by one
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
          // Get a key representing one of bits of I/O activity
          SelectionKey key = it.next();

          // What kind of activity is it?
          if (key.isAcceptable()) {

            // It's an incoming connection.  Register this socket with
            // the Selector so we can listen for input on it
            Socket s = ss.accept();
            System.out.println( "Got connection from "+s );

            // Make sure to make it non-blocking, so we can use a selector
            // on it.
            SocketChannel sc = s.getChannel();
            sc.configureBlocking( false );

            // Register it with the selector, for reading
            sc.register( selector, SelectionKey.OP_READ );


            /**
             * Add new user to hash map
             */

            if(!chatUsers.containsKey(sc)){
                ChatUser newUser = new ChatUser(sc);
                chatUsers.put(sc, newUser);
                
            }


          } else if (key.isReadable()) {

            SocketChannel sc = null;

            try {

              // It's incoming data on a connection -- process it
              sc = (SocketChannel)key.channel();
              boolean ok = processInput( sc );

              // If the connection is dead, remove it from the selector
              // and close it
              if (!ok) {
                key.cancel();

                /**
                 * Remove user From hashmap
                 */
                chatUsers.remove(sc);


                Socket s = null;
                try {
                  s = sc.socket();
                  System.out.println( "Closing connection to "+s );
                  s.close();
                } catch( IOException ie ) {
                  System.err.println( "Error closing socket "+s+": "+ie );
                }
              }

            } catch( IOException ie ) {

              // On exception, remove this channel from the selector
              key.cancel();

              try {
                /**
                 * Remove user From hashmap
                 */
                chatUsers.remove(sc);
                sc.close();
              } catch( IOException ie2 ) { System.out.println( ie2 ); }

              System.out.println( "Closed "+sc );
            }
          }
        }

        // We remove the selected keys, because we've dealt with them.
        keys.clear();
      }
    } catch( IOException ie ) {
      System.err.println( ie );
    }
  }

    // Just read the message from the socket and send it to stdout
    static private boolean processInput( SocketChannel sc ) throws IOException {
        // Read the message to the buffer
        buffer.clear();
        sc.read( buffer );
        buffer.flip();

        // If no data, close the connection
        if (buffer.limit()==0) {
            return false;
        }

        // Decode and print the message to stdout
        String message = decoder.decode(buffer).toString();
        //System.out.print( message );


        /**
         * 
         * Parse incoming message
         */
        parseMessage(sc, message);
    
        return true;
    }

    /**
     * 
     * @param chatRoom - Socket Channel that sent data
     * @param message - Text message
     * @param senderUserName - Username that sent the current message
     */
    public static void sendMessage(String chatRoom, String message, String senderUserName){
      message = message+"\n";
      ByteBuffer response = ByteBuffer.wrap(message.getBytes(charset));

      for(ChatUser user: chatUsers.values()){
        response.rewind();
        if(user.chatRoomName.equals(chatRoom) && !user.nick.equals(senderUserName)){
          try{
            user.userChannel.write(response);
            System.out.println(message);
          }catch(IOException e){
              e.printStackTrace();
          }
        }
      }
    }

    /**
     * Function that send a message to a specific client
     * @param channel - Socket Channel that sent data
     * @param message - Message to the client
     */
    public static void userOutput(SocketChannel channel, String message){
        message = message+"\n";
        ByteBuffer response = ByteBuffer.wrap(message.getBytes(charset));

        try{
            channel.write(response);
            System.out.println(message);
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Function to check if there is another user connected with the given nickname (newNick)
     * @param newNick - A user nickname
     * @return
     */
    public static boolean validateNick(String newNick){
        for(ChatUser user : chatUsers.values()){
            if(user.nick.equals(newNick)){
                return false;
            }
        }
        return true;
    }

    /**
     * Function to update a user nickname
     * @param channel - Socket Channel that sent data
     * @param newNick - a user nickname
     */
    public static void setNewUserNick(SocketChannel channel, String newNick){
        ChatUser user = chatUsers.get(channel);
        String oldNick = user.nick;
        user.nick = newNick;
        if(user.status.equals("init")){
          user.status = "outside";
        }
        if(user.status.equals("inside")){
            sendMessage(user.chatRoomName, "NEWNICK " + oldNick + " " + newNick, user.nick);
        }
    }
    
    /**
     * Function to validate if a user is allowed to join a chat room
     * @param channel - Socket Channel that sent data
     * @return
     */
    public static boolean validateJoin(SocketChannel channel){
      ChatUser user = chatUsers.get(channel);
      if(user.status.equals("init")){
        return false;
      }
      return true;
    }

    /**
     * Function to validate if a user is allowed to send a message
     * @param channel
     * @return
     */
    public static boolean validateUserMessage(SocketChannel channel){
      ChatUser user = chatUsers.get(channel);
      if(user.status.equals("init") || user.status.equals("outside") || user.status.equals("")){
        return false;
      }
      return true;
    }

    /**
     * Function to handle user text messages
     * @param channel - Socket Channel that sent data
     * @param message - String holding a text message
     */
    public static void handleUserMessage(SocketChannel channel, String message){
      ChatUser user = chatUsers.get(channel);
      /**
       * escape / character
       */
      if(message.substring(0, 1).equals("/")){
        message = message.substring(1, message.length());
      }
      message="MESSAGE "+user.nick+" "+ message;
      sendMessage(user.chatRoomName, message, user.nick);
    }

    /**
     * Function that retires a user from the chat room he is currently in
     * @param channel - Socket Channel that sent data
     */
    public static void handleRoomLeave(SocketChannel channel){
      ChatUser user = chatUsers.get(channel);
      String message = "LEFT " + user.nick;
      if(user.status.equals("inside")){
        sendMessage(user.chatRoomName, message, user.nick);
        user.chatRoomName = "";
        user.status = "outside";
      }
    }

    /**
     * Function to disconnect a user from the Chatserver
     * @param channel - Socket Channel that sent data
     */
    public static void handleUserExit(SocketChannel channel){
      handleRoomLeave(channel);
      chatUsers.remove(channel);
      try{
        channel.close();
      }catch(IOException io){
        System.out.println(io);
      }
      System.out.println("Closed: " + channel);
    }

    /**
     * Function that binds a user to a given chat room name
     * If the user is already in a chat room, this function callsback to handleRoomLeave
     *
     * @param channel - Socket Channel that sent data
     * @param newChatRoomName - String representing the name of a chat room
     */
    public static void setUserNewJoin(SocketChannel channel, String newChatRoomName){

      ChatUser user = chatUsers.get(channel);
      String message = "JOINED " + user.nick;
      
      if(user.status.equals("outside")){
        user.chatRoomName=newChatRoomName;
        user.status="inside";
        sendMessage(newChatRoomName, message, user.nick);
      }else if(user.status.equals("inside")){
        handleRoomLeave(channel);
        user.chatRoomName=newChatRoomName;
        user.status="inside";
        sendMessage(newChatRoomName, message, user.nick);
      }
    }

    /**
     * Funcion that send a message to a given user (if it exists)
     * 
     * @param channel - User socket channel that sent the data
     * @param privateTartget - Target user to receive the message
     * @param privateMessage - Text message
     */
    public static void handlePrivateMessage(SocketChannel channel, String privateTartget, String privateMessage){
      ChatUser user = chatUsers.get(channel);
      String message = "PRIVATE " + user.nick + " " + privateMessage;

      if(user.status.equals("init") || user.status.equals("") || privateTartget.equals("") || privateMessage.equals("")){
        userOutput(channel, "ERROR");
      }else{
        if(validateNick(privateTartget)){
          userOutput(channel, "ERROR");
        }else{

          for(ChatUser targetUser: chatUsers.values()){
            if(targetUser.nick.equals(privateTartget)){
              userOutput(targetUser.userChannel, message);
            }
          }
          userOutput(channel, "OK");
        }
      }
    }

    /**
     * Function that handles the chat commands
     * /nick - set your nickname
     * /join - join a chat room
     * /leave - leave a chatroom
     * /bye - exit chatserver
     * /priv - send private message
     * text message - normal text message
     * @param channel - Socket Channel that sent data
     * @param message - Text message from the socketChannel
     */
    public static void parseMessage(SocketChannel channel, String message){

        Scanner sc = new Scanner(message);
        
        if(sc.hasNext()){
          String command = sc.next();
            if(command.equals("/nick")){
              String newNick="";
              if(sc.hasNext())
                newNick = sc.next();

              if(newNick != "" && validateNick(newNick)){
                  //set new nick
                  setNewUserNick(channel, newNick);
                  userOutput(channel, "OK");
              }else{
                  //Error
                  userOutput(channel, "ERROR");
              }

            }else if(command.equals("/join")){
              String newChatRoomName = "";
              if(sc.hasNext())
                newChatRoomName = sc.next();
              
              if(validateJoin(channel)){
                setUserNewJoin(channel, newChatRoomName);
                userOutput(channel, "OK");
              }else{
                //ERROR
                userOutput(channel, "ERROR");
              }

            }else if(command.equals("/leave")){
              handleRoomLeave(channel);
              userOutput(channel, "OK");

            }else if(command.equals("/bye")){
              userOutput(channel, "BYE");
              handleUserExit(channel);
            }else if(command.equals("/priv")){
              
              String privateTarget="";
              String privateMessage="";

              if(sc.hasNext()){
                privateTarget=sc.next();
              }
              if(sc.hasNext()){
                privateMessage = sc.nextLine();
              }
              
              handlePrivateMessage(channel, privateTarget, privateMessage);
              
            }else{
              if(validateUserMessage(channel)){
                handleUserMessage(channel, message);
              }else{
                //ERROR
                userOutput(channel, "ERROR");
              }
            }
        }
        sc.close();
    }
}

