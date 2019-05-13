package main;

public class IRCMessage {

    private String message;

	public IRCMessage(String message) { this.message = message; }

    public String getServerMessage() { return this.message; }

    public String getFormatedMessage() { return getSender() + ": "+ getMessage(); }

    public String getSender() { return this.message.split("!~")[0].replace(":", ""); }

    public String getMessage() { return this.message.split(" :")[1]; }
}