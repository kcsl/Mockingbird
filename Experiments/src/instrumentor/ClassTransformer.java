package instrumentor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author rodykers
 * <p>
 * Does nothing more than call the MethodTransformer.
 */
public class ClassTransformer extends ClassVisitor {
    public ClassTransformer(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int i, int access, String s, String s1, String s2, String[] strings) {
        access &= ~Opcodes.ACC_FINAL;
        access &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        access |= Opcodes.ACC_PUBLIC;
        super.visit(i, access, s, s1, s2, strings);
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name,
            String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        access &= ~Opcodes.ACC_FINAL;
        access &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        access |= Opcodes.ACC_PUBLIC;
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new MethodTransformer(mv);
        }
        return mv;
    }
}
