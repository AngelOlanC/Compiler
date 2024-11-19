           .MODEL SMALL                     
           .486                     
           .STACK                     
           .DATA                     
msg001     DB   80 DUP('$')         
n001       DW   ?                   
i001       DW   ?                   
AUX_STRING DB   80 DUP('$')         
           .CODE                     
MAIN       PROC FAR                 
           .STARTUP                     
           MOV  [msg001 + 2], 'i'   
           MOV  [msg001 + 3], 'n'   
           MOV  [msg001 + 4], 'g'   
           MOV  [msg001 + 5], 'r'   
           MOV  [msg001 + 6], 'e'   
           MOV  [msg001 + 7], 's'   
           MOV  [msg001 + 8], 'e'   
           MOV  [msg001 + 9], ' '   
           MOV  [msg001 + 10], 'n'  
           MOV  [msg001 + 11], '$'  
                                    
           MOV  AH, 09H             
           LEA  DX, [msg001 + 2]    
           INT  21H                 
                                    
           MOV  AH, 02H             
           MOV  DL, 0AH             
           INT  21H                 
                                    
           MOV  AH, 0AH             
           LEA  DX, AUX_STRING      
           INT  21H                 
                                    
           MOV  BX, 10              
           XOR  AX, AX              
           LEA  SI, AUX_STRING      
           INC  SI                  
           INC  SI                  
                                    
VUELVE_LECTURA0:                          
           MOV  DL, [SI]            
           CMP  DL, 0DH             
           JE   FIN_LECTURA0        
                                    
           MUL  BX                  
           MOV  DX, AX              
           MOV  AL, [SI]            
           SUB  AL, '0'             
           CBW                      
                                    
           ADD  DX, AX              
           MOV  AX, DX              
           INC  SI                  
           JMP  VUELVE_LECTURA0     
                                    
FIN_LECTURA0:                          
           MOV  n001, AX            
                                    
           MOV  AH, 02H             
           MOV  DL, 0AH             
           INT  21H                 
                                    
           MOV  BX, 1               
           MOV  i001, BX            
                                    
START_WHILE1:                          
           MOV  BX, i001            
           MOV  AX, BX              
           MOV  BX, n001            
           MOV  DX, BX              
           CMP  AX, DX              
           JNLE END_WHILE1          
                                    
           MOV  BX, i001            
           MOV  AX, BX              
           MOV  BX, 10              
           XOR  CX, CX              
                                    
PUSHEAR2:                           
           XOR  DX, DX              
           DIV  BX                  
           PUSH DX                  
           INC  CX                  
           TEST AX, AX              
           JNZ  PUSHEAR2            
                                    
POPEAR2:                            
           POP  DX                  
           ADD  DL, '0'             
           MOV  AH, 02H             
           INT  21H                 
           LOOP POPEAR2             
                                    
           MOV  AH, 02H             
           MOV  DL, 0AH             
           INT  21H                 
                                    
           PUSH AX                  
           PUSH DX                  
                                    
           MOV  BX, i001            
                                    
           MOV  AX, BX              
                                    
           MOV  BX, 1               
                                    
           ADD  AX, BX              
           MOV  BX, AX              
           POP  DX                  
           POP  AX                  
                                    
           MOV  i001, BX            
                                    
           JMP  START_WHILE1        
                                    
END_WHILE1:                          
                                    
           .EXIT                     
MAIN       ENDP                     
END                                 
