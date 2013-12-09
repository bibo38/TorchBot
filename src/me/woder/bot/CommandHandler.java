package me.woder.bot;

import java.io.IOException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.woder.network.Packet;
import me.woder.world.Block;
import me.woder.world.Location;

public class CommandHandler {
    Client c;
    public ImportCommand impor = new ImportCommand();
    
    public CommandHandler(Client c){
        this.c = c;
    }
    
    public void processCommand(String command, String[] args, String username){
        if(command.equalsIgnoreCase("help")){
            commandHelp(args, username); 
        }else if(command.equalsIgnoreCase("under")){
            Block b = c.whandle.getWorld().getBlock(c.location).getRelative(0, -2, 0);
            if (b != null) {
                c.chat.sendMessage("Block is: " + b.getTypeId() + " and its meta data is: " + b.getMetaData());
            } else {
                c.chat.sendMessage("Failed :(");
            }     
        }else if(command.equalsIgnoreCase("version")){
            c.chat.sendMessage(c.versioninfo);
        }else if(command.equalsIgnoreCase("respawn")){
            try {
                ByteArrayDataOutput buf = ByteStreams.newDataOutput();
                Packet.writeVarInt(buf, 22);
                buf.writeByte(0);
                Packet.sendPacket(buf, c.out);
                c.chat.sendMessage("Respawned!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(command.equalsIgnoreCase("reload")){
            c.ploader.reloadPlugins();
        }else if(command.equalsIgnoreCase("export")){
            impor.processCommand(c, args);
        }else if(command.equalsIgnoreCase("import")){
            c.whandle.getWorld().importer.importb(c, args[1]);
        }else if(command.equalsIgnoreCase("place")){
            c.whandle.getWorld().placeBlock(c.location.getBlockX()+1, c.location.getBlockY(), c.location.getBlockZ(), 3);
        }else{
            c.ehandle.handleCommand(command, args);
        }
    }
    
    public void processConsoleCommand(String message){
        if(message.startsWith("/")){
            //TODO do stuff with commands
            AttributeSet attribute = c.gui.attributes.get(0);
            try {
                c.gui.doc.insertString(c.gui.doc.getLength(), message, attribute);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            c.chat.sendMessage(message);
        }else{
            c.chat.sendMessage(message);         
        }
    }
    
    public void commandHelp(String[] messages, String sender){    
        /*if (messages.length < 2){
            sendMessage(channel, "Catagorys are: op, fun, normal and plugin <Bot created by woder>"); 
        }else if (messages[1].equalsIgnoreCase("op")){
            sendMessage(channel, "Commands are: mod, unmod, kick, kickban, unban");
        }else if (messages[1].equalsIgnoreCase("normal")){
            sendMessage(channel, "Commands are: echo, time, isup, mcping, haspaid, calc(not implemented yet)");
        }else if (messages[1].equalsIgnoreCase("fun")){
            sendMessage(channel, "Commands are: slap, roulette, eightball"); */
      if(messages.length > 1){
        if (messages[1].equalsIgnoreCase("plugin")){
            String append = "";
            for(int z = 0; z < c.cmds.length; z++){
               if(z == 0){
                append = c.cmds[z] + " - " + c.descriptions[z];
               }else{
                append += ", " + c.cmds[z] + " - " + c.descriptions[z];
               }
            }
            c.chat.sendMessage(sender + ": " + append);
        }
      }else{
        c.chat.sendMessage("Catagorys are: op, fun, normal and plugin <Bot created by woder>");
      }
    }


}
