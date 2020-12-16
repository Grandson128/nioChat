# nioChat
Small multiplex chat in java using the NIO library. 


Start Chat server with :
java ChatServer <port>
  Ex:
    java ChatServer 8000


Start Client with : 
java ChatClient <host> <port>
  Ex:
    java ChatClient localhost 8000

[PT]

# Estados e transições

<table style="border: 1px solid black;" id="yui_3_17_2_1_1608142609621_55">
   
   <thead>
      <tr>
         <th style="border: 1px solid black;">Estado actual</th>
         <th style="border: 1px solid black;">Evento</th>
         <th style="border: 1px solid black;">Acção</th>
         <th style="border: 1px solid black;">Próximo estado</th>
         <th style="border: 1px solid black;">Notas</th>
      </tr>
   </thead>
   <tbody id="yui_3_17_2_1_1608142609621_54">
      <tr id="yui_3_17_2_1_1608142609621_57">
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;" id="yui_3_17_2_1_1608142609621_56"><code>ERROR</code></td>
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>init</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code><i>nome</i></code> fica indisponível para outros utilizadores</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/join <i>sala</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>JOINED <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">entrou na sala <code><i>sala</i></code>; começa a receber mensagens dessa sala</td>
      </tr>
      <tr id="yui_3_17_2_1_1608142609621_53">
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;" id="yui_3_17_2_1_1608142609621_52"><code id="yui_3_17_2_1_1608142609621_51">ERROR</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;">mantém o nome antigo</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code></td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>mensagem</i></code></td>
         <td style="border: 1px solid black;"><code>MESSAGE <i>nome mensagem</i></code> para todos os utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">necessário escape de / inicial, i.e., / passa a //, // passa a ///, etc.</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; !disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">mantém o nome antigo</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/nick <i>nome</i> &amp;&amp; disponível(<i>nome</i>)</code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>NEWNICK <i>nome_antigo nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code>/join <i>sala</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala antiga<br><code>JOINED <i>nome</i></code> para os outros utilizadores na sala nova</td>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">entrou na sala <code><i>sala</i></code>; começa a receber mensagens dessa sala; deixa de receber mensagens da sala antiga</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/leave</i></code></td>
         <td style="border: 1px solid black;"><code>OK</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;"><code>outside</code></td>
         <td style="border: 1px solid black;">deixa de receber mensagens</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/bye</i></code></td>
         <td style="border: 1px solid black;"><code>BYE</code> para o utilizador<br><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;"><code>inside</code></td>
         <td style="border: 1px solid black;">utilizador fechou a conexão</td>
         <td style="border: 1px solid black;"><code>LEFT <i>nome</i></code> para os outros utilizadores na sala</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>/bye</i></code></td>
         <td style="border: 1px solid black;"><code>BYE</code> para o utilizador</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;">utilizador fechou a conexão</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">—</td>
         <td style="border: 1px solid black;">servidor fecha a conexão ao cliente</td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer excepto <code>inside</code></td>
         <td style="border: 1px solid black;"><code><i>mensagem</i></code></td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;">mantém o estado</td>
         <td style="border: 1px solid black;"></td>
      </tr>
      <tr>
         <td style="border: 1px solid black;">qualquer</td>
         <td style="border: 1px solid black;">comando não suportado nesse estado</td>
         <td style="border: 1px solid black;"><code>ERROR</code></td>
         <td style="border: 1px solid black;">mantém o estado</td>
         <td style="border: 1px solid black;"></td>
      </tr>
   </tbody>
</table>
