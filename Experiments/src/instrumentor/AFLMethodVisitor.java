package instrumentor;

import edu.cmu.sv.kelinci.Mem;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashSet;
import java.util.Random;

import static net.bytebuddy.jar.asm.Opcodes.*;

/**
 * @author rodykers
 * <p>
 * Adds AFL-like instrumentation to branches.
 * <p>
 * Uses the ASM MethodVisitor to instrument the start of methods,
 * the location immediately after a branch (else case), as well as
 * all labels.
 * <p>
 * There are also methods in MethodVisitor that we could override
 * to instrument tableswitch, lookupswitch and try-catch. But as those
 * jump to labels in any case (including default), instrumenting the
 * labels only is enough.
 */
public class AFLMethodVisitor extends MethodVisitor {

    Random r;
    private HashSet<Integer> ids;

    public AFLMethodVisitor(MethodVisitor mv) {
        super(ASM5, mv);

        ids = new HashSet<>();
        r = new Random();
    }

    /**
     * Best effort to generate a random id that is not already in use.
     */
    private int getNewLocationId() {
        int id;
        int tries = 0;
        do {
            id = r.nextInt(Mem.SIZE);
            tries++;
        } while (tries <= 10 && ids.contains(id));
        ids.add(id);
        return id;
    }

    /**
     * Instrument a program location, AFL style. Each location gets a
     * compile time random ID, hopefully unique, but maybe not.
     * <p>
     * Instrumentation is the bytecode translation of this:
     * <p>
     * Mem.mem[id^Mem.prev_location]++;
     * Mem.prev_location = id >> 1;
     */
    private void instrumentLocation() {
        Integer id = getNewLocationId();
        mv.visitFieldInsn(GETSTATIC, "instrumentor/AFLPathMem", "mem", "[B");
        mv.visitLdcInsn(id);
        mv.visitFieldInsn(GETSTATIC, "instrumentor/AFLPathMem", "prev_location", "I");
        mv.visitInsn(IXOR);
        mv.visitInsn(DUP2);
        mv.visitInsn(BALOAD);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitInsn(I2B);
        mv.visitInsn(BASTORE);
        mv.visitIntInsn(SIPUSH, (id >> 1));
        mv.visitFieldInsn(PUTSTATIC, "instrumentor/AFLPathMem", "prev_location", "I");
    }

    @Override
    public void visitCode() {
        mv.visitCode();

        /**
         *  Add instrumentation at start of method.
         */
        instrumentLocation();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        mv.visitJumpInsn(opcode, label);

        /**
         *  Add instrumentation after the jump.
         *  Instrumentation for the if-branch is handled by visitLabel().
         */
        instrumentLocation();
    }

    @Override
    public void visitLabel(Label label) {
        mv.visitLabel(label);

        /**
         * Since there is a label, we most probably (surely?) jump to this location. Instrument.
         */
        instrumentLocation();
    }

    public static DynamicType.Builder<?> applyAFLTransformation(DynamicType.Builder<?> builder,
            ElementMatcher<? super MethodDescription> descriptions) {
        return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().writerFlags(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES).method(descriptions,
                (AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper) (instrumentedType, instrumentedMethod, methodVisitor, implementationContext, typePool, writerFlags, readerFlags) ->
                        new AFLMethodVisitor(methodVisitor)));
    }

}
