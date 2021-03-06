package me.woder.world;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import me.woder.bot.Client;
import me.woder.network.ByteArrayDataInputWrapper;

@SuppressWarnings("unused")
public class Chunk {
    private boolean groundup;
    private boolean lighting;
    private int chunksize;
    private int pbitmap;
    private int x;
    private int z;
    private int[] blockse;
    public List<Part> parts;
    public int blocknum;
    public int nsection;
    private int ablocks;
    private Client c;
    
    public Chunk(Client c, int x, int z, int pbitmap, boolean lighting, boolean groundup) {
        // Create chunk sections.
        this.c = c;
        this.groundup = groundup;
        this.lighting = lighting;
        this.pbitmap = pbitmap;
        this.x = x;
        this.z = z;
        parts = new ArrayList<Part>();

        blocknum = 0;
        nsection = 0;
        ablocks = 0;
        //System.out.println(Integer.toBinaryString(pbitmap));
        for (int i = 0; i < 16; i++) {
            //System.out.println("Bit map = " + (pbitmap & (1 << i)));
            if ((pbitmap & (1 << i)) != 0) {
                nsection++; // "parts of the chunk"
                parts.add(new Part((byte)i, c));
                //System.out.println("Added new part: " + i);
            }
        }

        for (int i = 0; i < 16; i++) {
            if ((pbitmap & (1 << i)) == 1) {
                ablocks++;
            }
        }
        //size of the chunk
        chunksize = nsection * (4096 * (5 + (lighting?1:0)) / 2) + (groundup?256:0);
        // Number of sections * blocks per section = blocks in this "Chunk"
        blocknum = nsection * 4096;
    }
    
    public void fillChunk(){
       int offset = 0;
       int current = 0;
        
       for(int i = 0; i < 16; i++) {
           if ((pbitmap & (1 << i)) != 0) {

                int[] temp = new int[4096];
                System.arraycopy(blockse, offset, temp, 0, 4096);
                Part mySection = parts.get(current);
                mySection.blocks = temp;
                offset += 4096;
                current += 1;
            }
       }
       blockse = null;
        
     }
    
     public int getBlockId(int Bx, int By, int Bz) {
        Part part = GetSectionByNumber(By);
        return part.getBlock(getXinSection(Bx), GetPositionInSection(By), getZinSection(Bz), Bx, By, Bz).getTypeId();
     }

     public Block getBlock(int Bx, int By, int Bz) {
        Part part = GetSectionByNumber(By);
        return part.getBlock(getXinSection(Bx), GetPositionInSection(By), getZinSection(Bz), Bx, By, Bz);    
     }

     public void updateBlock(int Bx, int By, int Bz, int id, int meta) {
        // Updates the block in this chunk.
        // TODO: Ensure that the block being updated is in this chunk.
        // Even though chances of that exception throwing are tiny.

        Part part = GetSectionByNumber(By);    
        part.setBlock(getXinSection(Bx), GetPositionInSection(By), getZinSection(Bz), id, meta);

     }
    
   //Data looks like this now: Block data and Meta (unsigned short), Block light, sky light (if true), biome, (if true)
     public void getData(ByteArrayDataInputWrapper buf) {
        // Loading chunks, network handler hands off the decompressed bytes
        // This function takes its portion, and returns what's left.
        //the size of this chunk
        
        c.chunksloaded = true;
        byte[] temp;
        blockse = readUnsignedShorts(buf);
        //System.out.println("Chunk size is: " + chunksize + " block num is: " + blocknum);
        temp = new byte[chunksize - (blocknum*2)];
        buf.readFully(temp, 0, temp.length);
        //System.arraycopy(deCompressed, blocknum, blockmeta, 0, blocknum/2);
        /*for(int i = 0; i < blocks.length; i++){
            System.out.println("id: " + blocks[i]);
        }*/
        //System.out.println(deCompressed.length);
        //System.out.println("Blocknum" + blocknum + " block num AND removeable: " + (blocknum + removeable));

        fillChunk(); // Populate all of our sections with the bytes we just aquired.

     }
     
     public int[] readUnsignedShorts(ByteArrayDataInputWrapper buf){
         int[] ints = new int[blocknum];
         for(int i = 0; i<blocknum; i++){
             //ints[i] = (buf.readShort() & 0xffff);
             int one = buf.readByte() & 0xff;
             int two = buf.readByte() & 0xff;           
             ints[i] = ((two << 8) | one) & 0xffff;
         }
         return ints;
     }
     
     public Integer getX(){
         return x;
     }
     
     public Integer getZ(){
         return z;
     }
    
     private Part GetSectionByNumber(int blockY) {
         Part thisSection = null;

         for(Part y : parts) {
             if (y.y == blockY / 16) {
                 thisSection = y;
                 break;
             }
         }

         if (thisSection == null) { // Add a new section, if it doesn't exist yet.
             thisSection = new Part((byte)(blockY / 16),c);
             parts.add(thisSection);
         }

         return thisSection;
     }

     private int getXinSection(int BlockX) {
         return BlockX - (x * 16);
     }     

     private int GetPositionInSection(int blockY) {
         return blockY & (16 - 1); // Credits: SirCmpwn Craft.net
     }
     
     private int getZinSection(int BlockZ) {
         return BlockZ - (z * 16);
     }
    
    

}
