/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;


/**
 *
 * @author vishal
 */
public class PrintOut {
     static  boolean   value = false;
    public  PrintOut(boolean value ) {
         this.value = value;
     }   
    public static void printOutM(String statemnet , int variable ){
        if(value){
            System.out.println("....... ************ ......... " + statemnet + "........ ****** ........... " + variable);
        }
        
    }
    public static void PrintOutLong(String statement , long variable){
        if(value){
            System.out.println("....... ****** ....... " + statement + "....... ******........" + variable);
        }
    }
    public static void PrintOutDouble(String statement , double variable){
        if(value){
            System.out.println("....... ****** ....... " + statement + "....... ******........" + variable);
        }
    }
    public static void PrintChannelBuffer(String statement , ByteBuf buf ){
        if(value){
            System.out.println("....... ****** ....... " + statement + "....... ******........" + ByteBufUtil.hexDump(buf));
        }
    }
    public static void PrintOutString(String statement){
        if(value){
            System.out.println("........ ***** ..........  " + statement);
        }
    }
    
    public static void PrintOutStringString(String statement,String str){
        if(value){
            System.out.println("........ ***** ..........  " + statement + ".......*****......"+ str);
        }
    }

}
