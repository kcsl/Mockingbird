package instrumentor;


import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;

/**
 * @author rodykers
 * <p>
 * Does nothing more than call the AFLMethodVisitor.
 */
public class AFLClassVisitor extends ClassVisitor {
    public AFLClassVisitor(ClassVisitor cv) {
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
            mv = new AFLMethodVisitor(mv);
        }
        return mv;
    }

    public static AsmVisitorWrapper create() {
        return new AsmVisitorWrapper() {
            @Override
            public int mergeWriter(int flags) {
                return flags;
            }

            @Override
            public int mergeReader(int flags) {
                return flags;
            }

            @Override
            public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
                    Implementation.Context implementationContext, TypePool typePool,
                    FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags,
                    int readerFlags) {
                return new AFLClassVisitor(classVisitor);
            }
        };
    }
}
