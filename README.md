# DLX-Assembler
DLX Assembler in Java for CS5483 (Computer Architecture)

A DLX Assembler for CS5483 (Computer Architecture), encoding all opcodes found on p.127 of
_The DLX Instruction Set Architecture Handbook_ by Philip M Sailer and David R Kaeli, excepting
those opcodes deemed to use special registers, and adding an all "0x0" nop code. These excluded
opcodes include: cvtf2d, cvtf2i, cvtd2f, cvtd2i, cvti2f, cvti2d, movfp2i, movi2fp, movi2s, movs2i, 
rfe, and trap. 

1) Do to some _slight_ misreading of instructions, I thought that we had to impliment the opcodes
  and opcode encodings on our own, thus a fair portion of this file is devoted to this alone, 
  including but not limited to instruction() and its helper methods, which follow the pattern
  i*(), where * is the first letter of the instruction [ia(), is()], while a few have further
  helper methods following the pattern i*#(), where * remains the first letter of the instruction
  and # is the most prominent letter in the subgroup [isb(), isl()].
  
2) All instructions are incoded except those listed above (cvtf2d, cvtf2i, cvtd2f, cvtd2i, cvti2f, 
  cvti2d, movfp2i, movi2fp, movi2s, movs2i, rfe, and trap), which were deemed to use special registers.
  However, while parenthsises are supported in lType() and sType() instructions, commas are not
  supported. Additionally, all registers are to be written with two-digits. So the instruction 
  "add r1, r18, r31" is to be written "add r01 r18 r31". Named locations are only supported with
  jType() and bType() instructions.
  
3) All labels are assumed to be the only content on their respective lines. IE, "label:" is acceptable
  while "label: add r01, r18, r31" is not. 
  
4) It is assumed that all instructions and registers are entered correctly, so the assembler does not
  check to make sure 0 <= register < 32, or that all instructions are spelled correctly. There are a few
  instances where a mispelled instruction will still meet the requirements for an instruction encoding
  and so will be treated as such.
  
5) Directives only support single lines of ints, floats, and doubles. To add more than one, the same number
  of instructions is required. IE, ".word 123" is good, ".word 123, 456" is not. 
  
6) String inputs are not to be seperated by double or single quotes. IE, ".ascii dog" good, ".ascii 'dog'"
  is not.

7) All doubles are treated as floats. 

Because of 1-7 (but especially 1), it is assumed that none of the tests pass (this is also because, while 
  the file would compile, invoking "java Assembler input.dlx" never finished running on student). However, 
  because of this a slightly modified version called Assembler2.java was created that would always take 
  input from the input.dlx file in the same folder as it and print to output.hex. While this will run, it 
  will not print to output.hex or output.txt, although it will do both on my personal computer. This may be
  because of the occasional Windows-to-Student machine issues we sometimes have. Both files can be found 
  on GitHub, under the DLX-Assembler repository. 
  
Input from Command Line: https://github.com/nicoleal/DLX-Assembler/blob/master/src/Assembler.java

Input from "input.dlx": https://github.com/nicoleal/DLX-Assembler/blob/nicoleal-patch-1/src/Assembler.java
