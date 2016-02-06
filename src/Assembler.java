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
	public static String blank = "00000";
	public String input;
	public static String line;
	public static String immediate;
	public static String offset;
	public static String rs1;
	public static String rs2;
	public static String rd;
	public static BufferedReader bufferedReader;
	static ArrayList<String[]> symbolTable = new ArrayList<String[]>();
	static ArrayList<String> stringTable = new ArrayList<String>();
	static ArrayList<Integer> dataTable = new ArrayList<Integer>();
	static ArrayList<Float> floatTable = new ArrayList<Float>();
	private Scanner in;
	
	public static ArrayList<String> passOne = new ArrayList<String>();
	
	
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
		bufferedReader.close();
	}
	
	/**
	 * parse - determines if an input string is a label, if not, passes on
	 * 		to moreParse(). Updates the address.
	 * 
	 * @param line the input string
	 */
	public static void parse(String line)
	{
		if (line.contains(":"))
		{
			 isLabel(line);
		}
		else
		{
			moreParse(line);
		}
		address += 4;
	}
	
	public static void moreParse(String line)
	{
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
		 else if (c == 0x3B); // hex ;
		 else if (c == 0x2E) //hex .
		 {
			 directives(line.substring(1));
		 }
		 else
		 {
			 String s;
			 line = instruction(line);
			 if (line.length() < 9)
			 {
				 while (line.length() < 8)
				 {
					 line = "0" + line;
				 }
				 passOne.add(line);
			 }
			 else if (line.length() < 32)
			 {
				 int i = Integer.parseInt(instruction(line), 2) * 2;
				 s = Integer.toString(i, 16);
				 if (s.length() < 8)
					 s = "0" + s;
				 passOne.add(s);
			 }
			 else
			 {
				 s = Integer.toString(Integer.parseInt(line, 2), 16);
				 if (s.length() < 8)
					 s = "0" + s;
				 passOne.add(s);
			 }
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
	 * 		XXXXXXXX: XXXXXXXXXX		or		XXXXXXXXXX:
	 * 		  LABEL      LINE					   LINE
	 * 
	 * @param line the original input string
	 * @return line minus any labels that may exist
	 */
	public static void isLabel(String line)
	{
		int c = 0x3A; // hex :
		c = line.indexOf(c);
		String label = line.substring(0, c);
		String[] s = {label, ((Integer) address).toString()};
		symbolTable.add(s);
	}
	
	/**
	 * nop - returns an operation with an operand and function value of 0x0;
	 * 		i.e., 0x00000000.
	 * 
	 * @return the nop hex code
	 */
	public static String nop()
	{
		return "00000000";
	}
	
	/**
	 * directives - handles all directives. Assumes the period has been stripped from
	 * 		the input string and all char are lower case. Each case is commented separately.
	 * 		Will do nothing if encounters unknown/incorrect directive. 
	 * 
	 * @param line the input string stripped of leading period
	 */
	public static void directives(String line)
	{
		switch (line.charAt(0)) 
		{
		/*
		 * Determines if .align or .asciiz. If .align, determines the decimal value of
		 * 		the lower order bits to mask. Raises [2^(mask size) - 1] to get mask
		 * 		and bitwise and's with address. Incrementally increases address until
		 * 		low order bits are aligned. IF .asciiz, adds strings to stringTable.
		 * 		Assumes values are entered individulally and are NOT marked by
		 * 		either double or single quotes.
		 */
			case 0x61: //hex a
			{
				a(line);
				break;
			}
			/*
			 *  Determines if .data or .double. If data, sets address to given value or 
			 *  	0x200. If double, treats exactly as float. If neither, break.
			 */
			case 0x64: // hex d
			{
				d(line);
				break;
			}
			/*
			 * If it's .float, add numbers as 32-bit floats to floatTable, to be
			 * 		added at end of instruction sequence, until there are no more
			 * 		values left in sequence. Assumes values are entered indivdually. 
			 * 		All numbers are assumed to be entered as floats. If not, break.
			 * 
			 *		.float num1
			 */
			case 0x66: // hex f
			{
				f(line);
				break;
			}
			/*]
			 * If it's .space, increments the address by one byte size for each
			 * 		value specified in size. Assumes that size is given in integer number
			 * 		of byte spaces, NOT instruction size/hex value. If not, break;
			 */
			case 0x73: // hex s
			{
				s(line);
				break;
			}
			/*
			 * If it's .text, set address accordingly. If not, break.
			 */
			case 0x74: // hex t
			{
				t(line);
				break;
			}
			/*
			 * If it's .word, add numbers as 32-bit integers to dataTable, to be
			 * 		added at end of instruction sequence, until there are no more
			 * 		values left in sequence. Assumes values are separated by commas
			 * 		if in sequence, or otherwise only a single value. All numbers are
			 * 		assumed to be entered as Integers. If not, break.
			 * 
			 * 		.word num1
			 */
			case 0x77: // hex w
			{
				w(line);
				break;
			}
			default:
				break;
		}
	}

	/**
	 * instruction - takes on the Sisyphean task of decoding the DLX instructions
	 * 		contains about as many helper methods as you can imagine this taking,
	 * 		which determine which of the various instructions it is and encodes it
	 * 		in the proper format. Assumes all instructions are written in LOWER
	 * 		CASE LETTERS. Assumes all registers are entered as 2-digit values,
	 * 		ie, 09 or 31, with no commas in the instruction. Parenthesis are allowed.
	 * 		Assumes names/labels only occur in J-type instructions 
	 * 
	 * DOES NOT CHECK CHAR BY CHAR FOR CORRECT INSTRUCTION. An incorrect instruction
	 * 		may slip past if it meets certain criteria - ie, "anei" will be read as
	 * 		"andi" because it starts with "a", and contains "i" and "n". 
	 * 
	 * INSTRUCTIONS CONSIDERED TO USE "SPECIAL" REGISTERS INCLUDE: cvtf2d, cvtf2i,
	 * 		cvtd2f, cvtd2i, cvti2f, cvti2d, movfp2i, movi2fp, movi2s, movs2i, rfe, 
	 * 		and trap. 
	 *  	
	 * @param line the input string stripped of any leading spaces, labels, etc.
	 */
	public static String instruction(String line)
	{
		switch(line.charAt(0))
		{
			/*
			 * Determines if instruction is: ADDI, ADDUI, ANDI, ADD, ADDU, AND
			 * 		ADDF, or ADDD. Passes along to the appropriate coder if one 
			 * 		of the above.
			 */
			case 0x61: //hex a
			{
				line = ia(line);
				break;
			}
			/*
			 * Determines if instruction is: BEQZ, BNEZ, BFPT, or BFPF.
			 */
			case 0x62: //hex b
			{
				line = ib(line);
				break;
			}
			/*
			 * Determines if instruction is: DIV, DIVD, DIVF, or DIVU
			 */
			case 0x64: //hex d
			{
				line = id(line);
				break;
			}
			/*
			 * Determines if instruction is EQD or DQF
			 */
			case 0x65:
			{
				line = ie(line);
				break;
			}
			/*
			 * Determines if instruction is GED, GEF, GTD, or GTF
			 */
			case 0x67:
			{
				line = ig(line);
				break;
			}
			/*
			 * Determines if instruction is J, JAL, JALR, or JR
			 */
			case 0x6A:
			{
				line = ij(line);
				break;
			}
			/*
			 * Determines if instruction is LB, LBU, LD, LED, LEF,
			 * 		LF, LH, LHI, LHU, LTD, LTF, or LW.
			 */
			case 0x6C:
			{
				line = il(line);
				break;
			}
			/*
			 * Determines if instruction is MOVD, MOVF, MULT, MULTD
			 * 		MULTF, or MULTU 
			 */
			case 0x6D:
			{
				line = im(line);
				break;
			}
			/*
			 * Determines if instruction is NED, NEF, or NOP
			 */
			case 0x6E:
			{
				line = in(line);
				break;
			}
			/*
			 * Determines if instruction is OR or ORI
			 */
			case 0x6F:
			{
				line = io(line);
				break;
			}
			/*
			 * Determines if instruction is SB, SD, SEQ, SEQI, SF, SGE,
			 * 		SGEI, SGT, SGTI, SH, SLE, SLEI, SLL, SLLI, SLT, SLTI,
			 * 		SNE, SNEI, SRA, SRAI, SRL, SRLI, SUB, SUBD, SUBF, SUBI, 
			 * 		SUBU, SUBUI, or SW 
			 */
			case 0x73:
			{
				line = is(line);
				break;
			}
			/*
			 * Determines if instruction is XOR or XORI
			 */
			case 0x78:
			{
				line = ix(line);
				break;
			}
		}
		return line;
	}
	
	
	
	
	
	
	
	
	
	
	public static void main(String args[]) throws IOException
	{
		//for (int i = 0; i < 12; i++)
		//{
			//addressPrinter();
		//}
		
		//System.out.println(address);
		parse("add r01 r19 r11");
		parse("dog:");
		System.out.println(address);
		parse("add r01 r19 r11");
		parse("add r01 r19 r11");
		parse("add r01 r19 r11");
		parse("add r01 r19 r11");
		//System.out.println(address);
		parse("bfpf dog");
		//System.out.println(address);
		
		for (int i = 0; i < passOne.size(); i++)
		{
			addressPrinter();
			System.out.println(passOne.get(i));
		}
		//parse(".align 4");
		//addressPrinter();
		//System.out.println(passOne.get(1));
	}
	
	/****************************************************************************
	 * Here follows helper methods that are mentioned above. The are properly   *
	 * 		commented here, and do a lot of the grunt work.						*
	 ***************************************************************************/
	
	/**
	 * bType - modified iType, where rs1 is "00000" or rs1 depending on type, rs2
	 * 		is "00000", and immediate is the 16-bit value of the address stored in
	 * 		the stringTable that matches the name/label. 
	 *  
	 * @param line the input line
	 * @return the final 26-bits of the Branch instructions (rs1/unused, unused, 
	 * 		address of name)
	 */
	public static String bType(String line)
	{
		if (line.contains("z"))
		{
			int i = line.indexOf('r');
			rs1 = prettyRegs(i, line);
			line = line.substring(i + 4);
		}
		else
		{
			rs1 = blank;
			line = line.substring(5);
		}
		
		String s[];
		for (int i = 0; i < symbolTable.size(); i++)
		{
			s = symbolTable.get(i);
			if (s[0].equals(line))
			{
				immediate = s[1];
			}
		}	
				
		immediate = largeBit(immediate, 16);
		immediate = "0000" + immediate;
		while (immediate.length() < 6)
		{
			immediate = "0" + immediate;
		}
				
		return Integer.toHexString(Integer.parseInt(immediate));
	}

	/**
	 * eType - identical to rType65, but replaces third 5-bit section with a string
	 * 		of 5 0's, making for an 11=bit unused string (rs1, rs2, 11-bit unused)
	 * 
	 * @param line the input string
	 * @return the 21-bit decoded string (rs1, rs2, unused)
	 */
	public static String eType(String line)
	{
		int i = line.indexOf('r');
		rs1 = prettyRegs(i, line);
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs2 = prettyRegs(i, line);

		return rs1 + rs2 + blank + blank + "0";
	}
	
	/**
	 * iType - takes an input string, finds the first register, stores the value as
	 * 		a 5-bit binary number; finds the second register, stores the value as a
	 * 		5-bit binary number; stores the remainder as a 16-bit binary number.
	 * 
	 * @param line the entire input string
	 * @return a string containing the last 26-bits of a I-Type instruction (rs1, rd,
	 * 		and the immediate concatenated without spaces) 
	 */
	public static String iType(String line)
	{
		int i = line.indexOf('r');
		rd = prettyRegs(i, line);
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = prettyRegs(i, line);
		
		immediate = prettyLarge(i + 4, line.length(), 15, line); 
				
		return rs1 + rd + immediate;
	}
	
	/**
	 * jType - modified bType, which only searches for the name in the symbol Table
	 * 		and returns its 26-bit address.
	 * 
	 * @param line the input string
	 * @return the 26-bit address string (name)
	 */
	public static String jType(String line)
	{
		if (line.contains("l"))
		{
			line = line.substring(4);
		}
		else
		{
			line = line.substring(2);
		}

		String s[];
		for (int i = 0; i < symbolTable.size(); i++)
		{
			s = symbolTable.get(i);
			if (s[0].equals(line))
			{
				immediate = s[1];
			}
		}
		immediate = Integer.toHexString(Integer.parseInt(largeBit(immediate, 24)));
		while (immediate.length() < 6)
		{
			immediate = "0" + immediate;
		}
		
		
		return immediate;
	}
	
	/**
	 * jType2 - modified rType56, which finds rs1, stores it as a 5-bit binary, and
	 * 		adds the remaining 21-bit unused character string.
	 *  
	 * @param line the input string
	 * @return the 26-bit tail of the I-type instructions (rs1, unused)
	 */
	public static String jType2(String line)
	{
		line = line.replaceAll("r ", "t");
		int i = line.indexOf('r');
		rs1 = prettyRegs(i, line);


		return rs1 + blank + blank + blank + blank + "0";
	}
	
	/**
	 * lType - Modified rType65 that takes into account the offsets.
	 * 
	 * @param line the input line
	 * @return the 26-bit string (rs1, rd, offset)
	 */
	public static String lType(String line)
	{
		int i = line.indexOf('r');
		rd = prettyRegs(i, line);

		line = line.substring(i + 4);
		i = line.indexOf('(');
		immediate = prettyLarge(0, i, 15, line);
						
		rs1 = prettyLarge(i + 2, line.length() - 1, 5, line);
		
		return rs1 + rd + immediate;
	}
	
	/**
	 * lType2 - Modified lType that takes into account the immediate
	 * 
	 * @param line the input string
	 * @return the 26-bit string (rs1, rd, immediate)
	 */
	public static String lType2(String line)
	{
		int i = line.indexOf('r');
		rd = prettyRegs(i, line);
		
		immediate = prettyLarge(i + 4, line.length(), 15, line);
		
		return blank + rd + immediate;
	}
	
	/**
	 * mType - modified rType56 that replaces rs2 with a 5-bit unused string.
	 * 
	 * @param line the input string
	 * @return the 20-bit string (rs1, 5-bit unused, rd, 5-bit unused)
	 */
	public static String mType(String line)
	{
		int i = line.indexOf('r');
		rd = prettyRegs(i, line);
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = prettyRegs(i, line);

		return rs1 + blank + rd + blank;
	}
		
	/**
	 * rType56 - takes an input string, finds the first register, stores the value as
	 * 		a 5-bit binary number; finds the second register, stores the value as a
	 * 		5-bit binary number; stores the remainder as a 5-bit binary number; then
	 * 		attaches a 5-bit "00000" unused string to the value. 
	 * 
	 * @param line the entire input string
	 * @return a string containing the last 20-bits of a R-Type instruction (rs1, rs2,
	 * 		rd, and the 5-bit unused string) 
	 */
	public static String rType56(String line)
	{
		int i = line.indexOf('r');
		rd = prettyRegs(i, line);
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = prettyRegs(i, line);
		
		rs2 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 5))));

		return rs1 + rs2 + rd + blank;
	}
	
	/**
	 * rType65 - identical to rType56 in every way, except it adds an extra "0" to the
	 * 		unused bits, so that there are 6 unused bits instead of 5.
	 */ 
	public static String rType65(String line)
	{
		return rType56(line) + "0";
	}
	
	/**
	 * sType - reversal of jType, accounting for offset at the begining
	 * 
	 * @param line the input line
	 * @return the 26-bit string (rs1, rd, immediate)
	 */
	public static String sType(String line)
	{
		int i = line.indexOf('(');
		int t = line.indexOf(' ') + 1;
		immediate = prettyLarge(t, i, 15, line); 
		
		line = line.substring(i);
		rs1 = prettyRegs(i, line);
		
		line = line.substring(i );
		i = line.indexOf('r');
		rd = prettyRegs(i, line);

		return rs1 + rd + immediate;
	}
	
	/**
	 * fiveBit - turns any string of less than 5 characters into a 5 character string
	 * 		with leading 0's.
	 * 
	 * @param reg any register value that needs to be 5-bits in length
	 * @return the 5-bit string
	 */
	public static String fiveBit(String reg)
	{
		return largeBit(reg, 5);
	}
	
	/**
	 * largeBit - does the same as fiveBit, except for lengths longer than 5.
	 * 
	 * @param reg the register that needs to be expanded 
	 * @param l the length the reg needs to be
	 * @return the length-bit string
	 */
	public static String largeBit(String reg, int l)
	{
		while (reg.length() < l)
		{
			reg = "0" + reg;
		}
		return reg;
	}
	
	/**
	 * prettyLarge - the version of prettyRegs for non-five-bit extensions, etc.
	 * 
	 * @param start the start of the substring
	 * @param end the end of the substring
	 * @param length how far to extend the string
	 * @param line the input string
	 * @return the "prettified" string
	 */
	public static String prettyLarge(int start, int end, int length, String line)
	{
		return largeBit(Integer.toBinaryString(Integer.parseInt(line.substring(start, end))), length);
	}
	
	/**
	 * prettyRegs - Takes advantage of a shared code pattern used for many registers.
	 * 		basically does what it says on the tin.
	 * 
	 * @param i the index of the first r/register
	 * @param line the input string
	 * @return the 5-bit string 
	 */
	public static String prettyRegs(int i, String line)
	{
		return fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
	}
	
	/****************************************************************************
	 * Here follows random helper methods who are properly commented where they *
	 * 		first occur in the program. They probably don't need to be their    *
	 * 		methods, but it makes the code look cleaner, so....                 *
	 ***************************************************************************/
	
	/*
	 * Helper methods for directives
	 */
	public static void a(String line)
	{
		int index;
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
				String s = line.substring(7);
				stringTable.add(s.substring(0, s.length()));
			}
		}
	}

	public static void d(String line)
	{
		if (line.charAt(1) == 0x61) // hex a
		{
			if ((line.charAt(2) == 0x74) && (line.charAt(3) == 0x61)) // is it ".data" ?
			{
				if (line.length() < 5) // is it JUST ".data" ?
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
			if ((line.charAt(2) == 0X75) && (line.charAt(3) == 0x62)
					&& (line.charAt(4) == 0x6C) && (line.charAt(5) == 0x65)) // is it ".double" ?
			{
				floatTable.add(Float.parseFloat(line.substring(7)));
			}
	}
	
	public static void f(String line)
	{
		if ((line.charAt(1) == 0x6C) && (line.charAt(2) == 0x6F)
				&& (line.charAt(3) == 0x61) && (line.charAt(4) == 0x74)) // is it ".float" ?
		{
			floatTable.add(Float.parseFloat(line.substring(6)));
		}
	}
	
	public static void s(String line)
	{
		if ((line.charAt(1) == 0x70) && (line.charAt(2) == 0x61)
				&& (line.charAt(3) == 0x63) && (line.charAt(4) == 0x65)) //is it ".space" ?
		{
			int index = Integer.parseInt(line.substring(6));
			for (int i = 0; i < index; i++)
			{
				address++;
			}
		}
	}
	
	public static void t(String line)
	{
		if ((line.charAt(1) == 0x65) && (line.charAt(2) == 0x78)
				&& (line.charAt(3) == 0x74)) // is it ".text" ?
		{
			if (line.length() < 5) // is it JUST ".text" ?
			{
				address = 00;
			}
			else // otherwise is ".text XXXX"
			{
				address = Integer.parseInt(line.substring(5), 16);
			}
		}
	}
		
	public static void w(String line)
	{
		if ((line.charAt(1) == 0x6F) && (line.charAt(2) == 0x72)
				&& (line.charAt(3) == 0x64)) // is it ".word" ?
		{
			dataTable.add(Integer.parseInt(line.substring(5)));
		}
	}
	
	/*
	 * Helper methods for instructions
	 */
	public static String ia(String line)
	{
		String s;
		
		if (line.contains("i"))
		{
			if (line.contains("n")) //ANDI
			{
				s = "001100";
			}
			else if (line.contains("u")) //ADDUI
			{
				s = "001001";
			}
			else //ADDI
			{
				s = "001000";
			}
			line = s + iType(line);
		}
		else
		{
			s = blank;
			String t = rType56(line);
			if (line.charAt(2) == 0x6E) //AND
			{
				s += "0" + t + "100101";
			}	
			else if (line.charAt(3) == 0x66) //ADDF
			{
				s += "1" + t + "00000";
			}
			else if (line.charAt(3) == 0x75) // ADDU
			{
				s += "0" + t + "100001";
			}
			else if (line.charAt(3) == 0x64) // ADDD
			{
				s += "1" + t + "00100";
			}
			else //ADD
			{
				s += "0" + t + "100000";
			}
			line = s;
		}
		
		return line;
	}
	
	public static String ib(String line)
	{
		String s;
		String t = bType(line);
		while (t.length() < 6)
		{
			t = "0" + t;
		}
		
		if (line.contains("q")) //BEQZ
		{
			if (t.charAt(0) > '7')
			{
				s = "09" + (t.charAt(0) - 1) + t.substring(1);
			}
			else
			{
				s = "08" + t;
			}
		}
		else if (line.contains("z")) //BNEZ
		{
			if (t.charAt(0) > '7')
			{
				s = "0b" + (t.charAt(0) - 1) + t.substring(1);
			}
			else
			{
				s = "0a" + t;
			}
		}
		else if (line.contains("t")) // BFPT
		{
			if (t.charAt(0) > '7')
			{
				s = "0d" + (t.charAt(0) - 1) + t.substring(1);
			}
			else
			{
				s = "0c" + t;
			}
		}
		else //BFPF
		{
			if (t.charAt(0) > '7')
			{
				s = "0f" + (t.charAt(0) - 1) + t.substring(1);
			}
			else
			{
				s = "0d" + t;
			}
		}
		
		return s;
	}
	
	public static String id(String line)
	{
		String s = "000001";
		String t = rType65(line);
		
		if (line.contains("f")) // DIVF
		{
			s += t + "00011";
		}
		else if (line.contains("u")) //DIVU
		{
			s += t + "10111";
		}
		else if (line.charAt(3) == 'd') // DIVD
		{
			s += t + "00111";
		}
		else //DIV
		{
			s += t + "01111";
		}
		
		return s;
	}
		
	public static String ie(String line)
	{
		String s = "000001";
			String t = eType(line);
			if (line.contains("d")) //EQD
			{
				s += t + "11000";
			}	
			else //EQF
			{
				s += t + "10000";
			}
		line = s;
		
		return line;
	}
	
	public static String ig(String line)
	{
		String s = "000001";
		String t = eType(line);
		if (line.contains("e"))
		{
			if (line.contains("d")) //GED
			{
				s += t + "11101";
			}
			else //GEF
			{
				s += t + "10101";
			}
		}
		else
		{
			if (line.contains("d")) //GTD
			{
				s += t + "11011";
			}
			else //GTF
			{
				s += t + "10011";
			}
		}
		line = s;
		return line;
	}
	
	public static String ij(String line)
	{
		if (line.contains("a"))
		{
			if (line.contains("alr")) //JALR
			{
				line = "010011" + jType2(line);
			}
			else //JAL
			{
				line = "C" + jType(line);
			}
		}
		else
		{
			if (line.contains("jr")) //JR
			{
				line = "010010" + jType2(line);
			}
			else //J
			{
				line = "4" + jType(line);
			}
		}
		return line;
	}
	
	public static String il(String line)
	{
		String s = blank + "1";
		String t;
		
		if (line.contains("e"))
		{
			t = eType(line);
			if (line.contains("d")) //LED
			{
				s += t + "11100";
			}
			else //LEF
			{
				s += t + "10100";
			}
		}
		else if (line.contains("t"))
		{
			t = eType(line);
			if (line.contains("d")) //LTD
			{
				s += t + "11010";
			}
			else //LTF
			{
				s += t + "10010";
			}	
		}
		else if (line.contains("f"))
		{
			if (line.contains("e")) //LEF
			{
				t = rType65(line);
				s += "1" + t + "10100";
			}
			else //LF
			{
				s = "100110" + lType(line);
			}	
		}
		else if (line.contains("b"))
		{
			t = lType(line);
			if (line.contains("u")) //LBU
			{
				s = "100100" + t;
			}
			else //LB
			{
				s = "100000" + t;
			}
		}
		else if (line.contains("h"))
		{
			if (line.contains("u")) //LHU
			{
				s = "100101" + lType(line);
			}
			else if (line.contains("i")) //LHI
			{
				s = "100000" + lType2(line);
			}
			else //LH
			{
				s = "100001" + lType(line);
			}
		}
		else if (line.contains("w")) //LW
		{
			return "100000" + lType(line);
		}
		else //LD
		{
			s = "100111" + lType(line);
		}
		return s;
	}
	
	public static String im(String line)
	{
		String s = blank + "1";
		
		if (line.contains("i2") || line.contains("2i"))
		{
			s = "";
		}
		else if (line.contains("v"))
		{
			if (line.contains("d")) //MOVD
			{
				line = "blank" + "0" + mType(line) + "1000011";
			}
			else //MOVF
			{
				line = "blank" + "0" + mType(line) + "1000010";
			}
		}
		else if (line.contains("f")) // MULTF
		{
			line = s + rType65(line) + "00010";
		}
		else if (line.contains("d")) // MULTD
		{
			line = s + rType65(line) + "00110";
		}
		else if (line.charAt(4) == 'u') // MULTU
		{
			line = s + rType65(line) + "10110";
		}
		else //MULT
		{
			line = s + rType65(line) + "01110";
		}
		
		return line;
	}
	
	public static String in(String line)
	{
		String s = "000001";
		
		if (line.contains("p")) // NOP
		{
			line = "00000000000000000000000000000000";
		}
		else if (line.contains("f")) //NEF
		{
			line = s + eType(line) + "10001";
		}
		else //NED
		{
			line = s + eType(line) + "11001";
		}
		return line;
	}
	
	public static String io(String line)
	{
		if (line.contains("i")) //ORI
		{
			line = "001101" + iType(line.replaceAll("ri", "t"));
		}
		else //OR
		{
			line = "000000" + rType56(line.replaceAll("r ", "t")) + "100101";
		}
		
		return line;
	}
	
	public static String is(String line)
	{
		String s = blank;
		
		if (line.contains("h")) // SH
		{
			line = "101001" + sType(line);
		}
		else if (line.contains("w")) // SW
		{
			line = "101011" + sType(line);
		}
		else if (line.contains("q"))
		{
			if (line.contains("i")) // SEQI
			{
				line = "011000" + iType(line);
			}
			else // SEQ
			{
				line = s + "0" + rType56(line) + "101000";
			}
		}
		else if (line.contains("b"))
		{
			line = isb(line);
		}
		else if (line.contains("g"))
		{
			if (line.contains("sgti")) //SGTI
			{
				line = "011011" + iType(line);
			}
			else if (line.contains("sgt")) // SGT
			{
				line = s + "0" + rType56(line) + "101011";
			}
			else if (line.contains("sgei")) // SGEI
			{
				line = "011101" + iType(line);
			}
			else //SGE
			{
				line = s + "0" + rType56(line) + "101101";
			}
		}
		else if (line.contains("sr"))
		{
			if (line.contains("li")) // SRLI
			{
				line = "010110" + iType(line.replaceAll("rl", "t"));
			}
			else if (line.contains("l")) //SRL
			{
				line = s + "0" + rType56(line.replaceAll("rl", "t")) + "000110";
			}
			else if (line.contains("i")) //SRAI
			{
				line = "010111" + iType(line.replaceAll("ra", "t"));
			}
			else // SRA
			{
				line = s + "0" + rType56(line.replaceAll("ra", "t")) + "000111";
			}
		}
		else if (line.contains("l"))
		{
			line = isl(line);
		}
		else if (line.contains("n"))
		{
			if (line.contains("i")) //SNEI
			{
				line = "011001" + iType(line);
			}
			else //SNE
			{
				line = s + "0" + rType56(line) + "101001";
			}
		}
		else if (line.contains("d")) //SD
		{
			line = "101111" + sType(line);
		}
		else //SF
		{
			line = "101110" + iType(line);
		}
		return line;
	}
	
	public static String isb(String line)
	{
		String s = blank;
		
		if (line.contains("d")) // SUBD
		{
			line = s + "1" + rType65(line) + "00101";
		}
		else if (line.contains("f")) // SUBF
		{
			line = s + "1" + rType65(line) + "00001";
		}
		else if (line.charAt(3) == 'i') //SUBI
		{
			line = "001010" + iType(line);
		}
		else if (line.charAt(4) == 'i') // SUBUI
		{
			line = "001011" + iType(line);
		}
		else if (line.charAt(3) == 'u') //SUBU
		{
			line = s + "0" + rType56(line) + "100011";
		}
		else if (line.charAt(1) == 'u')// SUB
		{
			line = s + "0" + rType56(line) + "100010";
		}
		else //SB
		{
			line = "101000" + sType (line);
		}
		return line;
	}
	
	public static String isl(String line)
	{
		String t;
		if (line.contains("i"))
		{
			t = iType(line);
			if (line.contains("e")) //SLEI
			{
				line = "011100" + t;
			}
			else if (line.contains("t")) //SLTI
			{
				line = "011010" + t;
			}
			else //SLLI
			{
				line = "001100" + t;
			}
		}
		else
		{
			t = rType56(line);
			if (line.contains("e")) // SLE
			{
				line = t + "101110";
			}
			else if (line.contains("t")) // SLT
			{
				line = t + "101010";
			}
			else //SLL
			{
				line = t + "000100";
			}		
		}
		return line;
	}
	
	public static String ix(String line)
	{	
		if (line.contains("i")) //XORI
		{
			line = "001110" + iType(line.replaceAll("ri", "t"));
		}
		else //XOR
		{
			line = "000000" + rType56(line.replaceAll("r ", "t")) + "100110";
		}
		return line;
	}
	
}
