# Websocket Chatty Echo Server

A simple websocket echo server, with the ability to to be 'chatty', meaning it echoes back the received message 'n' times with a configurable delay of 'x' milliseconds. This server can be used for testing purposes in scenarios were multi-message generation is needed.

The server uses a specific API in order to generate the multiple messagse. A user should send a JSON message similar to the below:

```json
{
    "msg": "The message to echo back",
    "times": 20, //How many times to echo back the message
    "delay": 200 // Delay in milliseconds between messages
}
``` 

For example, for the above message, the server would send back to the user the text 'The message to echo back', 20 time, with a delay of 200milliseconds between the messages.

## Build
The project can be build using ```mvn install```. The only caveat is that it has a dependency to projects in this repository (they are not published in Maven Central, hence someone would need to clone and build those projects locally) https://github.com/nikkatsa/nk-jutil

## Run
The server's main class is ```com.nikoskatsanos.chatty.echo.ChattyEchoServer``` and it needs a command line argument of ```-p```  or ```--port``` specifying the port that the server will use. It can be run as a normal java application. The build also produces an appassembler folder under ```target/appassembler/bin```, where a script ```ChattyEchoServer.sh``` exists