//import java.util.Arrays;
//
//public class RiscVPipeline {
//    // Registradores do pipeline
//    private static String IFIDIR, IDEXIR, EXMEMIR, MEMWBIR;
//    private static int PC = 0; // Program Counter
//    private static int[] Regs = new int[32]; // Banco de registradores
//    private static int[] Memory = new int[256]; // Memória simulada
//    private static int IDEXA, IDEXB, EXMEMALUOut, MEMWBValue;
//    private static int branchImm = 0;
//
//    private static boolean branchTaken = false; // Controle de se o branch foi tomado
//
//    public static void main(String[] args) {
//        // Inicialização
//        inicializarPipeline();
//
////        // Conjunto de instruções em binário (simulando uma sequência)
////        String[] instrucoes = {
////                "0000000 00010 00110 000 10000 0110011", // add rd, rs1, rs2 (R-Type) -> add reg16, reg6, reg2
////                "000000000100 00100 000 10001 0010011", // addi rd, rs1, 4 (I-Type) - addi reg17, rs4, 4
////                "0 000000 00011 01010 000 0010 0 1100011", // beq rs1, rs2, L (B-Type) -> beq reg8, reg3, 4
////                "0000000 11110 11100 010 0010 0 0100011"  // sw rs2, imm(rs1) (S-Type) -> sw reg30, 4(reg28)
////        };
//
//        // Conjunto de instruções em binário (simulando uma sequência)
//        String[] instrucoes = {
//                "00000000001000110000100000110011", // add rd, rs1, rs2 (R-Type) -> add reg16, reg6, reg2
//                "00000000010000100000100010010011", // addi rd, rs1, 4 (I-Type) - addi reg17, rs4, 4
//                "00000000100001000000010001100011", // beq rs1, rs2, L (B-Type) -> beq reg8, reg8, 8
//                "00000000001000110000110010110011", // add rd, rs1, rs2 (R-Type) -> add reg25, reg6, reg2
//                "00000000001101010000001001100011", // beq rs1, rs2, L (B-Type) -> beq reg8, reg3, 4
//                "00000001111011100010001000100011"  // sw rs2, imm(rs1) (S-Type) -> sw reg30, 4(reg28)
//        };
//
//        // Simulação do pipeline por ciclos de clock
//        for (int ciclo = 0; ciclo < instrucoes.length + 4; ciclo++) {
//            System.out.println("Ciclo: " + ciclo);
//            writeBackStage();
//            memoryAccessStage();
//            executeStage();
//            decodeStage();
//            fetchStage(instrucoes);
//            imprimirPipeline();
//        }
//    }
//
//    private static void inicializarPipeline() {
//        IFIDIR = IDEXIR = EXMEMIR = MEMWBIR = "NOP"; // Inicializa com NOP
//        for (int i = 0; i < 32; i++) {
//            Regs[i] = i; // Inicializa registradores com valores de índice
//        }
//    }
//
//    // IF: Instruction Fetch
//    private static void fetchStage(String[] instrucoes) {
//        if (PC / 4 < instrucoes.length) {
//            IFIDIR = instrucoes[PC / 4]; // Busca a instrução da memória
//            PC += 4; // Incrementa o PC
//        } else {
//            IFIDIR = "NOP"; // Insere NOP após instruções
//        }
//    }
//
//    // ID: Instruction Decode
//    private static void decodeStage() {
//        if (!IFIDIR.equals("NOP")) {
//            String opcode = IFIDIR.substring(25, 32); // Opcode (últimos 7 bits)
//            int rs1 = Integer.parseInt(IFIDIR.substring(12, 17), 2); // rs1
//            int rs2 = Integer.parseInt(IFIDIR.substring(7, 12), 2); // rs2
//            int rd = Integer.parseInt(IFIDIR.substring(20, 25), 2); // rd
//            int imm = Integer.parseInt(IFIDIR.substring(0, 12), 2); // Imediato
//
//            // Decodifica os valores dos registradores
//            IDEXA = Regs[rs1];
//            IDEXB = Regs[rs2];
//
//            // Decodifica a instrução
//            IDEXIR = IFIDIR; // Passa adiante a instrução
//        }
//    }
//
//    //    // EX: Execute
//    private static void executeStage() {
//        if (!IDEXIR.equals("NOP")) {
//            String opcode = IDEXIR.substring(25, 32); // Opcode
//            int funct3 = Integer.parseInt(IDEXIR.substring(17, 20), 2); // Funct3
//            int imm = 0; // Inicializamos o imediato
//
//            // Extrair imediato de acordo com o formato da instrução
//            switch (opcode) {
//                case "0110011": // R-Type (add) não usa imediato
//                    EXMEMALUOut = IDEXA + IDEXB;
//                    break;
//
//                case "0010011": // I-Type (addi)
//                    imm = Integer.parseInt(IDEXIR.substring(0, 12), 2);
//                    if (IDEXIR.charAt(0) == '1') {
//                        imm |= 0xFFFFF000; // Extensão de sinal
//                    }
//                    EXMEMALUOut = IDEXA + imm;
//                    break;
//
//                case "0100011": // S-Type (sw)
//                    int immHigh = Integer.parseInt(IDEXIR.substring(0, 7), 2); // imm[11:5]
//                    int immLow = Integer.parseInt(IDEXIR.substring(20, 25), 2); // imm[4:0]
//                    imm = (immHigh << 5) | immLow; // Combina as partes
//                    if (IDEXIR.charAt(0) == '1') {
//                        imm |= 0xFFFFF000; // Extensão de sinal
//                    }
//                    EXMEMALUOut = IDEXA + imm; // Calcula endereço de memória
//                    break;
//
//                case "1100011": // B-Type (beq)
//                    int imm12 = Integer.parseInt(IDEXIR.substring(0, 1), 2); // imm[12]
//                    int imm10_5 = Integer.parseInt(IDEXIR.substring(1, 7), 2); // imm[10:5]
//                    int imm4_1 = Integer.parseInt(IDEXIR.substring(20, 24), 2); // imm[4:1]
//                    int imm11 = Integer.parseInt(IDEXIR.substring(24, 25), 2); // imm[11]
//                    imm = (imm12 << 12) | (imm11 << 11) | (imm10_5 << 5) | (imm4_1 << 1);
//
//                    if (IDEXIR.charAt(0) == '1') {
//                        imm |= 0xFFFFF000; // Extensão de sinal
//                    }
//
//                    // Verifica se o branch é tomado
//                    if (IDEXA == IDEXB) {
//                        branchTaken = true;
//                        // Atualiza o PC para o endereço de salto
//                        PC += imm - 4; // O PC já foi incrementado no fetch
//                        System.out.println("Branch tomado. Atualizando PC para: " + PC);
//                    } else {
//                        branchTaken = false;
//                    }
//                    break;
//            }
//            EXMEMIR = IDEXIR; // Passa a instrução adiante
//        }
//    }
//
//    // MEM: Memory Access
//    private static void memoryAccessStage() {
//        if (!EXMEMIR.equals("NOP")) {
//            String opcode = EXMEMIR.substring(25, 32); // Opcode
//            int rs2 = Integer.parseInt(EXMEMIR.substring(7, 12), 2); // rs2
//
//            if (opcode.equals("0100011")) { // sw
//                Memory[EXMEMALUOut] = Regs[rs2]; // Armazena na memória
//            } else {
//                MEMWBValue = EXMEMALUOut; // Passa o valor adiante
//            }
//            MEMWBIR = EXMEMIR; // Passa a instrução adiante
//        }
//    }
//
//    // WB: Write Back
//    private static void writeBackStage() {
//        if (!MEMWBIR.equals("NOP")) {
//            String opcode = MEMWBIR.substring(25, 32); // Opcode
//            int rd = Integer.parseInt(MEMWBIR.substring(20, 25), 2); // rd
//
//            if (opcode.equals("0110011") || opcode.equals("0010011")) { // add ou addi
//                Regs[rd] = MEMWBValue; // Escreve o resultado
//            }
//        }
//    }
//
//    // Imprime o estado do pipeline
//    private static void imprimirPipeline() {
//        System.out.println("PC: " + PC);
//        System.out.println("Regs: " + Arrays.toString(Regs));
//        System.out.println("Memory: " + Arrays.toString(Memory));
//        System.out.println("IFIDIR: " + IFIDIR);
//        System.out.println("IDEXIR: " + IDEXIR + ", IDEXA: " + IDEXA + ", IDEXB: " + IDEXB);
//        System.out.println("EXMEMIR: " + EXMEMIR + ", EXMEMALUOut: " + EXMEMALUOut);
//        System.out.println("MEMWBIR: " + MEMWBIR + ", MEMWBValue: " + MEMWBValue);
//        System.out.println("======================================");
//    }
//}

import java.util.Arrays;

public class RiscVPipeline {
    // Registradores do pipeline
    private static String IFIDIR, IDEXIR, EXMEMIR, MEMWBIR;
    private static int PC = 0; // Program Counter
    private static int[] Regs = new int[32]; // Banco de registradores
    private static int[] Memory = new int[256]; // Memória simulada
    private static int IDEXA, IDEXB, EXMEMALUOut, MEMWBValue;

    public static void main(String[] args) {
        // Inicialização
        inicializarPipeline();

        // Conjunto de instruções em binário (simulando uma sequência)
        String[] instrucoes = {
                "00000000001000110000100000110011", // add rd, rs1, rs2 (R-Type) -> add reg16, reg6, reg2
                "00000000010000100000100010010011", // addi rd, rs1, 4 (I-Type) - addi reg17, rs4, 4
                "00000000100001000000010001100011", // beq rs1, rs2, L (B-Type) -> beq reg8, reg8, 8
                "00000000001000110000110010110011", // add rd, rs1, rs2 (R-Type) -> add reg25, reg6, reg2
                "00000000001101010000001001100011", // beq rs1, rs2, L (B-Type) -> beq reg8, reg3, 4
                "00000001111011100010001000100011"  // sw rs2, imm(rs1) (S-Type) -> sw reg30, 4(reg28)
        };

        // Simulação do pipeline por ciclos de clock
        for (int ciclo = 0; ciclo < instrucoes.length + 4; ciclo++) {
            System.out.println("Ciclo: " + ciclo);
            writeBackStage();
            memoryAccessStage();
            executeStage();
            decodeStage();
            fetchStage(instrucoes);
            imprimirPipeline();
        }
    }

    private static void inicializarPipeline() {
        IFIDIR = IDEXIR = EXMEMIR = MEMWBIR = "NOP"; // Inicializa com NOP
        for (int i = 0; i < 32; i++) {
            Regs[i] = i; // Inicializa registradores com valores de índice
        }
    }

    // IF: Instruction Fetch
    private static void fetchStage(String[] instrucoes) {
        if (PC / 4 < instrucoes.length) {
            IFIDIR = instrucoes[PC / 4]; // Busca a instrução da memória
            PC += 4; // Incrementa o PC
        } else {
            IFIDIR = "NOP"; // Insere NOP após instruções
        }
    }

    // ID: Instruction Decode
    private static void decodeStage() {
        if (!IFIDIR.equals("NOP")) {
            String opcode = IFIDIR.substring(25, 32); // Opcode (últimos 7 bits)
            int rs1 = Integer.parseInt(IFIDIR.substring(12, 17), 2); // rs1
            int rs2 = Integer.parseInt(IFIDIR.substring(7, 12), 2); // rs2
            int rd = Integer.parseInt(IFIDIR.substring(20, 25), 2); // rd
            int imm = Integer.parseInt(IFIDIR.substring(0, 12), 2); // Imediato

            // Decodifica os valores dos registradores
            IDEXA = Regs[rs1];
            IDEXB = Regs[rs2];

            // Decodifica a instrução
            IDEXIR = IFIDIR; // Passa adiante a instrução
        } else {
            IDEXIR = "NOP";
        }
    }

    // EX: Execute
    private static void executeStage() {
        if (!IDEXIR.equals("NOP")) {
            String opcode = IDEXIR.substring(25, 32); // Opcode
            int imm = 0; // Inicializamos o imediato

            // Extrair imediato de acordo com o formato da instrução
            switch (opcode) {
                case "0110011": // R-Type (add) não usa imediato
                    EXMEMALUOut = IDEXA + IDEXB;
                    break;

                case "0010011": // I-Type (addi)
                    imm = Integer.parseInt(IDEXIR.substring(0, 12), 2);
                    if (IDEXIR.charAt(0) == '1') {
                        imm |= 0xFFFFF000; // Extensão de sinal
                    }
                    EXMEMALUOut = IDEXA + imm;
                    break;

                case "0100011": // S-Type (sw)
                    int immHigh = Integer.parseInt(IDEXIR.substring(0, 7), 2); // imm[11:5]
                    int immLow = Integer.parseInt(IDEXIR.substring(20, 25), 2); // imm[4:0]
                    imm = (immHigh << 5) | immLow; // Combina as partes
                    if (IDEXIR.charAt(0) == '1') {
                        imm |= 0xFFFFF000; // Extensão de sinal
                    }
                    EXMEMALUOut = IDEXA + imm; // Calcula endereço de memória
                    break;

                case "1100011": // B-Type (beq)
                    int imm12 = Integer.parseInt(IDEXIR.substring(0, 1), 2); // imm[12]
                    int imm10_5 = Integer.parseInt(IDEXIR.substring(1, 7), 2); // imm[10:5]
                    int imm4_1 = Integer.parseInt(IDEXIR.substring(20, 24), 2); // imm[4:1]
                    int imm11 = Integer.parseInt(IDEXIR.substring(24, 25), 2); // imm[11]
                    imm = (imm12 << 12) | (imm11 << 11) | (imm10_5 << 5) | (imm4_1 << 1);

                    if (IDEXIR.charAt(0) == '1') {
                        imm |= 0xFFFFF000; // Extensão de sinal
                    }

                    // Verifica se o branch é tomado
                    if (IDEXA == IDEXB) {
                        // Atualiza o PC para o endereço de salto
                        PC += imm - 4; // O PC já foi incrementado no fetch
                        System.out.println("Branch tomado. Atualizando PC para: " + PC);

                        // Insere NOPs apenas nos estágios IF e ID para flush
                        IFIDIR = "NOP";  // Insere NOP no estágio de IF
                        IDEXIR = "NOP";  // Insere NOP no estágio de ID
                    }
                    break;
            }
            EXMEMIR = IDEXIR; // Passa a instrução adiante
        } else {
            EXMEMIR = "NOP";
        }
    }

    // MEM: Memory Access
    private static void memoryAccessStage() {
        if (!EXMEMIR.equals("NOP")) {
            String opcode = EXMEMIR.substring(25, 32); // Opcode
            int rs2 = Integer.parseInt(EXMEMIR.substring(7, 12), 2); // rs2

            if (opcode.equals("0100011")) { // sw
                // Verifica se o endereço de memória está dentro dos limites
                if (EXMEMALUOut >= 0 && EXMEMALUOut < Memory.length) {
                    Memory[EXMEMALUOut] = Regs[rs2]; // Armazena na memória
                } else {
                    System.out.println("Erro: Endereço de memória inválido para sw: " + EXMEMALUOut);
                }
            } else {
                MEMWBValue = EXMEMALUOut; // Passa o valor adiante
            }
            MEMWBIR = EXMEMIR; // Passa a instrução adiante
        } else {
            MEMWBIR = "NOP";
        }
    }

    // WB: Write Back
    private static void writeBackStage() {
        if (!MEMWBIR.equals("NOP")) {
            String opcode = MEMWBIR.substring(25, 32); // Opcode
            int rd = Integer.parseInt(MEMWBIR.substring(20, 25), 2); // rd

            if (opcode.equals("0110011") || opcode.equals("0010011")) { // add ou addi
                Regs[rd] = MEMWBValue; // Escreve o resultado
            }
        }
    }

    // Imprime o estado do pipeline
    private static void imprimirPipeline() {
        System.out.println("PC: " + PC);
        System.out.println("Regs: " + Arrays.toString(Regs));
        System.out.println("Memory: " + Arrays.toString(Memory));
        System.out.println("IFIDIR: " + IFIDIR);
        System.out.println("IDEXIR: " + IDEXIR + ", IDEXA: " + IDEXA + ", IDEXB: " + IDEXB);
        System.out.println("EXMEMIR: " + EXMEMIR + ", EXMEMALUOut: " + EXMEMALUOut);
        System.out.println("MEMWBIR: " + MEMWBIR + ", MEMWBValue: " + MEMWBValue);
        System.out.println("======================================");
    }
}
