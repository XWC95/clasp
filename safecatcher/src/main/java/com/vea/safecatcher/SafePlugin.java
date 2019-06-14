package com.vea.safecatcher;

import com.vea.clasp.plugin.api.Capt;
import com.vea.clasp.plugin.api.OutputProvider;
import com.vea.clasp.plugin.api.Plugin;
import com.vea.clasp.plugin.api.annotations.Def;
import com.vea.clasp.plugin.api.asm.CaptClassVisitor;
import com.vea.clasp.plugin.api.graph.ClassInfo;
import com.vea.clasp.plugin.api.graph.Status;
import com.vea.clasp.plugin.api.log.Logger;
import com.vea.clasp.plugin.api.process.AnnotationProcessor;
import com.vea.clasp.plugin.api.transform.ClassRequest;
import com.vea.clasp.plugin.api.transform.ClassTransformer;
import com.vea.clasp.plugin.api.util.RelativeDirectoryProvider;
import com.vea.safecatcher.rt.Match;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

@Def(supportedAnnotationTypes = "com.vea.safecatcher.rt.Match")
public class SafePlugin extends Plugin<Capt> {
    static final String CACHE_NAME = "match.json";
    private MethodReplacer replacer;
    RelativeDirectoryProvider provider;
    private Capt capt;
    private Logger mLogger;

    @Override
    public void onCreate(Capt capt) throws IOException {
        this.capt = capt;
        provider = capt.getOutputs().getProvider(OutputProvider.Type.CACHE);

        mLogger = capt.getContext().getLogger(MethodReplacer.class);

        replacer = new MethodReplacer(mLogger);
        if (capt.isIncremental()) {
            replacer.read(new InputStreamReader(provider.asSource(CACHE_NAME).inputStream()));
        }

//        Map<String, Object> args = capt.getArgs().getMyArguments().arguments();
//        if (!args.get("plugin_defined_args1").equals(121322)) {
//            throw new AssertionError();
//        }
//        if (!((Map) args.get("plugin_defined_args2")).isEmpty()) {
//            throw new AssertionError();
//        }
    }

    @Override
    public AnnotationProcessor onProcessAnnotations() {
        return replacer.toAnnotationProcessor();
    }

    @Override
    public ClassTransformer onTransformClass() {
        return new ClassTransformer() {
            @Override
            public ClassRequest beforeTransform() {
                return new ClassRequest() {
                    @Override
                    public Set<String> extraSpecified() {
                        return replacer.extra();
                    }

                    @Override
                    public Scope scope() {
                        return capt.isIncremental() && replacer.hasNew() ? Scope.ALL : Scope.CHANGED;
                    }
                };
            }

            @Override
            public CaptClassVisitor onTransform(ClassInfo classInfo, boolean required) {
                if (capt.isIncremental() && classInfo.status() != Status.NOT_CHANGED && classInfo.status() != Status.ADDED) {
                    replacer.onClassRemovedOrUpdated(classInfo.name());
                }
                if (classInfo.exists() && !replacer.isMatchClass(classInfo.name())) {
                    return new SafeClassVisitor();
                }
                return null;
            }

            @Override
            public void afterTransform() throws IOException {
                Writer writer = new OutputStreamWriter(provider.asSink(SafePlugin.CACHE_NAME).outputStream(), Charset.defaultCharset());
                replacer.write(writer);
            }
        };
    }

    class SafeClassVisitor extends CaptClassVisitor {

        private String className;

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                    mLogger.lifecycle(name);

                    if (!replacer.match(className, owner, name, desc, mv)) {
                        mv.visitMethodInsn(opcode, owner, name, desc, itf);
                    } else {
                        context().notifyChanged();
                    }
                }
            };
        }
    }
}
