/**
 * Project I: DLX Assembler
 * 		An Assembler for the DLX instruction set, using the instruction layouts found in
 * 		_The DLX Instruction Set Architecture Handbook_ by Sailer and Kaeli, omitting all 
 * 		instructions that modify or use a special register and/or use a trap, and including a
 * 		nop R-Type instruction with an opcode and function code of 0. 
 *  
 * @author Nicole Loew
 * @version CS5483-101 Computer Architecture Spring 2016; 7 February 2016
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Assembler 
{
	public static int address = 0;
	public static int data = 0x200;
	public String input;
	public String line;
	public static BufferedReader bufferedReader;
	ArrayList<String[]> symbolTable = new ArrayList<String[]>();
	ArrayList<String> stringTable = new ArrayList<String>();
	ArrayList<Integer> dataTable = new ArrayList<Integer>();
	ArrayList<Float> floatTable = new ArrayList<Float>();
	private Scanner in;
	
	
	
	
	public Assembler()
	{
	}
	
	
	/**
	 * addressPrinter - a method that prints the 8-digit hex value of the
	 * 		address and then updates the address by 0x4. The format will
	 * 		appear as follows, with a trailing space:
	 * 
	 * 		XXXXXXXX: 
	 */
	public static void addressPrinter()
	{
		System.out.printf("%08d", Integer.parseInt(Integer.toHexString(address), 16));
		System.out.print(": ");
		address += 4;
	}
	
	/**
	 * getsInput - a method that reads the input file name from the console
	 * 		and opens the File/Buffered Reader.
	 * 
	 * @throws FileNotFoundException
	 */
	public void getInput() throws FileNotFoundException
	{
		in = new Scanner(System.in);
		input = in.nextLine();
		in.close();
		FileReader fileReader = new FileReader(input);
		bufferedReader = new BufferedReader(fileReader);
	}

	/**
	 * readLine - uses BufferedReader to read input file line by line
	 * 
	 * @throws IOException
	 */
	public void readLine() throws IOException
	{
		while((line = bufferedReader.readLine()) != null)
		{
			parse(line);
		}
	}
	
	
	
	
	public void parse(String line)
	{
		 line = isLabel(line);
		 
		 int index = 0;
		 char c = line.charAt(index);
		 
		 while (c == 0x20) // hex " "
		 {
			 c = line.charAt(index + 1);
		 }
		 
		 if (c == 0x00) // NULL
		 {
			 line = nop();
		 }
		 else if (c == 0x2E) //hex .
		 {
			 directives(line.substring(1));
		 }
		 else
		 {
			 //instructions
		 }
		 
	}
	
	
	/**
	 * isLabel - determines if the instruction begins with a label. If so,
	 * 		it determines the index of the colon, strips everything that
	 * 		occurs before it into its own string before passing it and the
	 * 		instruction address to the symbolTable, and returns everything that
	 * 		occurs after the colon to parse(); otherwise it returns parse
	 * 		unchanged, as shown below:
	 * 
	 * 		XXXXXXXX: XXXXXXXXXX		or		XXXXXXXXXX
	 * 		  LABEL      LINE					   LINE
	 * 
	 * @param line the original input string
	 * @return line minus any labels that may exist
	 */
	public String isLabel(String line)
	{
		if (line.contains(":"))
		{
			int c = 0x3A; // hex :
			c = line.indexOf(c);
			String label = line.substring(0, c);
			String[] s = {label, ((Integer) address).toString()};
			symbolTable.add(s);
			line = line.substring(c + 1);
		}
		return line;
	}
	
	
	/**
	 * nop - returns an operation with an operand and function value of 0x0;
	 * 		i.e., 0x00000000.
	 * 
	 * @return the nop hex code
	 */
	public String nop()
	{
		String s = "00000000";
		return s;
	}
	
	/**
	 * directives - handles all directives. Assumes the period has been stripped from
	 * 		the input string and all char are lower case. Each case is commented separately.
	 * 		Will do nothing if encounters unknown/incorrect directive. 
	 * 
	 * @param line the input string stripped of leading period
	 */
	public void directives(String line)
	{
		char c = line.charAt(0);
		String s;
		int index;
		
		switch (c) 
		{
		/*
		 * Determines if .align or .asciiz. If .align, determines the decimal value of
		 * 		the lower order bits to mask. Raises [2^(mask size) - 1] to get mask
		 * 		and bitwise and's with address. Incrementally increases address until
		 * 		low order bits are aligned. IF .asciiz, adds strings to stringTable.
		 * 		Assumes values are separated by commas if in sequence, or otherwise 
		 * 		only a single value. All strings are marked by 
		 */
			case 0x61: //hex a
			{
				if (line.charAt(1) == 0x6C) // hex l
				{
					if ((line.charAt(2) == 0x69) && (line.charAt(3) == 0x67)
							&& (line.charAt(4) == 0x6E)) //is it ".align" ?
					{
						index = ((int) Math.pow(2, Integer.parseInt(line.substring(6)))) - 1;
						while ((address & index) != 0)
						{
							address++;
						}
					}
				}
				if (line.charAt(1) == 0x73) // hex s
				{
					if ((line.charAt(2) == 0x63) && (line.charAt(3) == 0x69)
							&& (line.charAt(4) == 0x69) && (line.charAt(5) == 0x7A)) // is it ".asciiz" ?
					{
						s = line.substring(8);
						CharSequence seq = ","; // 0x2C
						while (s.contains(seq)) // while there are multiple strings
						{
							s = seq.toString();
							index = s.indexOf(0x2C);
							stringTable.add(s.substring(0, index - 1));
							seq = seq.subSequence(index + 3, seq.length());
						}
						s = seq.toString();
						stringTable.add(s.substring(0, s.length() - 1)); //add remaining string
					}
				}
				break;
			}
			/*
			 *  Determines if .data or .double. If data, sets address to given value or 
			 *  	0x200. If double, treats exactly as float. If neither, break.
			 */
			case 0x64: // hex d
			{
				if (line.charAt(1) == 0x61) // hex a
				{
					if ((line.charAt(2) == 0x74) && (line.charAt(3) == 0x61)) // is it ".data" ?
					{
						if (line.charAt(4) == 0x00) // is it JUST ".data" ?
						{
							address = data;
						}
						else // otherwise is ".data XXXX"
						{
							address = Integer.parseInt(line.substring(5), 16);
						}
					}
				}
				if (line.charAt(1) == 0x6F) // hex 0
					if ((line.charAt(2) == 75) && (line.charAt(3) == 0x62)
							&& (line.charAt(4) == 0x6C) && (line.charAt(5) == 0x65)) // is it ".double" ?
					{
						s = line.substring(7);
						CharSequence seq = ","; // 0x2C
						while (s.contains(seq)) // while there are multiple numbers
						{
							s = seq.toString();
							index = s.indexOf(0x2C);
							floatTable.add(Float.parseFloat(s.substring(0, index)));
							seq = seq.subSequence(index + 2, seq.length());
						}
						floatTable.add(Float.parseFloat(seq.toString())); //add remaining number
					}
				break;
			}
			/*
			 * If it's .float, add numbers as 32-bit floats to floatTable, to be
			 * 		added at end of instruction sequence, until there are no more
			 * 		values left in sequence. Assumes values are separated by commas
			 * 		if in sequence, or otherwise only a single value. All numbers are
			 * 		assumed to be entered as floats. If not, break.
			 * 
			 * 		.float num1, num2, num3		or		.float num1
			 */
			case 0x66: // hex f
			{
				if ((line.charAt(1) == 0x6C) && (line.charAt(2) == 0x6F)
						&& (line.charAt(3) == 0x6a) && (line.charAt(4) == 0x74)) // is it ".float" ?
				{
					s = line.substring(6);
					CharSequence seq = ","; // 0x2C
					while (s.contains(seq)) // while there are multiple numbers
					{
						s = seq.toString();
						index = s.indexOf(0x2C);
						floatTable.add(Float.parseFloat(s.substring(0, index)));
						seq = seq.subSequence(index + 2, seq.length());
					}
					floatTable.add(Float.parseFloat(seq.toString())); //add remaining number
				}
				break;
			}
			/*]
			 * If it's .space, increments the address by one byte size for each
			 * 		value specified in size. Assumes that size is given in integer number
			 * 		of byte spaces, NOT instruction size/hex value. If not, break;
			 */
			case 0x73: // hex s
			{
				if ((line.charAt(1) == 0x70) && (line.charAt(2) == 0x61)
						&& (line.charAt(3) == 0x63) && (line.charAt(4) == 0x65)) //is it ".space" ?
				{
					index = Integer.parseInt(line.substring(6));
					for (int i = 0; i < index; i++)
					{
						address++;
					}
				}
				break;
			}
			/*
			 * If it's .text, set address accordingly. If not, break.
			 */
			case 0x74: // hex t
			{
				if ((line.charAt(1) == 0x65) && (line.charAt(2) == 0x78)
						&& (line.charAt(3) == 0x74)) // is it ".text" ?
				{
					if (line.charAt(4) == 0x00) // is it JUST ".text" ?
					{
						address = 0x0;
					}
					else // otherwise is ".text XXXX"
					{
						address = Integer.parseInt(line.substring(5), 16);
					}
				}
				break;
			}
			/*
			 * If it's .word, add numbers as 32-bit integers to dataTable, to be
			 * 		added at end of instruction sequence, until there are no more
			 * 		values left in sequence. Assumes values are separated by commas
			 * 		if in sequence, or otherwise only a single value. All numbers are
			 * 		assumed to be entered as Integers. If not, break.
			 * 
			 * 		.word num1, num2, num3		or		.word num1
			 */
			case 0x77: // hex w
			{
				if ((line.charAt(1) == 0x6F) && (line.charAt(2) == 0x72)
						&& (line.charAt(3) == 0x64)) // is it ".word" ?
				{
					s = line.substring(5);
					CharSequence seq = ","; // 0x2C
					while (s.contains(seq)) // while there are multiple numbers
					{
						s = seq.toString();
						index = s.indexOf(0x2C);
						dataTable.add(Integer.parseInt(s.substring(0, index)));
						seq = seq.subSequence(index + 2, seq.length());
					}
					dataTable.add(Integer.parseInt(seq.toString())); //add remaining number
				}
				break;
			}
			default:
				break;
		}
		
		
		
		
		
	}
	
	
	
	
	
	
	public static void main(String args[]) throws IOException
	{
		//for (int i = 0; i < 12; i++)
		//{
			//addressPrinter();
		//}
		
		bufferedReader.close();
	}
}
