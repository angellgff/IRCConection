package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodContext;

public class IRCConnection
extends Thread {

BufferedWriter writer;
private String server = "irc.SwiftIRC.net";
Socket socket;
private boolean running = true;

private ArrayList<IRCListener> listeners;
private String username;
private String channel;
private String channelPassword;

public IRCConnection(String channel) {
	this.listeners = new ArrayList<>();
	this.channel = channel;
	this.username = generateIRCName();
	this.channelPassword = "";
}

public IRCConnection(String channel, String channelPassword) {
	this.listeners = new ArrayList<>();
	this.channel = channel;
	this.username = generateIRCName();
	this.channelPassword = channelPassword;
}

public IRCConnection(String username, String channel, String channelPassword){
	this.listeners = new ArrayList<>();
	this.username = username;
	this.channel = channel;
	this.channelPassword = channelPassword;
}

public IRCConnection(String server, String username, String channel, String channelPassword){
	this.listeners = new ArrayList<>();
	this.channel = channel;
	this.username = username;
	this.server = server;
	this.channelPassword = channelPassword;
}

@Override
public void run(){
try {
    socket = new Socket(server, 8000);
    writer = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream()));
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

    writer.write("NICK " + username + "\r\n");
    writer.write("USER " + username + " 8 * : IRC bot\r\n");
    writer.flush();

    final long timeout = System.currentTimeMillis() + 10000;

    String line;
    while ((line = reader.readLine()) != null) {
        if (System.currentTimeMillis() >= timeout) {
            MethodContext.log("Connection timed out");
            break;
        }
        if (line.contains("004")) {
            break;
        } else if (line.contains("433")) {
            MethodContext.log("Nickname is already in use");
            return;
        }
    }

    writer.write("JOIN " + channel + " " + channelPassword + "\r\n");
    writer.flush();

    while ((line = reader.readLine()) != null) {
        if(!running)
            break;

        if(line.contains("PING ")) {
            writer.write("PONG " + line.substring(5) + "\n");
            writer.flush();
        } else {
            addTrigger(new IRCMessage(line));
       }
    }
} catch(Exception e) {
    e.printStackTrace();
}
}

public void addListener(IRCListener ircListener) { listeners.add(ircListener); }

public void removeListener(IRCListener ircListener) { listeners.remove(ircListener); }

private void addTrigger(IRCMessage message){
for (IRCListener listener : listeners)
    listener.messageReceived(message);
}

public void closeConnection() {
MethodContext.log("Closing connection");
while (!socket.isClosed()) {
    try {
        socket.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
running = false;

MethodContext.log("Connection closed");
}

private void send(String buffer) {
try {
    this.writer.write(buffer);
    this.writer.flush();
} catch (IOException e) {
    e.printStackTrace();
}
}

public void sendChannelMessage(String message) { this.send("PRIVMSG " + this.channel + " :" + message + "\n"); }

public void sendPrivateMessage(String name, String message) { this.send("PRIVMSG " + name + " :" + message + "\n"); }

public void sendNotice(String target, String message) { this.send("NOTICE " + target + " :" + message + "\n"); }

private String getRSN() {
return Client.getClient().getLocalPlayer().getName();
}

private String generateIRCName() {
String outputName;
int t = 0;
for (int i = 0; i < getRSN().length(); i++) {
    if(Character.isDigit(getRSN().charAt(i)))
        t++;
    else
        break;
}
outputName = getRSN().substring(t);

if (outputName.contains(" "))
    outputName = outputName.replaceAll(" ", "_");

return outputName;
}
}