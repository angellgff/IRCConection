package main;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;

public class Main extends AbstractScript implements IRCListener{
	public static IRCConnection connection;

	public static void main(String[] args) {




	}
		   @Override
		   public void onStart() {
		      connection = new IRCConnection("#mychannel");
		      connection.addListener(this);
		      connection.start();
		   }

		   @Override
		   public void onExit() {
		      connection.closeConnection();
		   }

		   @Override
		   public void messageReceived(IRCMessage message) {
		      String msg = message.getMessage();
		      String sender = message.getSender();

		      if (sender.equals("your_name")) {
		          if (msg.equals(".strlvl")) {
		              connection.sendChannelMessage("My str lvl is " + getSkills().getRealLevel(Skill.STRENGTH));
		          } else if (msg.equals(".stopscript")) {
		              getClient().getInstance().getScriptManager().stop();
		          }
		      }
		   }
		@Override
		public int onLoop() {
			// TODO Auto-generated method stub
			return 0;
		}
		   

}
