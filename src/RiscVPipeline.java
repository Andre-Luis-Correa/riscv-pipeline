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
                "00000000001000110000100000110011", // add rd, rs1, rs2 (R-Type) -> add reg1, reg6, reg2
                "00000000010000100001100010010011", // addi r3, r2, 4 (I-Type)
                "00000000001100010001000001100011", // beq r1, r3, 4 (B-Type)
                "00000000001100100001001000100011"  // sw r3, 4(r2) (S-Type)
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
        }
    }

    // EX: Execute
    private static void executeStage() {
        if (!IDEXIR.equals("NOP")) {
            String opcode = IDEXIR.substring(25, 32); // Opcode
            int funct3 = Integer.parseInt(IDEXIR.substring(17, 20), 2); // Funct3
            int imm = Integer.parseInt(IDEXIR.substring(0, 12), 2); // Imediato

            switch (opcode) {
                case "0110011": // R-Type (add)
                    EXMEMALUOut = IDEXA + IDEXB;
                    break;
                case "0010011": // I-Type (addi)
                    EXMEMALUOut = IDEXA + imm;
                    break;
                case "1100011": // B-Type (beq)
                    if (IDEXA == IDEXB) {
                        PC += imm - 4; // Ajusta o PC (já incrementado no fetch)
                    }
                    break;
                case "0100011": // S-Type (sw)
                    EXMEMALUOut = IDEXA + imm; // Calcula endereço
                    break;
            }
            EXMEMIR = IDEXIR; // Passa a instrução adiante
        }
    }

    // MEM: Memory Access
    private static void memoryAccessStage() {
        if (!EXMEMIR.equals("NOP")) {
            String opcode = EXMEMIR.substring(25, 32); // Opcode
            int rs2 = Integer.parseInt(EXMEMIR.substring(7, 12), 2); // rs2

            if (opcode.equals("0100011")) { // sw
                Memory[EXMEMALUOut] = Regs[rs2]; // Armazena na memória
            } else {
                MEMWBValue = EXMEMALUOut; // Passa o valor adiante
            }
            MEMWBIR = EXMEMIR; // Passa a instrução adiante
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
