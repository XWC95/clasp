package com.vea.safecatcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.logging.Logger;

public final class TimingMethodAdapter extends LocalVariablesSorter implements Opcodes {
    private int startVarIndex;
    private String methodName;
    private Logger logger;
    public TimingMethodAdapter(String name, int access, String desc, MethodVisitor mv) {
        super(Opcodes.ASM5, access, desc, mv);
        this.methodName = name
            .replace("/", ".")
            .replace("\\","");

        logger = Logger.getLogger(TimingMethodAdapter.class.getSimpleName());
        logger.warning(methodName);
//      methodName =   me.vea.cost.test\<init>
    }

    @Override
    public void visitCode() {
        super.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/vea/safecatcher/rt/TimeUtil", "currentTimeMillis", "()J", false);
        startVarIndex = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(Opcodes.LSTORE, startVarIndex);
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            mv.visitMethodInsn(INVOKESTATIC, "com/vea/safecatcher/rt/TimeUtil", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, startVarIndex);
            mv.visitInsn(LSUB);
            int index = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, index);
            mv.visitLdcInsn(methodName);
            mv.visitVarInsn(LLOAD, index);
            mv.visitMethodInsn(INVOKESTATIC, "com/vea/safecatcher/rt/BlockManager", "timingMethod", "(Ljava/lang/String;J)V", false);
        }
        super.visitInsn(opcode);
    }
}