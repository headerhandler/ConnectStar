/*  Copyright (C) 2014 by Oduah Tobi
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *	
 *	The above copyright notice and this permission notice shall be included in
 *	all copies or substantial portions of the Software.
 *	
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *	THE SOFTWARE.
 */
package com.connectstar;

import java.io.IOException;
import java.io.InputStream;

public class Operation {
	public static boolean modemDial(int mode, String connection){
		switch(mode){
			case 0:
				try {
					String command = "rasdial " + "\"" + connection + "\"";
					Process p = Runtime.getRuntime().exec(command);
					p.waitFor();
					if (p.exitValue() != 0)
						return false;
				}catch (IOException e) {
					return false;
				}catch (InterruptedException e){
					return false;
				}
				return true;
				
			case 1:
				try{
					Process p3 = Runtime.getRuntime().exec("rasdial " + "\"" + connection + "\"" + " /disconnect");
					p3.waitFor();
				}catch(IOException e){
					return false;
				}catch (InterruptedException e){
					return false;
				}
				return true;
						
			default:
				try {
					Process p = Runtime.getRuntime().exec("rasdial");
					p.waitFor();
					try{
						InputStream in = p.getInputStream();
						StringBuilder a = new StringBuilder(100);
						int k;
						while ((k = in.read()) != -1){
							a.append((char)k);
							if (a.indexOf("\n") != -1)
								break;
						}
						if (a.toString().trim().equalsIgnoreCase("connected to"))
							return true;
						else if (a.toString().trim().equalsIgnoreCase("no connections"))
							return false;
					}catch(IOException e){
					}
					System.out.println(String.format("Connection error code - %s", p.exitValue()));
				}catch (IOException e) {
					return false;
				}catch (InterruptedException e){
					return false;
				}
				return false;
			}
	}
}
