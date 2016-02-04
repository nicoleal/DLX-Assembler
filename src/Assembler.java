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
	
	/**
	 * parse - determines if an input string is a label, nop, directive
	 * 		or instruction. Has about five dozen helper methods to do
	 * 		this, so more grand central than anything else.
	 * 
	 * @param line the input string
	 */
	public static void parse(String line)
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
		 else if (c == 0x3B); // hex ;
		 else if (c == 0x2E) //hex .
		 {
			 directives(line.substring(1));
		 }
		 else
		 {
			 line = Integer.toString(Integer.parseInt(instruction(line), 2), 16);
			 System.out.println(line);
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
	public static String isLabel(String line)
	{
		if (line.contains(":"))
		{
			int c = 0x3A; // hex :
			c = line.indexOf(c);
			String label = line.substring(0, c);
			String[] s = {label, ((Integer) address).toString()};
			symbolTable.add(s);
			
			if (line.length() <= c) 
			{
				line = line.substring(c + 1);
			}
		}
		return line;
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
			 * 		.word num1, num2, num3		or		.word num1
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
	 * 		ie, 09 or 31, with no commas in the instruction.
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


	public static String im(String line)
	{
		String s = "000000";
		
		if (line.contains("i2") || line.contains("2i"))
		{
			s = "";
		}
		else if (line.contains("v"))
		{
			if (line.contains("d")) //MOVD
			{
				line = s + mType(line) + "1000011";
			}
			else //MOVF
			{
				line = s + mType(line) + "1000010";
			}
		}
		else if (line.contains("u")) // MULTU
		{
			line = s + rType65(line) + "10110";
		}
		else if (line.contains("f")) // MULTF
		{
			line = s + rType65(line) + "00010";
		}
		else if (line.contains("d")) // MULTD
		{
			line = s + rType65(line) + "00110";
		}
		else //MULT
		{
			line = s + rType65(line) + "01110";
		}
		
		return line;
	}

	
	
	
	public static String lType(String line)
	{
		int i = line.indexOf('r');
		rd = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));

		line = line.substring(i + 4);
		i = line.indexOf('(');
		immediate = largeBit(Integer.toBinaryString(Integer.parseInt(line.substring(0, i))), 16);
		
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 2, line.length() - 1))));
		
		return rs1 + rd + immediate;
	}
	
	
	public static String lType2(String line)
	{
		int i = line.indexOf('r');
		rd = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
				
		rs1 = "00000";
		
		immediate = (Integer.toBinaryString(Integer.parseInt(line.substring(i + 4))));
		while (immediate.length() < 16)
		{
			immediate = "0" + immediate;
		}
		
		return rs1 + rd + immediate;
	}
	

	
	public static void main(String args[]) throws IOException
	{
		//for (int i = 0; i < 12; i++)
		//{
			//addressPrinter();
		//}
		
		System.out.println(address);
		parse("multf r05 r13 r31");
		//System.out.println(immediate);
		//parse(".align 4");
		//System.out.println(address);
		
		//bufferedReader.close();
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
			rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
			line = line.substring(i + 3);
		}
		else
		{
			rs1 = "00000";
			line = line.substring(5);
		}
		
		rs2 = "00000";
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
			
		return rs1 + rs2 + immediate;
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
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs2 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));

		return rs1 + rs2 + "00000000000";
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
		rd = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		immediate = largeBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 4))), 16);

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
		
		return largeBit(immediate, 26);
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
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));

		return rs1 + "000000000000000000000";
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
		rd = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));

		return rs1 + "00000" + rd + "00000";
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
		rd = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		line = line.substring(i + 3);
		i = line.indexOf('r');
		rs1 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 1, i + 3))));
		
		rs2 = fiveBit(Integer.toBinaryString(Integer.parseInt(line.substring(i + 5))));

		return rs1 + rs2 + rd + "00000";
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
			s = "000000";
			String t = rType56(line);
			if (line.charAt(2) == 0x6E) //AND
			{
				s += t + "100101";
			}	
			else if (line.charAt(3) == 0x66) //ADDF
			{
				s += t + "00000";
			}
			else if (line.charAt(3) == 0x75) // ADDU
			{
				s += t + "100001";
			}
			else if (line.charAt(3) == 0x64) // ADDD
			{
				s += t + "00100";
			}
			else //ADD
			{
				s += t + "100000";
			}
			line = s;
		}
		
		return line;
	}
	
	public static String ib(String line)
	{
		String s;
		
		if (line.contains("q")) //BEQZ
		{
			s = "000100";
		}
		else if (line.contains("z")) //BNEZ
		{
			s = "000101";
		}
		else if (line.contains("t")) // BFPT
		{
			s = "000110";
		}
		else //BFPF
		{
			s = "000111";
		}
		
		return s + line;
	}
	
	public static String id(String line)
	{
		String s = "000000";
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
		String s = "000000";
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
		String s = "000000";
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
		String s;
		String t;
		if (line.contains("a"))
		{
			if (line.contains("al")) //JALR
			{
				t = jType2(line);
				s = "010011" + t;
			}
			else //JAL
			{
				t = jType(line);
				s = "000011" + t;
			}
		}
		else
		{
			if (line.contains("jr")) //JR
			{
				t = jType2(line);
				s = "010010" + t;
			}
			else //J
			{
				t = jType(line);
				s = "000001" + t;
			}
		}
		line = s;
		return line;
	}
	
	public static String il(String line)
	{
		String s = "000000";
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
				s += t + "10100";
			}
			else //LF
			{
				t = lType(line);
				s = "100110" + t;
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
				t = lType(line);
				s = "100101" + t;
			}
			else if (line.contains("i")) //LHI
			{
				t = lType2(line);
				s = "100000" + t;
			}
			else //LH
			{
				t = lType(line);
				s = "100001" + t;
			}
		}
		else if (line.contains("w")) //LW
		{
			t = lType(line);
			return "100000" + t;
		//	s =  + t;
		}
		else //LD
		{
			t = lType(line);
			s = "100111" + t;
		}
		return s;
	}
	
	public static String in(String line)
	{
		String s = "000000";
		
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
		String s = "000000";
		
		if (line.contains("i")) //ORI
		{
			line = "001101" + iType(line.replaceAll("ri", "t"));
		}
		else //OR
		{
			line = s + rType56(line.replaceAll("r ", "t")) + "100101";
		}
		
		return line;
	}
	
	public static String ix(String line)
	{
		String s = "000000";
		
		if (line.contains("i")) //XORI
		{
			line = "001110" + iType(line.replaceAll("ri", "t"));
		}
		else //XOR
		{
			line = s + rType56(line.replaceAll("r ", "t")) + "100110";
		}
		return line;
	}
	
}
